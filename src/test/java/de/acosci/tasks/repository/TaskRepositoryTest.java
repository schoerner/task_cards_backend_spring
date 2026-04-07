package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.BoardColumn;
import de.acosci.tasks.model.entity.Project;
import de.acosci.tasks.model.entity.ProjectMember;
import de.acosci.tasks.model.entity.Role;
import de.acosci.tasks.model.entity.Task;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.model.enums.BoardColumnType;
import de.acosci.tasks.model.enums.ProjectRole;
import de.acosci.tasks.model.enums.TaskPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@TestPropertySource("classpath:application-test.properties")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProjectMemberRepository projectMemberRepository;
    @Autowired
    private BoardColumnRepository boardColumnRepository;
    @Autowired
    private RoleRepository roleRepository;

    private User user1;
    private Project project1;
    private BoardColumn notAssigned;

    @BeforeEach
    void setUp() {
        Role roleUser = new Role();
        roleUser.setName(Role.RoleName.ROLE_USER);
        roleRepository.save(roleUser);

        user1 = new User();
        user1.setEmail("test@user1.de");
        user1.setPassword("password1");
        user1.setFirstName("Max");
        user1.setLastName("Mustermann");
        user1.setRoles(Set.of(roleUser));
        user1 = userRepository.save(user1);

        project1 = new Project();
        project1.setName("Projekt1");
        project1.setCreator(user1);
        project1 = projectRepository.save(project1);

        ProjectMember member = new ProjectMember();
        member.setProject(project1);
        member.setUser(user1);
        member.setRole(ProjectRole.OWNER);
        projectMemberRepository.save(member);

        notAssigned = new BoardColumn();
        notAssigned.setProject(project1);
        notAssigned.setName("Not assigned");
        notAssigned.setPosition(0);
        notAssigned.setType(BoardColumnType.SYSTEM);
        notAssigned.setDeletable(false);
        notAssigned = boardColumnRepository.save(notAssigned);
    }

    @Test
    void aProjectCanHaveMultipleTasks() {
        Task task1 = new Task();
        task1.setTitle("Task1");
        task1.setDescription("Task1 description");
        task1.setCreator(user1);
        task1.setProject(project1);
        task1.setBoardColumn(notAssigned);
        task1.setPriority(TaskPriority.MEDIUM);

        Task task2 = new Task();
        task2.setTitle("Task2");
        task2.setDescription("Task2 description");
        task2.setCreator(user1);
        task2.setProject(project1);
        task2.setBoardColumn(notAssigned);
        task2.setPriority(TaskPriority.HIGH);

        taskRepository.save(task1);
        taskRepository.save(task2);

        assertEquals(2, taskRepository.findAllByProject_IdAndArchivedFalse(project1.getId()).size());
        assertEquals(2, taskRepository.findAllByProject_IdAndBoardColumn_IdAndArchivedFalse(project1.getId(), notAssigned.getId()).size());
    }
}
