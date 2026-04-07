package de.acosci.tasks.model.enums;

/**
 * Event types stored in the task activity log.
 */
public enum TaskActivityType {
    CREATED,
    UPDATED,
    COLUMN_CHANGED,
    PRIORITY_CHANGED,
    DUE_DATE_CHANGED,
    ASSIGNEE_ADDED,
    ASSIGNEE_REMOVED,
    LABEL_ADDED,
    LABEL_REMOVED,
    COMMENT_CREATED,
    COMMENT_UPDATED,
    COMMENT_DELETED,
    SUBTASK_CREATED,
    SUBTASK_UPDATED,
    SUBTASK_DELETED,
    DEPENDENCY_ADDED,
    DEPENDENCY_REMOVED,
    ARCHIVED,
    RESTORED
}
