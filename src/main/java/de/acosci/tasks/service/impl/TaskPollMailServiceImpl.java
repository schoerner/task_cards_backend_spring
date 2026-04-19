package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.entity.TaskPoll;
import de.acosci.tasks.model.entity.TaskPollParticipant;
import de.acosci.tasks.repository.TaskPollRepository;
import de.acosci.tasks.service.TaskPollMailService;
import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
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
    public void sendInvitations(Long taskId) {
        TaskPoll poll = taskPollRepository.findByTask_Id(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task poll not found for task: " + taskId));

        List<TaskPollParticipant> recipients = poll.getParticipants().stream()
                .filter(participant -> participant.getExternalEmail() != null && !participant.getExternalEmail().isBlank())
                .toList();

        for (TaskPollParticipant participant : recipients) {
            ensureInvitationToken(participant);
            sendInvitationMail(poll, participant);
            participant.setInvitedAt(OffsetDateTime.now());
        }

        taskPollRepository.save(poll);
    }

    @Override
    public void sendReminders(Long taskId) {
        TaskPoll poll = taskPollRepository.findByTask_Id(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task poll not found for task: " + taskId));

        List<TaskPollParticipant> recipients = poll.getParticipants().stream()
                .filter(participant -> participant.getExternalEmail() != null && !participant.getExternalEmail().isBlank())
                .filter(participant -> participant.getRespondedAt() == null)
                .toList();

        for (TaskPollParticipant participant : recipients) {
            ensureInvitationToken(participant);
            sendReminderMail(poll, participant);
            participant.setLastReminderAt(OffsetDateTime.now());
        }

        taskPollRepository.save(poll);
    }

    private void ensureInvitationToken(TaskPollParticipant participant) {
        if (participant.getInvitationToken() == null || participant.getInvitationToken().isBlank()) {
            String rawToken = UUID.randomUUID().toString();
            participant.setInvitationToken(rawToken);
            participant.setInvitationTokenHash(hashToken(rawToken));
        }
    }

    private void sendInvitationMail(TaskPoll poll, TaskPollParticipant participant) {
        String subject = "Einladung zur Terminabfrage: " + poll.getTitle();
        String body = """
                Hallo,

                Sie wurden zu einer Terminabfrage eingeladen.

                Titel:
                %s

                Beschreibung:
                %s

                Link zur Teilnahme:
                %s

                Über diesen Link können Sie Ihre Verfügbarkeit eintragen und später auch erneut ändern, solange die Terminabfrage noch läuft.

                Viele Grüße
                %s
                """.formatted(
                safe(poll.getTitle()),
                safe(poll.getDescription()),
                buildPublicLink(participant),
                safe(fromName)
        );

        sendMail(participant.getExternalEmail(), subject, body);
    }

    private void sendReminderMail(TaskPoll poll, TaskPollParticipant participant) {
        String subject = "Erinnerung zur Terminabfrage: " + poll.getTitle();
        String body = """
                Hallo,

                dies ist eine Erinnerung zur Terminabfrage.

                Titel:
                %s

                Link zur Teilnahme:
                %s

                Über diesen Link können Sie Ihre bisherige Verfügbarkeit erneut öffnen und ändern.

                Viele Grüße
                %s
                """.formatted(
                safe(poll.getTitle()),
                buildPublicLink(participant),
                safe(fromName)
        );

        sendMail(participant.getExternalEmail(), subject, body);
    }

    private String buildPublicLink(TaskPollParticipant participant) {
        return publicBaseUrl + "/polls/respond/" + participant.getInvitationToken();
    }

    private void sendMail(String to, String subject, String body) {
        try {
            var mimeMessage = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());

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
            helper.setText(body, false);

            mailSender.send(mimeMessage);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not send task poll email.", ex);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
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