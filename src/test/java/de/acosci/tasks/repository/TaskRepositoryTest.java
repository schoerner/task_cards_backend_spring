package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.Project;
import de.acosci.tasks.model.entity.Task;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.service.impl.TaskServiceImpl;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)//, classes = {TaskServiceImpl.class})
@DataJpaTest // https://courses.baeldung.com/courses/1295711/lectures/30127904
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
//@Transactional(propagation = Propagation.NOT_SUPPORTED)
@TestPropertySource("classpath:application-test.properties") // H2 for Testing: https://medium.com/@akshatakanaje08/setting-up-h2-for-testing-in-spring-boot-application-7f016220a475
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TimeRecordRepository timeRecordRepository;

    @Autowired
    private UserRepository userRepository;

    private final User user1 = new User();
    private final User user2 = new User();
    private final Project project1 = new Project();
    private final Task task1 = new Task();
    private final Task task2 = new Task();

    @BeforeEach
    void setUp() {
        user1.setEmail("test@user1.de");
        user1.setPassword("password1");
        user1.setFirstName("Max");
        user1.setLastName("Mustermann");
        user1.setRegistration(new Date());
        user1.setTasks(new ArrayList<>());
        user1.setProjects(new HashSet<>());

        user2.setEmail("test@user2.de");
        user2.setPassword("password2");
        user2.setFirstName("Heike");
        user2.setLastName("Musterfrau");
        user2.setRegistration(new Date());
        user2.setTasks(new ArrayList<>());
        user2.setProjects(new HashSet<>());

        project1.setName("Projekt1");
        project1.setTasks(new ArrayList<>());
        project1.setUsers(new HashSet<>());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testCreateAProjectWith2Users() {
        assertEquals(0, userRepository.count());
        assertEquals(0, projectRepository.count());

        assertNull(user1.getId());
        User insertedUser1 = userRepository.save(user1);
        assertEquals(1, userRepository.count());
        assertNotNull(insertedUser1.getId());

        insertedUser1 = userRepository.save(user1);
        assertEquals(1, userRepository.count());

        User insertedUser2 = userRepository.save(user2);
        assertEquals(2, userRepository.count());

        assertNull(project1.getId());
        Project insertedProject1 = projectRepository.save(project1);
        assertNotNull(insertedProject1.getId());
        assertEquals(1, projectRepository.count());


        assertTrue(project1.getUsers().add(user1));
        assertEquals(1, projectRepository.findById(insertedProject1.getId()).get().getUsers().size());

        assertFalse(project1.getUsers().add(user1));
        assertTrue(project1.getUsers().remove(user1));
        insertedProject1 = projectRepository.save(project1);
        assertEquals(0, projectRepository.findById(insertedProject1.getId()).get().getUsers().size());

    }

    @Test
    void aProjectWith2TasksWithAnUserAndCreatorEach() {
        assertTrue(project1.getUsers().add(user1));
        assertFalse(project1.getUsers().add(user1));
        assertTrue(project1.getUsers().add(user2));
        assertTrue(project1.getUsers().contains(user1));
        assertTrue(project1.getUsers().contains(user2));

        task1.setTitle("Task1");
        task1.setDescription("Task1 of user 1 description");
        task1.setCreator(user1);
        task1.setProject(project1);
        user1.getTasks().add(task1);
        project1.getTasks().add(task1);

        task2.setTitle("Task2");
        task2.setDescription("Task2 of user2 description");
        task2.setCreator(user2);
        task2.setProject(project1);
        user2.getTasks().add(task2);
        user1.getTasks().add(task2);

        assertEquals(0, userRepository.count());
        assertEquals(0, projectRepository.count());
        assertEquals(0, taskRepository.count());
        assertEquals(0, timeRecordRepository.count());

        // AA: Set breakpoint and step over: Will the ID be assigned by the repository/H2 database?
        User insertedUser1 = userRepository.save(user1);
        assertEquals(user1, insertedUser1);
        assertEquals(1, userRepository.count());
        User insertedUser2 = userRepository.save(user2);
        assertEquals(2, userRepository.count());
        assertEquals(user2, insertedUser2);

        assertNull(project1.getId());
        Project insertedProject1 = projectRepository.save(project1);
        assertNotNull(insertedProject1.getId());
        assertEquals(project1, insertedProject1);
        assertEquals(2, projectRepository.findById(insertedProject1.getId()).get().getUsers().size());

        assertEquals(1, projectRepository.count());
        taskRepository.save(task1);
        assertEquals(1, taskRepository.count());

        project1.getTasks().add(task2);
        taskRepository.save(task2);
        assertEquals(2, taskRepository.count());

        projectRepository.delete(project1);

        assertEquals(2, userRepository.count());
        assertEquals(0, projectRepository.count());
        assertEquals(0, taskRepository.count());
        assertEquals(0, timeRecordRepository.count());
    }
}