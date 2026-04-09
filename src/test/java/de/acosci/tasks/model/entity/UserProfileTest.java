package de.acosci.tasks.model.entity;

import de.acosci.tasks.repository.UserProfileRepository;
import de.acosci.tasks.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource("classpath:application-test.properties")
class UserProfileTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    final User user1 = new User();
    final UserProfile user1Profile = new UserProfile();

    @BeforeEach
    void setUp() {
        user1.setEmail("test@user1.de");
        user1.setPassword("password1");
        user1.setFirstName("Max");
        user1.setLastName("Mustermann");
        user1.setRegistration(new Date());

        user1Profile.setName("Nick1");
        user1Profile.setDescription("Description1");
        user1Profile.setPictureUrl("url1");

        user1Profile.setUser(user1);
    }

    @Test
    void testSaveUserProfile() {
        assertEquals(0, userRepository.findAll().size());
        assertEquals(0, userProfileRepository.findAll().size());

        assertNull(user1.getId());
        assertNull(user1Profile.getId());

        User insertedUser1 = userRepository.save(user1);
        assertEquals(1, userRepository.findAll().size());
        assertEquals(0, userProfileRepository.findAll().size());
        assertNotNull(insertedUser1.getId());

        user1Profile.setUser(insertedUser1);
        insertedUser1.setProfile(user1Profile);

        UserProfile insertedUser1Profile = userProfileRepository.save(user1Profile);
        assertEquals(1, userRepository.findAll().size());
        assertEquals(1, userProfileRepository.findAll().size());
        assertNotNull(insertedUser1Profile.getId());
        assertEquals(insertedUser1.getId(), insertedUser1Profile.getId());

        userRepository.delete(insertedUser1);
        assertEquals(0, userRepository.findAll().size());
        assertEquals(0, userProfileRepository.findAll().size());
    }
}