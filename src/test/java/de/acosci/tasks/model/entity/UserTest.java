package de.acosci.tasks.model.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    final User user1 = new User();
    final User user2 = new User();
    final User user1Copy = new User();

    @BeforeEach
    void setUp() {
        user1.setId(1L);
        user1.setEmail("test@user1.de");
        user1.setPassword("password1");
        user1.setFirstName("Max");
        user1.setLastName("Mustermann");

        user2.setId(2L);
        user2.setEmail("test@user2.de");
        user2.setPassword("password2");
        user2.setFirstName("Heike");
        user2.setLastName("Musterfrau");

        user1Copy.setId(1L);
        user1Copy.setEmail("other@user1.de");
        user1Copy.setPassword("another");
        user1Copy.setFirstName("Other");
        user1Copy.setLastName("Name");
    }

    @Test
    void testEqualsUsesIdIdentity() {
        assertNotEquals(user1, user2);
        assertEquals(user1, user1);
        assertEquals(user1, user1Copy);
    }

    @Test
    void testHashCodeUsesIdIdentity() {
        assertNotEquals(user1.hashCode(), user2.hashCode());
        assertEquals(user1.hashCode(), user1Copy.hashCode());
    }

    @Test
    void testSetUsesEntityIdentity() {
        Set<User> users = new HashSet<>();
        assertTrue(users.add(user1));
        assertFalse(users.add(user1Copy));
        assertTrue(users.add(user2));
        assertEquals(2, users.size());
    }
}
