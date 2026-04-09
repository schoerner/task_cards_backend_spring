package de.acosci.tasks.service;

import de.acosci.tasks.model.entity.BoardColumn;
import de.acosci.tasks.model.entity.Project;
import de.acosci.tasks.repository.BoardColumnRepository;
import de.acosci.tasks.repository.ProjectRepository;
import de.acosci.tasks.repository.TaskRepository;
import de.acosci.tasks.service.impl.BoardColumnServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {BoardColumnServiceImpl.class})
class BoardColumnServiceImplTest {

    @MockitoBean
    private BoardColumnRepository boardColumnRepository;
    @MockitoBean
    private ProjectRepository projectRepository;
    @MockitoBean
    private TaskRepository taskRepository;

    @Autowired
    private BoardColumnServiceImpl boardColumnService;

    private BoardColumn first;
    private BoardColumn second;
    private BoardColumn third;

    @BeforeEach
    void setUp() {
        Project project = new Project();
        project.setId(2L);

        first = new BoardColumn();
        first.setId(10L);
        first.setProject(project);
        first.setName("A");
        first.setPosition(0);

        second = new BoardColumn();
        second.setId(11L);
        second.setProject(project);
        second.setName("B");
        second.setPosition(1);

        third = new BoardColumn();
        third.setId(12L);
        third.setProject(project);
        third.setName("C");
        third.setPosition(2);

        when(boardColumnRepository.findAllByProject_IdOrderByPositionAsc(2L))
                .thenReturn(List.of(first, second, third))
                .thenReturn(List.of(third, first, second));
        when(boardColumnRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void reorderColumns_updatesPositionsAtomically() {
        List<BoardColumn> reordered = boardColumnService.reorderColumns(2L, List.of(12L, 10L, 11L));

        assertEquals(List.of(12L, 10L, 11L), reordered.stream().map(BoardColumn::getId).toList());
        assertEquals(0, third.getPosition());
        assertEquals(1, first.getPosition());
        assertEquals(2, second.getPosition());
    }

    @Test
    void reorderColumns_rejectsMissingOrDuplicateIds() {
        assertThrows(IllegalArgumentException.class,
                () -> boardColumnService.reorderColumns(2L, List.of(10L, 10L, 11L)));
    }
}
