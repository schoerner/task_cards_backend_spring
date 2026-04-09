package de.acosci.tasks.model.enums;

import lombok.Getter;

/**
 * Priority scale from 0 (very low) to 5 (very high).
 */
@Getter
public enum TaskPriority {
    LOW(0),
    MEDIUM(1),
    HIGH(2),
    URGENT(3);

    private final int level;

    TaskPriority(int level) {
        this.level = level;
    }

}
