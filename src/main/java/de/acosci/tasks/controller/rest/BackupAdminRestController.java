package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.dto.BackupFileResponseDTO;
import de.acosci.tasks.model.dto.BackupTriggerResponseDTO;
import de.acosci.tasks.service.BackupAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/admin/backups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Backup Administration", description = "REST-API für Backup-Listen, Download und manuelles Starten")
public class BackupAdminRestController {

    private final BackupAdminService backupAdminService;

    @Operation(summary = "Alle verfügbaren Backup-Dateien abrufen")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Backups erfolgreich geladen",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BackupFileResponseDTO.class)))
            ),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<BackupFileResponseDTO>> getBackups() {
        return ResponseEntity.ok(backupAdminService.listBackups());
    }

    @Operation(summary = "Backup manuell starten")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Backup-Runner wurde erfolgreich angesprochen",
                    content = @Content(schema = @Schema(implementation = BackupTriggerResponseDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content)
    })
    @PostMapping("/run")
    public ResponseEntity<BackupTriggerResponseDTO> runBackup() {
        return ResponseEntity.ok(backupAdminService.triggerBackup());
    }

    @Operation(summary = "Backup-Datei herunterladen")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Datei wird heruntergeladen"),
            @ApiResponse(responseCode = "404", description = "Datei nicht gefunden", content = @Content)
    })
    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> downloadBackup(@PathVariable String fileName) {
        Resource resource = backupAdminService.getBackupResource(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(resource.getFilename()).build().toString()
                )
                .body(resource);
    }
}