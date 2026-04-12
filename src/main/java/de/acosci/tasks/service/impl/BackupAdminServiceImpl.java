package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.dto.BackupFileResponseDTO;
import de.acosci.tasks.model.dto.BackupTriggerResponseDTO;
import de.acosci.tasks.service.BackupAdminService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class BackupAdminServiceImpl implements BackupAdminService {

    private static final Pattern SAFE_FILE_NAME = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_task-backup\\.zip$");

    private final Path backupRoot;
    private final String runnerUrl;
    private final String runnerToken;
    private final HttpClient httpClient;

    public BackupAdminServiceImpl(
            @Value("${app.backups.storage-path:/opt/backups/task}") String backupStoragePath,
            @Value("${app.backups.runner-url:http://task_backup_runner:8081/api/v1/backup/run}") String runnerUrl,
            @Value("${app.backups.runner-token:change-me}") String runnerToken
    ) {
        this.backupRoot = Path.of(backupStoragePath).normalize();
        this.runnerUrl = runnerUrl;
        this.runnerToken = runnerToken;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public List<BackupFileResponseDTO> listBackups() {
        if (!Files.exists(backupRoot)) {
            return List.of();
        }

        try (var stream = Files.list(backupRoot)) {
            List<BackupFileResponseDTO> backups = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> SAFE_FILE_NAME.matcher(path.getFileName().toString()).matches())
                    .map(this::toDto)
                    .sorted(Comparator.comparing(BackupFileResponseDTO::getCreatedAt).reversed())
                    .toList();

            for (int i = 0; i < backups.size(); i++) {
                backups.get(i).setLatest(i == 0);
            }

            return backups;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Backups konnten nicht gelesen werden.", e);
        }
    }

    @Override
    public BackupTriggerResponseDTO triggerBackup() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(runnerUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + runnerToken)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 202) {
                return new BackupTriggerResponseDTO("STARTED", "Backup wurde gestartet.");
            }

            if (response.statusCode() == 409) {
                return new BackupTriggerResponseDTO("RUNNING", "Es läuft bereits ein Backup.");
            }

            if (response.statusCode() == 401) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Backup-Runner lehnt das Token ab.");
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Backup-Runner antwortete mit HTTP " + response.statusCode()
            );
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Backup-Runner ist derzeit nicht erreichbar.",
                    e
            );
        }
    }

    @Override
    public Resource getBackupResource(String fileName) {
        if (fileName == null || !SAFE_FILE_NAME.matcher(fileName).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ungültiger Dateiname.");
        }

        Path filePath = backupRoot.resolve(fileName).normalize();

        if (!filePath.startsWith(backupRoot)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ungültiger Dateipfad.");
        }

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Backup-Datei nicht gefunden.");
        }

        return new FileSystemResource(filePath);
    }

    private BackupFileResponseDTO toDto(Path path) {
        try {
            BackupFileResponseDTO dto = new BackupFileResponseDTO();
            dto.setFileName(path.getFileName().toString());
            dto.setSizeBytes(Files.size(path));

            Instant createdAt = Files.getLastModifiedTime(path).toInstant();
            dto.setCreatedAt(createdAt);
            dto.setRetentionBucket(classifyRetentionBucket(createdAt));
            dto.setLatest(false);

            return dto;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Backup-Datei konnte nicht verarbeitet werden.", e);
        }
    }

    private String classifyRetentionBucket(Instant createdAt) {
        long ageDays = Duration.between(createdAt, Instant.now()).toDays();

        if (ageDays <= 14) {
            return "DAILY";
        }
        if (ageDays <= 56) {
            return "WEEKLY";
        }
        if (ageDays <= 366) {
            return "MONTHLY";
        }
        return "YEARLY";
    }
}