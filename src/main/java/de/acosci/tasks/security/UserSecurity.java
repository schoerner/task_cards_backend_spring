package de.acosci.tasks.security;

import de.acosci.tasks.model.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {

    public boolean isSelf(Long userId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof User currentUser)) {
            return false;
        }

        return currentUser.getId().equals(userId);
    }
}