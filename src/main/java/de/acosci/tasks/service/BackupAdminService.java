package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.BackupFileResponseDTO;
import de.acosci.tasks.model.dto.BackupTriggerResponseDTO;
import org.springframework.core.io.Resource;

import java.util.List;

public interface BackupAdminService {
    List<BackupFileResponseDTO> listBackups();
    BackupTriggerResponseDTO triggerBackup();
    Resource getBackupResource(String fileName);
}