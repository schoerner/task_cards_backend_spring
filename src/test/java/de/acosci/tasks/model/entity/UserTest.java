package de.acosci.tasks.model.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    final User user1 = new User();
    final User user2 = new User();
    final User user12 = new User();

    @BeforeEach
    void setUp() {
        user1.setEmail("test@user1.de");
        user1.setPassword("password1");
        user1.setFirstName("Max");
        user1.setLastName("Mustermann");
        user1.setRegistration(new Date());

        user2.setEmail("test@user2.de");
        user2.setPassword("password2");
        user2.setFirstName("Heike");
        user2.setLastName("Musterfrau");
        user2.setRegistration(new Date());

        user12.setEmail("test@user1.de");
        user12.setPassword("password1");
        user12.setFirstName("Max");
        user12.setLastName("Mustermann");
        user12.setRegistration(new Date());
    }

    @Test
    void testEquals() {
        assertNotEquals(user1, user2);
        assertEquals(user1, user1);
        assertEquals(user1, user12);
        assertEquals(user2, user2);
        assertNotEquals(user2, user1);
    }

    @Test
    void testHashCode() {
        assertNotEquals(user1.hashCode(), user2.hashCode());
        assertEquals(user1.hashCode(), user1.hashCode());
        assertEquals(user1.hashCode(), user12.hashCode());
    }

    @Test
    void testToString() {
        System.out.println(user1.toString());
        System.out.println(user2.toString());
        System.out.println(user12.toString());
    }

    @Test
    void testSet() {
        Set<User> users = new HashSet<>();
        assertTrue( users.add(user1) );
        assertFalse( users.add(user1) );
        assertTrue( users.add(user2) );
        assertTrue( users.remove(user1) );
        assertFalse( users.remove(user1) );
        assertTrue( users.remove(user2) );
    }
}