package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.dto.TaskPollMailRequestDTO;
import de.acosci.tasks.model.entity.TaskPoll;
import de.acosci.tasks.model.entity.TaskPollParticipant;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.repository.TaskPollRepository;
import de.acosci.tasks.service.TaskPollMailService;
import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskPollMailServiceImpl implements TaskPollMailService {

    private final TaskPollRepository taskPollRepository;
    private final JavaMailSender mailSender;

    @Value("${app.poll.public-base-url}")
    private String publicBaseUrl;

    @Value("${app.poll.mail.from}")
    private String fromAddress;

    @Value("${app.poll.mail.from-name:ACoSci Tasks}")
    private String fromName;

    @Value("${app.poll.mail.reply-to:}")
    private String replyTo;

    @Override
    public void sendInvitations(Long taskId, TaskPollMailRequestDTO dto) {
        TaskPoll poll = findPoll(taskId);

        List<TaskPollParticipant> recipients = poll.getParticipants().stream()
                .filter(participant -> hasText(resolveRecipientEmail(participant)))
                .toList();

        OffsetDateTime now = OffsetDateTime.now();
        for (TaskPollParticipant participant : recipients) {
            ensureInvitationToken(participant);
            sendInvitationMail(poll, participant, dto);
            participant.setInvitedAt(now);
        }

        taskPollRepository.save(poll);
    }

    @Override
    public void sendReminders(Long taskId, TaskPollMailRequestDTO dto) {
        TaskPoll poll = findPoll(taskId);

        List<TaskPollParticipant> recipients = poll.getParticipants().stream()
                .filter(participant -> hasText(resolveRecipientEmail(participant)))
                .filter(participant -> participant.getRespondedAt() == null)
                .toList();

        OffsetDateTime now = OffsetDateTime.now();
        for (TaskPollParticipant participant : recipients) {
            ensureInvitationToken(participant);
            sendReminderMail(poll, participant, dto);
            participant.setLastReminderAt(now);
        }

        taskPollRepository.save(poll);
    }

    @Override
    public void sendFinalizationNotification(Long taskId, TaskPollMailRequestDTO dto) {
        TaskPoll poll = findPoll(taskId);

        if (poll.getFinalizedStartAt() == null) {
            throw new IllegalStateException("Task poll has not been finalized yet.");
        }

        List<TaskPollParticipant> recipients = poll.getParticipants().stream()
                .filter(participant -> hasText(resolveRecipientEmail(participant)))
                .toList();

        for (TaskPollParticipant participant : recipients) {
            sendFinalizationMail(poll, participant, dto);
        }
    }

    private TaskPoll findPoll(Long taskId) {
        return taskPollRepository.findByTask_Id(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task poll not found for task: " + taskId));
    }

    private void ensureInvitationToken(TaskPollParticipant participant) {
        if (participant.getInvitationToken() == null || participant.getInvitationToken().isBlank()) {
            String rawToken = UUID.randomUUID().toString();
            participant.setInvitationToken(rawToken);
            participant.setInvitationTokenHash(hashToken(rawToken));
        }
    }

    private void sendInvitationMail(TaskPoll poll, TaskPollParticipant participant, TaskPollMailRequestDTO dto) {
        String subject = hasText(dto != null ? dto.getSubject() : null)
                ? dto.getSubject()
                : "Terminabfrage: " + safe(poll.getTitle());

        String publicLink = buildPublicLink(participant);
        String customText = normalizeCustomText(dto != null ? dto.getMessageText() : null);
        String customHtml = normalizeCustomHtml(dto != null ? dto.getMessageHtml() : null);

        String textBody = buildInvitationTextBody(poll, publicLink, customText);
        String htmlBody = wrapInMailLayout(
                "Terminabfrage",
                "Sie wurden zu einer Terminabfrage eingeladen.",
                customHtml,
                "Zur Terminabfrage",
                publicLink,
                null
        );

        sendMail(resolveRecipientEmail(participant), subject, textBody, htmlBody);
    }

    private void sendReminderMail(TaskPoll poll, TaskPollParticipant participant, TaskPollMailRequestDTO dto) {
        String subject = hasText(dto != null ? dto.getSubject() : null)
                ? dto.getSubject()
                : "Erinnerung zur Terminabfrage: " + safe(poll.getTitle());

        String publicLink = buildPublicLink(participant);
        String customText = normalizeCustomText(dto != null ? dto.getMessageText() : null);
        String customHtml = normalizeCustomHtml(dto != null ? dto.getMessageHtml() : null);

        String textBody = buildReminderTextBody(poll, publicLink, customText);
        String htmlBody = wrapInMailLayout(
                "Erinnerung zur Terminabfrage",
                "Dies ist eine Erinnerung zur noch offenen Terminabfrage.",
                customHtml,
                "Zur Terminabfrage",
                publicLink,
                null
        );

        sendMail(resolveRecipientEmail(participant), subject, textBody, htmlBody);
    }

    private void sendFinalizationMail(TaskPoll poll, TaskPollParticipant participant, TaskPollMailRequestDTO dto) {
        String subject = hasText(dto != null ? dto.getSubject() : null)
                ? dto.getSubject()
                : "Termin steht fest: " + safe(poll.getTitle());

        String formattedSlot = formatSlot(poll.getFinalizedStartAt(), poll.getFinalizedEndAt());
        String customText = normalizeCustomText(dto != null ? dto.getMessageText() : null);
        String customHtml = normalizeCustomHtml(dto != null ? dto.getMessageHtml() : null);

        String textBody = buildFinalizationTextBody(formattedSlot, customText);
        String htmlBody = wrapInMailLayout(
                "Gemeinsamer Termin steht fest",
                "Die Terminabfrage ergab folgenden Slot als gemeinsamen Termin:",
                customHtml,
                null,
                null,
                formattedSlot
        );

        String icsContent = buildFinalizationIcs(poll, participant);

        sendMail(
                resolveRecipientEmail(participant),
                subject,
                textBody,
                htmlBody,
                "termin.ics",
                icsContent
        );
    }

    private String buildInvitationTextBody(TaskPoll poll, String publicLink, String customText) {
        StringBuilder builder = new StringBuilder();
        builder.append("Hallo,\n\n");
        builder.append("Sie wurden zu einer Terminabfrage eingeladen.\n\n");
        builder.append("Titel:\n").append(safe(poll.getTitle())).append("\n\n");

        if (hasText(customText)) {
            builder.append(customText).append("\n\n");
        }

        builder.append("Link zur Teilnahme:\n").append(publicLink).append("\n\n");
        builder.append("Viele Grüße\n").append(safe(fromName));
        builder.append("\n\n").append(buildPrivacyFooterText());
        return builder.toString();
    }

    private String buildReminderTextBody(TaskPoll poll, String publicLink, String customText) {
        StringBuilder builder = new StringBuilder();
        builder.append("Hallo,\n\n");
        builder.append("Dies ist eine Erinnerung zur noch offenen Terminabfrage.\n\n");
        builder.append("Titel:\n").append(safe(poll.getTitle())).append("\n\n");

        if (hasText(customText)) {
            builder.append(customText).append("\n\n");
        }

        builder.append("Link zur Teilnahme:\n").append(publicLink).append("\n\n");
        builder.append("Viele Grüße\n").append(safe(fromName));
        builder.append("\n\n").append(buildPrivacyFooterText());
        return builder.toString();
    }

    private String buildFinalizationTextBody(String formattedSlot, String customText) {
        StringBuilder builder = new StringBuilder();
        builder.append("Hallo,\n\n");
        builder.append("Der gemeinsame Termin steht fest.\n\n");
        builder.append(formattedSlot).append("\n\n");

        if (hasText(customText)) {
            builder.append(customText).append("\n\n");
        }

        builder.append("Viele Grüße\n").append(safe(fromName));
        builder.append("\n\n").append(buildPrivacyFooterText());
        return builder.toString();
    }

    private String normalizeCustomText(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private String normalizeCustomHtml(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private String wrapInMailLayout(String headline, String intro, String messageHtml, String actionLabel, String actionUrl, String highlightedSlot) {
        StringBuilder builder = new StringBuilder();
        builder.append("<div style=\"font-family:Arial,sans-serif;font-size:14px;line-height:1.6;color:#212529;max-width:720px;\">");

        if (hasText(headline)) {
            builder.append("<h2 style=\"margin:0 0 16px 0;font-size:22px;\">").append(escapeHtml(headline)).append("</h2>");
        }

        if (hasText(intro)) {
            builder.append("<p style=\"margin:0 0 16px 0;\">").append(escapeHtml(intro)).append("</p>");
        }

        if (hasText(highlightedSlot)) {
            builder.append("<p style=\"margin:0 0 16px 0;padding:12px 14px;background:#f8f9fa;border-radius:8px;font-weight:700;\">")
                    .append(escapeHtml(highlightedSlot))
                    .append("</p>");
        }

        if (hasText(messageHtml)) {
            builder.append("<div style=\"margin:0 0 20px 0;\">").append(messageHtml).append("</div>");
        }

        if (hasText(actionLabel) && hasText(actionUrl)) {
            builder.append("<p style=\"margin:0 0 20px 0;\"><a href=\"")
                    .append(escapeHtml(actionUrl))
                    .append("\" style=\"display:inline-block;padding:10px 16px;background:#0d6efd;color:#ffffff;text-decoration:none;border-radius:6px;\">")
                    .append(escapeHtml(actionLabel))
                    .append("</a></p>");
            builder.append("<p style=\"margin:0;color:#6c757d;font-size:12px;word-break:break-all;\">")
                    .append(escapeHtml(actionUrl))
                    .append("</p>");
        }

        if (hasText(buildPrivacyFooterHtml())) {
            builder.append("<hr style=\"margin:24px 0;border:none;border-top:1px solid #dee2e6;\">");
            builder.append("<p style=\"margin:0;color:#6c757d;font-size:12px;\">").append(buildPrivacyFooterHtml()).append("</p>");
        }

        builder.append("</div>");
        return builder.toString();
    }

    private String formatSlot(OffsetDateTime startAt, OffsetDateTime endAt) {
        if (startAt == null) {
            return "";
        }

        ZoneId zoneId = ZoneId.of("Europe/Berlin");
        var localStart = startAt.toInstant().atZone(zoneId);
        var localEnd = endAt != null ? endAt.toInstant().atZone(zoneId) : null;

        DateTimeFormatter startFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        DateTimeFormatter endFormatter = DateTimeFormatter.ofPattern("HH:mm");
        return localEnd != null
                ? localStart.format(startFormatter) + " – " + localEnd.format(endFormatter) + " Uhr"
                : localStart.format(startFormatter) + " Uhr";
    }

    private String buildPrivacyFooterText() {
        return "Datenschutzhinweis: Im Rahmen dieser Terminabfrage werden Ihr Name und Ihre E-Mail-Adresse ausschließlich zur Durchführung der Terminabfrage und der zugehörigen Benachrichtigungen verwendet. Die Verarbeitung erfolgt nicht zu Werbezwecken. Die Daten werden auf einem Server in Deutschland verarbeitet und nach Wegfall des Zwecks im Rahmen der Terminabfrage wieder gelöscht.";
    }

    private String buildPrivacyFooterHtml() {
        return escapeHtml(buildPrivacyFooterText());
    }

    private String buildPublicLink(TaskPollParticipant participant) {
        return publicBaseUrl + "/polls/respond/" + participant.getInvitationToken();
    }

    private String buildFinalizationIcs(TaskPoll poll, TaskPollParticipant participant) {
        if (poll.getFinalizedStartAt() == null) {
            throw new IllegalStateException("Task poll has not been finalized yet.");
        }

        OffsetDateTime startAt = poll.getFinalizedStartAt();
        OffsetDateTime endAt = poll.getFinalizedEndAt() != null
                ? poll.getFinalizedEndAt()
                : startAt.plusHours(1);

        String uid = "task-poll-" + poll.getId() + "-" + participant.getId() + "@acosci.de";
        String dtStamp = formatIcsUtc(Instant.now());
        String dtStart = formatIcsUtc(startAt.toInstant());
        String dtEnd = formatIcsUtc(endAt.toInstant());

        String summary = escapeIcsText("Termin: " + safe(poll.getTitle()));

        StringBuilder description = new StringBuilder();
        description.append("Der gemeinsame Termin zur Terminabfrage steht fest.");
        if (hasText(poll.getTitle())) {
            description.append("\\n\\nTitel: ").append(escapeIcsText(poll.getTitle()));
        }
        if (hasText(publicBaseUrl) && participant.getInvitationToken() != null) {
            description.append("\\n\\nTerminabfrage: ")
                    .append(escapeIcsText(buildPublicLink(participant)));
        }

        return "BEGIN:VCALENDAR\r\n"
                + "PRODID:-//ACoSci Tasks//Task Poll//DE\r\n"
                + "VERSION:2.0\r\n"
                + "CALSCALE:GREGORIAN\r\n"
                + "METHOD:REQUEST\r\n"
                + "BEGIN:VEVENT\r\n"
                + "UID:" + uid + "\r\n"
                + "DTSTAMP:" + dtStamp + "\r\n"
                + "DTSTART:" + dtStart + "\r\n"
                + "DTEND:" + dtEnd + "\r\n"
                + "SUMMARY:" + summary + "\r\n"
                + "DESCRIPTION:" + description + "\r\n"
                + "STATUS:CONFIRMED\r\n"
                + "TRANSP:OPAQUE\r\n"
                + "END:VEVENT\r\n"
                + "END:VCALENDAR\r\n";
    }

    private String formatIcsUtc(Instant instant) {
        return DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                .withZone(ZoneOffset.UTC)
                .format(instant);
    }

    private String escapeIcsText(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n")
                .replace("\r", "\\n");
    }

    private String resolveRecipientEmail(TaskPollParticipant participant) {
        if (hasText(participant.getExternalEmail())) {
            return participant.getExternalEmail().trim();
        }

        User user = participant.getUser();
        if (user == null) {
            return null;
        }

        if (user.getProfile() != null && hasText(user.getProfile().getContactEmail())) {
            return user.getProfile().getContactEmail().trim();
        }

        return hasText(user.getEmail()) ? user.getEmail().trim() : null;
    }

    private void sendMail(String to, String subject, String textBody, String htmlBody) {
        sendMail(to, subject, textBody, htmlBody, null, null);
    }

    private void sendMail(String to, String subject, String textBody, String htmlBody, String attachmentFilename, String attachmentContent) {
        try {
            var mimeMessage = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            if (fromName != null && !fromName.isBlank()) {
                helper.setFrom(new InternetAddress(fromAddress, fromName));
            } else {
                helper.setFrom(fromAddress);
            }

            if (replyTo != null && !replyTo.isBlank()) {
                helper.setReplyTo(replyTo);
            }

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(safe(textBody), safe(htmlBody));

            if (hasText(attachmentFilename) && hasText(attachmentContent)) {
                mimeMessage.addHeader("Content-Class", "urn:content-classes:calendarmessage");
                helper.addAttachment(
                        attachmentFilename,
                        new ByteArrayResource(attachmentContent.getBytes(StandardCharsets.UTF_8)),
                        "text/calendar; charset=UTF-8; method=REQUEST"
                );
            }

            mailSender.send(mimeMessage);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not send task poll email.", ex);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String hashToken(String token) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Could not hash token.", ex);
        }
    }
}