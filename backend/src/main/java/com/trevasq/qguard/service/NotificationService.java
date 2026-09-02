package com.trevasq.qguard.service;

import com.trevasq.qguard.config.AppProperties;
import com.trevasq.qguard.domain.Booking;

import jakarta.mail.internet.MimeMessage;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class NotificationService {

    private final JavaMailSender mail;
    private final CalendarService calendar;
    private final AppProperties properties;

    public NotificationService(
            JavaMailSender mail,
            CalendarService calendar,
            AppProperties properties
    ) {
        this.mail = mail;
        this.calendar = calendar;
        this.properties = properties;
    }

    public void confirmation(Booking booking, String url) {
        try {
            MimeMessage message = mail.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setFrom(properties.sender());
            helper.setTo(booking.getEmail());
            helper.setSubject("Your QGuard demo is confirmed");

            helper.setText(
                    "Hello " + booking.getName() + ",\n\n"
                            + "Your QGuard demo is confirmed for "
                            + booking.getStartsAt()
                            + " UTC.\n\n"
                            + "Manage it securely: "
                            + url
            );

            byte[] calendarData = calendar
                    .invitation(booking)
                    .getBytes(StandardCharsets.UTF_8);

            helper.addAttachment(
                    "qguard-demo.ics",
                    new ByteArrayResource(calendarData),
                    "text/calendar"
            );

            mail.send(message);

        } catch (Exception e) {
            throw new NotificationException();
        }
    }

    public static class NotificationException
            extends RuntimeException {
    }
}
