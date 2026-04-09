package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.dto.BoardColumnCreateDTO;
import de.acosci.tasks.model.dto.BoardColumnReorderDTO;
import de.acosci.tasks.model.dto.BoardColumnResponseDTO;
import de.acosci.tasks.model.dto.BoardColumnUpdateDTO;
import de.acosci.tasks.model.entity.BoardColumn;
import de.acosci.tasks.model.mapper.BoardColumnMapper;
import de.acosci.tasks.service.BoardColumnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/board-columns")
@RequiredArgsConstructor
@Tag(name = "Board Columns", description = "REST-API zur Verwaltung der Kanban-Spalten")
public class BoardColumnRestController {

    private final BoardColumnService boardColumnService;

    @Operation(summary = "Kanban-Spalten eines Projekts anzeigen")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.isMemberByEmail(#projectId, authentication.name)")
    public ResponseEntity<List<BoardColumnResponseDTO>> getBoardColumns(@PathVariable Long projectId) {
        List<BoardColumnResponseDTO> result = boardColumnService.getBoardColumns(projectId)
                .stream()
                .map(BoardColumnMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Neue Kanban-Spalte anlegen")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.canManageBoardByEmail(#projectId, authentication.name)")
    public ResponseEntity<BoardColumnResponseDTO> createColumn(@PathVariable Long projectId,
                                                               @Valid @RequestBody BoardColumnCreateDTO dto) {
        BoardColumn column = boardColumnService.createColumn(projectId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(BoardColumnMapper.toResponseDTO(column));
    }

    @Operation(summary = "Kanban-Spalte aktualisieren")
    @PutMapping("/{columnId}")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.canManageBoardByEmail(#projectId, authentication.name)")
    public ResponseEntity<BoardColumnResponseDTO> updateColumn(@PathVariable Long projectId,
                                                               @PathVariable Long columnId,
                                                               @Valid @RequestBody BoardColumnUpdateDTO dto) {
        BoardColumn column = boardColumnService.updateColumn(projectId, columnId, dto);
        return ResponseEntity.ok(BoardColumnMapper.toResponseDTO(column));
    }

    @Operation(summary = "Kanban-Spalten atomar neu anordnen")
    @PatchMapping("/reorder")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.canManageBoardByEmail(#projectId, authentication.name)")
    public ResponseEntity<List<BoardColumnResponseDTO>> reorderColumns(@PathVariable Long projectId,
                                                                       @Valid @RequestBody BoardColumnReorderDTO dto) {
        List<BoardColumnResponseDTO> result = boardColumnService.reorderColumns(projectId, dto.getOrderedColumnIds())
                .stream()
                .map(BoardColumnMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Kanban-Spalte löschen")
    @DeleteMapping("/{columnId}")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.canManageBoardByEmail(#projectId, authentication.name)")
    public ResponseEntity<Void> deleteColumn(@PathVariable Long projectId,
                                             @PathVariable Long columnId,
                                             @RequestParam Long fallbackColumnId) {
        boardColumnService.deleteColumn(projectId, columnId, fallbackColumnId);
        return ResponseEntity.noContent().build();
    }
}
