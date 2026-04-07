package de.acosci.tasks.model.enums;

/**
 * Priority scale from 0 (very low) to 5 (very high).
 */
public enum TaskPriority {
    VERY_LOW(0),
    LOW(1),
    RATHER_LOW(2),
    MEDIUM(3),
    HIGH(4),
    VERY_HIGH(5);

    private final int level;

    TaskPriority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
