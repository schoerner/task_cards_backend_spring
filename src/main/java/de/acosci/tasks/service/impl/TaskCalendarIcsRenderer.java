package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.dto.TaskCalendarAssigneeDTO;
import de.acosci.tasks.model.dto.TaskCalendarEntryDTO;
import de.acosci.tasks.model.dto.TaskCalendarReminderDTO;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class TaskCalendarIcsRenderer {

    private static final DateTimeFormatter ICS_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    public String render(List<TaskCalendarEntryDTO> tasks) {
        StringBuilder sb = new StringBuilder();

        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//ACoSci//Tasks Calendar//DE\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("METHOD:PUBLISH\r\n");
        sb.append("X-WR-CALNAME:ACoSci Tasks\r\n");
        sb.append("NAME:ACoSci Tasks\r\n");
        sb.append("X-WR-TIMEZONE:Europe/Berlin\r\n");

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        for (TaskCalendarEntryDTO task : tasks) {
            OffsetDateTime start = task.getStartAt() != null ? task.getStartAt() : task.getDueDate();
            if (start == null) {
                continue;
            }

            OffsetDateTime end = task.getDueDate() != null ? task.getDueDate() : start.plusMinutes(30);
            if (!end.isAfter(start)) {
                end = start.plusMinutes(30);
            }

            sb.append("BEGIN:VEVENT\r\n");
            sb.append("UID:task-").append(task.getId()).append("@task.acosci.de\r\n");
            sb.append("DTSTAMP:").append(formatUtc(now)).append("\r\n");
            sb.append("DTSTART:").append(formatUtc(start)).append("\r\n");
            sb.append("DTEND:").append(formatUtc(end)).append("\r\n");
            sb.append("SUMMARY:").append(escape(summary(task))).append("\r\n");

            if (task.getProjectName() != null && !task.getProjectName().isBlank()) {
                sb.append("CATEGORIES:").append(escape(task.getProjectName())).append("\r\n");
            }

            if (task.getLocation() != null && !task.getLocation().isBlank()) {
                sb.append("LOCATION:").append(escape(task.getLocation())).append("\r\n");
            }

            if (task.getAssignees() != null) {
                for (TaskCalendarAssigneeDTO assignee : task.getAssignees()) {
                    if (assignee.getEmail() == null || assignee.getEmail().isBlank()) {
                        continue;
                    }

                    sb.append("ATTENDEE");
                    if (assignee.getProfileName() != null && !assignee.getProfileName().isBlank()) {
                        sb.append(";CN=").append(escape(assignee.getProfileName()));
                    }
                    sb.append(":mailto:").append(escape(assignee.getEmail())).append("\r\n");
                }
            }

            if (task.getCalendarReminders() != null) {
                for (TaskCalendarReminderDTO reminder : task.getCalendarReminders()) {
                    if (reminder.getMinutesBefore() == null || reminder.getMinutesBefore() < 0) {
                        continue;
                    }

                    sb.append("BEGIN:VALARM\r\n");
                    sb.append("ACTION:")
                            .append(escape(reminder.getActionType() != null ? reminder.getActionType() : "DISPLAY"))
                            .append("\r\n");
                    sb.append("TRIGGER:-PT").append(reminder.getMinutesBefore()).append("M\r\n");
                    sb.append("DESCRIPTION:")
                            .append(escape(
                                    reminder.getMessage() != null && !reminder.getMessage().isBlank()
                                            ? reminder.getMessage()
                                            : "Erinnerung für " + task.getTitle()
                            ))
                            .append("\r\n");
                    sb.append("END:VALARM\r\n");
                }
            }

            sb.append("DESCRIPTION:")
                    .append(escape(buildDescription(task)))
                    .append("\r\n");

            sb.append("END:VEVENT\r\n");
        }

        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    private String summary(TaskCalendarEntryDTO task) {
        if (task.getProjectName() != null && !task.getProjectName().isBlank()) {
            return task.getTitle() + " (" + task.getProjectName() + ")";
        }
        return task.getTitle();
    }

    private String buildDescription(TaskCalendarEntryDTO task) {
        StringBuilder sb = new StringBuilder();

        if (task.getProjectName() != null) {
            sb.append("Projekt: ").append(task.getProjectName()).append("\n");
        }
        if (task.getBoardColumnName() != null) {
            sb.append("Spalte: ").append(task.getBoardColumnName()).append("\n");
        }
        if (task.getLocation() != null && !task.getLocation().isBlank()) {
            sb.append("Ort: ").append(task.getLocation()).append("\n");
        }

        sb.append("Aktiv: ").append(task.isActive() ? "Ja" : "Nein").append("\n");
        sb.append("Archiviert: ").append(task.isArchived() ? "Ja" : "Nein");

        if (task.getDescription() != null && !task.getDescription().isBlank()) {
            sb.append("\n\nBeschreibung:\n").append(task.getDescription());
        }

        return sb.toString();
    }

    private String formatUtc(OffsetDateTime value) {
        return value.withOffsetSameInstant(ZoneOffset.UTC).format(ICS_DATE_TIME);
    }

    private String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n");
    }
}