package com.trevasq.qguard.service;

import com.trevasq.qguard.config.AppProperties;
import com.trevasq.qguard.domain.Booking;

import jakarta.mail.internet.MimeMessage;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class NotificationService {

    private final JavaMailSender mail;
    private final CalendarService calendar;
    private final AppProperties properties;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMMM yyyy");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a");

    public NotificationService(
            JavaMailSender mail,
            CalendarService calendar,
            AppProperties properties
    ) {
        this.mail = mail;
        this.calendar = calendar;
        this.properties = properties;
    }

    // ============================================================
    // BOOKING CONFIRMATION
    // ============================================================

    public void confirmation(
            Booking booking,
            String managementUrl
    ) {
        try {
            MimeMessage message = mail.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            StandardCharsets.UTF_8.name()
                    );

            helper.setFrom(properties.sender());
            helper.setTo(booking.getEmail());
            helper.setSubject(
                    "Your QGuard Demo is Confirmed — TrevasQ"
            );

            String customerHtml =
                    buildCustomerConfirmationEmail(
                            booking,
                            managementUrl
                    );

            helper.setText(customerHtml, true);

            byte[] calendarData =
                    calendar
                            .invitation(booking)
                            .getBytes(StandardCharsets.UTF_8);

            helper.addAttachment(
                    "qguard-demo.ics",
                    new ByteArrayResource(calendarData),
                    "text/calendar"
            );

            mail.send(message);

            sendDemoGiverNotification(booking);

        } catch (Exception e) {
            throw new NotificationException();
        }
    }

    // ============================================================
    // RESCHEDULE NOTIFICATION
    // ============================================================

    public void rescheduled(
            Booking booking,
            Instant previousStart,
            String managementUrl
    ) {
        try {
            // ----------------------------------------------------
            // Customer email
            // ----------------------------------------------------

            MimeMessage customerMessage =
                    mail.createMimeMessage();

            MimeMessageHelper customerHelper =
                    new MimeMessageHelper(
                            customerMessage,
                            true,
                            StandardCharsets.UTF_8.name()
                    );

            customerHelper.setFrom(properties.sender());
            customerHelper.setTo(booking.getEmail());

            customerHelper.setSubject(
                    "Your QGuard Demo Has Been Rescheduled — TrevasQ"
            );

            String customerHtml =
                    buildCustomerRescheduledEmail(
                            booking,
                            previousStart,
                            managementUrl
                    );

            customerHelper.setText(
                    customerHtml,
                    true
            );

            byte[] calendarData =
                    calendar
                            .invitation(booking)
                            .getBytes(StandardCharsets.UTF_8);

            customerHelper.addAttachment(
                    "qguard-demo-updated.ics",
                    new ByteArrayResource(calendarData),
                    "text/calendar"
            );

            mail.send(customerMessage);

            // ----------------------------------------------------
            // Demo giver email
            // ----------------------------------------------------

            sendDemoGiverRescheduledNotification(
                    booking,
                    previousStart
            );

        } catch (Exception e) {
            throw new NotificationException();
        }
    }

    // ============================================================
    // NEW BOOKING → DEMO GIVER
    // ============================================================

    private void sendDemoGiverNotification(
            Booking booking
    ) {

        if (properties.demoRecipientEmail() == null
                || properties.demoRecipientEmail().isBlank()) {
            return;
        }

        try {
            MimeMessage message =
                    mail.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            StandardCharsets.UTF_8.name()
                    );

            helper.setFrom(properties.sender());

            helper.setTo(
                    properties.demoRecipientEmail()
            );

            helper.setSubject(
                    "New QGuard Demo Booking — "
                            + booking.getName()
            );

            String schedulerHtml =
                    buildDemoGiverEmail(booking);

            helper.setText(
                    schedulerHtml,
                    true
            );

            mail.send(message);

        } catch (Exception e) {
            throw new NotificationException();
        }
    }

    // ============================================================
    // RESCHEDULE → DEMO GIVER
    // ============================================================

    private void sendDemoGiverRescheduledNotification(
            Booking booking,
            Instant previousStart
    ) {

        if (properties.demoRecipientEmail() == null
                || properties.demoRecipientEmail().isBlank()) {
            return;
        }

        try {
            MimeMessage message =
                    mail.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            StandardCharsets.UTF_8.name()
                    );

            helper.setFrom(properties.sender());

            helper.setTo(
                    properties.demoRecipientEmail()
            );

            helper.setSubject(
                    "QGuard Demo Rescheduled — "
                            + booking.getName()
            );

            String schedulerHtml =
                    buildDemoGiverRescheduledEmail(
                            booking,
                            previousStart
                    );

            helper.setText(
                    schedulerHtml,
                    true
            );

            mail.send(message);

        } catch (Exception e) {
            throw new NotificationException();
        }
    }

    // ============================================================
    // CUSTOMER CONFIRMATION EMAIL
    // ============================================================

    private String buildCustomerConfirmationEmail(
            Booking booking,
            String managementUrl
    ) {

        ZoneId businessZone =
                properties.businessZone();

        ZoneId customerZone =
                ZoneId.of(booking.getTimezone());

        ZonedDateTime businessStart =
                booking.getStartsAt()
                        .atZone(businessZone);

        ZonedDateTime businessEnd =
                booking.getEndsAt()
                        .atZone(businessZone);

        ZonedDateTime customerStart =
                booking.getStartsAt()
                        .atZone(customerZone);

        ZonedDateTime customerEnd =
                booking.getEndsAt()
                        .atZone(customerZone);

        String date =
                businessStart.format(DATE_FORMAT);

        String istTime =
                businessStart.format(TIME_FORMAT)
                        + " – "
                        + businessEnd.format(TIME_FORMAT)
                        + " IST";

        String customerLocalTime =
                customerStart.format(TIME_FORMAT)
                        + " – "
                        + customerEnd.format(TIME_FORMAT);

        String timezone =
                customerZone.getId();

        String googleCalendarUrl =
                buildGoogleCalendarUrl(booking);

        return """
                <!DOCTYPE html>
                <html>
                <body style="margin:0;
                    padding:0;
                    background:#f4f6f8;
                    font-family:Arial,sans-serif;
                    color:#1f2937;">

                <div style="max-width:620px;
                    margin:40px auto;
                    background:#ffffff;
                    border-radius:12px;
                    overflow:hidden;
                    border:1px solid #e5e7eb;">

                    <div style="background:#111827;
                        padding:28px;
                        text-align:center;
                        color:white;">

                        <div style="font-size:24px;
                            font-weight:bold;">
                            QGuard
                        </div>

                        <div style="font-size:13px;
                            margin-top:6px;
                            opacity:0.8;">
                            Quantum Cybersecurity by TrevasQ
                        </div>

                    </div>

                    <div style="padding:32px;">

                        <h2 style="margin-top:0;">
                            Your Demo is Confirmed ✓
                        </h2>

                        <p>
                            Hi %s,
                        </p>

                        <p>
                            Thank you for scheduling a demo of
                            <strong>QGuard by TrevasQ</strong>.
                            Your appointment has been successfully
                            confirmed.
                        </p>

                        <div style="background:#f8fafc;
                            border:1px solid #e5e7eb;
                            border-radius:10px;
                            padding:22px;
                            margin:25px 0;">

                            <div style="font-size:13px;
                                color:#6b7280;">
                                DEMO DATE
                            </div>

                            <div style="font-size:18px;
                                font-weight:bold;
                                margin-top:5px;">
                                %s
                            </div>

                            <div style="font-size:13px;
                                color:#6b7280;
                                margin-top:18px;">
                                DEMO TIME — INDIA STANDARD TIME
                            </div>

                            <div style="font-size:18px;
                                font-weight:bold;
                                margin-top:5px;">
                                %s
                            </div>

                            <div style="font-size:13px;
                                color:#6b7280;
                                margin-top:18px;">
                                YOUR LOCAL TIME
                            </div>

                            <div style="font-size:18px;
                                font-weight:bold;
                                margin-top:5px;">
                                %s
                            </div>

                            <div style="font-size:13px;
                                color:#6b7280;
                                margin-top:18px;">
                                YOUR TIMEZONE
                            </div>

                            <div style="font-size:15px;
                                margin-top:5px;">
                                %s
                            </div>

                            <div style="font-size:13px;
                                color:#6b7280;
                                margin-top:18px;">
                                COMPANY
                            </div>

                            <div style="font-size:15px;
                                margin-top:5px;">
                                %s
                            </div>

                            <div style="font-size:13px;
                                color:#6b7280;
                                margin-top:18px;">
                                JOB TITLE
                            </div>

                            <div style="font-size:15px;
                                margin-top:5px;">
                                %s
                            </div>

                            <div style="font-size:13px;
                                color:#6b7280;
                                margin-top:18px;">
                                DURATION
                            </div>

                            <div style="font-size:15px;
                                margin-top:5px;">
                                30 minutes
                            </div>

                        </div>

                        <h3>
                            Add to your calendar
                        </h3>

                        <p>
                            Save the demo to your calendar so you
                            don't miss the appointment.
                        </p>

                        <div style="margin:25px 0;">

                            <a href="%s"
                               style="display:inline-block;
                               background:#111827;
                               color:white;
                               padding:12px 20px;
                               border-radius:7px;
                               text-decoration:none;
                               font-weight:bold;">
                                Add to Google Calendar
                            </a>

                        </div>

                        <p style="font-size:14px;
                            color:#6b7280;">

                            We have also attached a calendar
                            invitation (.ics) that can be added
                            to Google Calendar, Outlook, Apple
                            Calendar, or another calendar app.

                        </p>

                        <div style="margin:30px 0;
                            text-align:center;">

                            <a href="%s"
                               style="display:inline-block;
                               background:#2563eb;
                               color:white;
                               padding:13px 26px;
                               border-radius:7px;
                               text-decoration:none;
                               font-weight:bold;">
                                Manage My Booking
                            </a>

                        </div>

                        <p style="font-size:14px;
                            color:#6b7280;">

                            You can use the button above to
                            reschedule or cancel your demo.

                        </p>

                        <hr style="border:none;
                            border-top:1px solid #e5e7eb;
                            margin:30px 0;">

                        <p style="font-size:13px;
                            color:#6b7280;
                            text-align:center;">

                            Best regards,<br>
                            <strong>TrevasQ Team</strong><br>
                            QGuard — Quantum Cybersecurity

                        </p>

                    </div>

                </div>

                </body>
                </html>
                """.formatted(
                escapeHtml(booking.getName()),
                date,
                istTime,
                customerLocalTime,
                escapeHtml(timezone),
                escapeHtml(booking.getCompany()),
                escapeHtml(booking.getJobTitle()),
                googleCalendarUrl,
                escapeHtml(managementUrl)
        );
    }

    // ============================================================
    // CUSTOMER RESCHEDULE EMAIL
    // ============================================================

    private String buildCustomerRescheduledEmail(
            Booking booking,
            Instant previousStart,
            String managementUrl
    ) {

        ZoneId businessZone =
                properties.businessZone();

        ZoneId customerZone =
                ZoneId.of(booking.getTimezone());

        // Previous time in business timezone
        ZonedDateTime previousBusinessStart =
                previousStart.atZone(businessZone);

        ZonedDateTime previousBusinessEnd =
                previousStart
                        .plusSeconds(1800)
                        .atZone(businessZone);

        // New time in business timezone
        ZonedDateTime newBusinessStart =
                booking.getStartsAt()
                        .atZone(businessZone);

        ZonedDateTime newBusinessEnd =
                booking.getEndsAt()
                        .atZone(businessZone);

        // Previous time in customer timezone
        ZonedDateTime previousCustomerStart =
                previousStart.atZone(customerZone);

        ZonedDateTime previousCustomerEnd =
                previousStart
                        .plusSeconds(1800)
                        .atZone(customerZone);

        // New time in customer timezone
        ZonedDateTime newCustomerStart =
                booking.getStartsAt()
                        .atZone(customerZone);

        ZonedDateTime newCustomerEnd =
                booking.getEndsAt()
                        .atZone(customerZone);

        String previousIstTime =
                previousBusinessStart.format(TIME_FORMAT)
                        + " – "
                        + previousBusinessEnd.format(TIME_FORMAT)
                        + " IST";

        String newIstTime =
                newBusinessStart.format(TIME_FORMAT)
                        + " – "
                        + newBusinessEnd.format(TIME_FORMAT)
                        + " IST";

        String previousLocalTime =
                previousCustomerStart.format(TIME_FORMAT)
                        + " – "
                        + previousCustomerEnd.format(TIME_FORMAT);

        String newLocalTime =
                newCustomerStart.format(TIME_FORMAT)
                        + " – "
                        + newCustomerEnd.format(TIME_FORMAT);

        String date =
                newBusinessStart.format(DATE_FORMAT);

        String timezone =
                customerZone.getId();

        String googleCalendarUrl =
                buildGoogleCalendarUrl(booking);

        return """
                <!DOCTYPE html>
                <html>
                <body style="margin:0;
                    padding:0;
                    background:#f4f6f8;
                    font-family:Arial,sans-serif;
                    color:#1f2937;">

                <div style="max-width:620px;
                    margin:40px auto;
                    background:#ffffff;
                    border-radius:12px;
                    overflow:hidden;
                    border:1px solid #e5e7eb;">

                    <div style="background:#111827;
                        padding:28px;
                        text-align:center;
                        color:white;">

                        <div style="font-size:24px;
                            font-weight:bold;">
                            QGuard
                        </div>

                        <div style="font-size:13px;
                            margin-top:6px;
                            opacity:0.8;">
                            Quantum Cybersecurity by TrevasQ
                        </div>

                    </div>

                    <div style="padding:32px;">

                        <h2 style="margin-top:0;">
                            Your Demo Has Been Rescheduled ✓
                        </h2>

                        <p>
                            Hi %s,
                        </p>

                        <p>
                            Your <strong>QGuard by TrevasQ</strong>
                            demo has been successfully rescheduled.
                        </p>

                        <div style="background:#fff7ed;
                            border:1px solid #fed7aa;
                            border-radius:10px;
                            padding:22px;
                            margin:25px 0;">

                            <h3 style="margin-top:0;">
                                Previous Schedule
                            </h3>

                            <p>
                                <strong>India Standard Time:</strong><br>
                                %s
                            </p>

                            <p>
                                <strong>Your Local Time:</strong><br>
                                %s
                            </p>

                        </div>

                        <div style="background:#f0fdf4;
                            border:1px solid #bbf7d0;
                            border-radius:10px;
                            padding:22px;
                            margin:25px 0;">

                            <h3 style="margin-top:0;">
                                New Schedule
                            </h3>

                            <p>
                                <strong>Date:</strong><br>
                                %s
                            </p>

                            <p>
                                <strong>India Standard Time:</strong><br>
                                %s
                            </p>

                            <p>
                                <strong>Your Local Time:</strong><br>
                                %s
                            </p>

                            <p>
                                <strong>Your Timezone:</strong><br>
                                %s
                            </p>

                            <p>
                                <strong>Duration:</strong><br>
                                30 minutes
                            </p>

                        </div>

                        <h3>
                            Update your calendar
                        </h3>

                        <p>
                            Please update your calendar with the
                            new appointment time. You can also use
                            the attached calendar invitation.
                        </p>

                        <div style="margin:25px 0;">

                            <a href="%s"
                               style="display:inline-block;
                               background:#111827;
                               color:white;
                               padding:12px 20px;
                               border-radius:7px;
                               text-decoration:none;
                               font-weight:bold;">
                                Add to Google Calendar
                            </a>

                        </div>

                        <div style="margin:30px 0;
                            text-align:center;">

                            <a href="%s"
                               style="display:inline-block;
                               background:#2563eb;
                               color:white;
                               padding:13px 26px;
                               border-radius:7px;
                               text-decoration:none;
                               font-weight:bold;">
                                Manage My Booking
                            </a>

                        </div>

                        <hr style="border:none;
                            border-top:1px solid #e5e7eb;
                            margin:30px 0;">

                        <p style="font-size:13px;
                            color:#6b7280;
                            text-align:center;">

                            Best regards,<br>
                            <strong>TrevasQ Team</strong><br>
                            QGuard — Quantum Cybersecurity

                        </p>

                    </div>

                </div>

                </body>
                </html>
                """.formatted(
                escapeHtml(booking.getName()),
                previousIstTime,
                previousLocalTime,
                date,
                newIstTime,
                newLocalTime,
                escapeHtml(timezone),
                googleCalendarUrl,
                escapeHtml(managementUrl)
        );
    }

    // ============================================================
    // DEMO GIVER — NEW BOOKING
    // ============================================================

    private String buildDemoGiverEmail(
            Booking booking
    ) {

        ZoneId businessZone =
                properties.businessZone();

        ZoneId customerZone =
                ZoneId.of(booking.getTimezone());

        ZonedDateTime businessStart =
                booking.getStartsAt()
                        .atZone(businessZone);

        ZonedDateTime businessEnd =
                booking.getEndsAt()
                        .atZone(businessZone);

        ZonedDateTime customerStart =
                booking.getStartsAt()
                        .atZone(customerZone);

        ZonedDateTime customerEnd =
                booking.getEndsAt()
                        .atZone(customerZone);

        String date =
                businessStart.format(DATE_FORMAT);

        String istTime =
                businessStart.format(TIME_FORMAT)
                        + " – "
                        + businessEnd.format(TIME_FORMAT)
                        + " IST";

        String customerLocalTime =
                customerStart.format(TIME_FORMAT)
                        + " – "
                        + customerEnd.format(TIME_FORMAT);

        String customerTimezone =
                customerZone.getId();

        return """
                <!DOCTYPE html>
                <html>
                <body style="margin:0;
                    padding:0;
                    background:#f4f6f8;
                    font-family:Arial,sans-serif;
                    color:#1f2937;">

                <div style="max-width:620px;
                    margin:40px auto;
                    background:#ffffff;
                    border-radius:12px;
                    overflow:hidden;
                    border:1px solid #e5e7eb;">

                    <div style="background:#111827;
                        padding:28px;
                        text-align:center;
                        color:white;">

                        <div style="font-size:24px;
                            font-weight:bold;">
                            QGuard
                        </div>

                        <div style="font-size:13px;
                            margin-top:6px;
                            opacity:0.8;">
                            Demo Scheduler Notification
                        </div>

                    </div>

                    <div style="padding:32px;">

                        <h2 style="margin-top:0;">
                            New Demo Booking 🔔
                        </h2>

                        <p>
                            A new QGuard demo has been scheduled.
                        </p>

                        <div style="background:#f8fafc;
                            border:1px solid #e5e7eb;
                            border-radius:10px;
                            padding:22px;
                            margin:25px 0;">

                            <h3 style="margin-top:0;">
                                Demo Schedule
                            </h3>

                            <p>
                                <strong>Date:</strong><br>
                                %s
                            </p>

                            <p>
                                <strong>Time — India Standard Time:</strong><br>
                                %s
                            </p>

                            <p>
                                <strong>Customer local time:</strong><br>
                                %s
                            </p>

                            <p>
                                <strong>Customer timezone:</strong><br>
                                %s
                            </p>

                            <p>
                                <strong>Duration:</strong><br>
                                30 minutes
                            </p>

                        </div>

                        <div style="background:#ffffff;
                            border:1px solid #e5e7eb;
                            border-radius:10px;
                            padding:22px;
                            margin:25px 0;">

                            <h3 style="margin-top:0;">
                                Customer Details
                            </h3>

                            <p>
                                <strong>Name:</strong><br>
                                %s
                            </p>

                            <p>
                                <strong>Email:</strong><br>
                                %s
                            </p>

                            <p>
                                <strong>Company:</strong><br>
                                %s
                            </p>

                            <p>
                                <strong>Job Title:</strong><br>
                                %s
                            </p>

                            <p>
                                <strong>Phone:</strong><br>
                                %s
                            </p>

                        </div>

                        <p style="font-size:14px;
                            color:#6b7280;">

                            Please make sure you are available
                            for the scheduled demo.

                        </p>

                        <hr style="border:none;
                            border-top:1px solid #e5e7eb;
                            margin:30px 0;">

                        <p style="font-size:13px;
                            color:#6b7280;
                            text-align:center;">

                            QGuard Demo Scheduler<br>
                            TrevasQ

                        </p>

                    </div>

                </div>

                </body>
                </html>
                """.formatted(
                date,
                istTime,
                customerLocalTime,
                escapeHtml(customerTimezone),
                escapeHtml(booking.getName()),
                escapeHtml(booking.getEmail()),
                escapeHtml(booking.getCompany()),
                escapeHtml(booking.getJobTitle()),
                escapeHtml(
                        booking.getPhone() == null
                                ? "Not provided"
                                : booking.getPhone()
                )
        );
    }

    // ============================================================
    // DEMO GIVER — RESCHEDULE
    // ============================================================

    private String buildDemoGiverRescheduledEmail(
            Booking booking,
            Instant previousStart
    ) {

        ZoneId businessZone =
                properties.businessZone();

        ZoneId customerZone =
                ZoneId.of(booking.getTimezone());

        ZonedDateTime previousStartBusiness =
                previousStart.atZone(businessZone);

        ZonedDateTime previousEndBusiness =
                previousStart
                        .plusSeconds(1800)
                        .atZone(businessZone);

        ZonedDateTime newStartBusiness =
                booking.getStartsAt()
                        .atZone(businessZone);

        ZonedDateTime newEndBusiness =
                booking.getEndsAt()
                        .atZone(businessZone);

        ZonedDateTime newStartCustomer =
                booking.getStartsAt()
                        .atZone(customerZone);

        ZonedDateTime newEndCustomer =
                booking.getEndsAt()
                        .atZone(customerZone);

        String previousTime =
                previousStartBusiness.format(TIME_FORMAT)
                        + " – "
                        + previousEndBusiness.format(TIME_FORMAT)
                        + " IST";

        String newTime =
                newStartBusiness.format(TIME_FORMAT)
                        + " – "
                        + newEndBusiness.format(TIME_FORMAT)
                        + " IST";

        String newCustomerTime =
                newStartCustomer.format(TIME_FORMAT)
                        + " – "
                        + newEndCustomer.format(TIME_FORMAT);

        String date =
                newStartBusiness.format(DATE_FORMAT);

        return """
                <!DOCTYPE html>
                <html>
                <body style="margin:0;
                    padding:0;
                    background:#f4f6f8;
                    font-family:Arial,sans-serif;
                    color:#1f2937;">

                <div style="max-width:620px;
                    margin:40px auto;
                    background:#ffffff;
                    border-radius:12px;
                    overflow:hidden;
                    border:1px solid #e5e7eb;">

                    <div style="background:#111827;
                        padding:28px;
                        text-align:center;
                        color:white;">

                        <div style="font-size:24px;
                            font-weight:bold;">
                            QGuard
                        </div>

                        <div style="font-size:13px;
                            margin-top:6px;
                            opacity:0.8;">
                            Demo Scheduler Notification
                        </div>

                    </div>

                    <div style="padding:32px;">

                        <h2 style="margin-top:0;">
                            Demo Rescheduled 🔄
                        </h2>

                        <p>
                            A QGuard demo booking has been
                            rescheduled.
                        </p>

                        <div style="background:#fff7ed;
                            border:1px solid #fed7aa;
                            border-radius:10px;
                            padding:22px;
                            margin:25px 0;">

                            <h3 style="margin-top:0;">
                                Previous Schedule
                            </h3>

                            <p>
                                <strong>Time — IST:</strong><br>
                                %s
                            </p>

                        </div>

                        <div style="background:#f0fdf4;
                            border:1px solid #bbf7d0;
                            border-radius:10px;
                            padding:22px;
                            margin:25px 0;">

                            <h3 style="margin-top:0;">
                                New Schedule
                            </h3>

                            <p>
                                <strong>Date:</strong><br>
                                %s
                            </p>

                            <p>
                                <strong>Time — IST:</strong><br>
                                %s
                            </p>

                            <p>
                                <strong>Customer local time:</strong><br>
                                %s
                            </p>

                            <p>
                                <strong>Customer timezone:</strong><br>
                                %s
                            </p>

                            <p>
                                <strong>Duration:</strong><br>
                                30 minutes
                            </p>

                        </div>

                        <div style="background:#ffffff;
                            border:1px solid #e5e7eb;
                            border-radius:10px;
                            padding:22px;
                            margin:25px 0;">

                            <h3 style="margin-top:0;">
                                Customer Details
                            </h3>

                            <p>
                                <strong>Name:</strong><br>
                                %s
                            </p>

                            <p>
                                <strong>Email:</strong><br>
                                %s
                            </p>

                            <p>
                                <strong>Company:</strong><br>
                                %s
                            </p>

                            <p>
                                <strong>Job Title:</strong><br>
                                %s
                            </p>

                            <p>
                                <strong>Phone:</strong><br>
                                %s
                            </p>

                        </div>

                        <hr style="border:none;
                            border-top:1px solid #e5e7eb;
                            margin:30px 0;">

                        <p style="font-size:13px;
                            color:#6b7280;
                            text-align:center;">

                            QGuard Demo Scheduler<br>
                            TrevasQ

                        </p>

                    </div>

                </div>

                </body>
                </html>
                """.formatted(
                previousTime,
                date,
                newTime,
                newCustomerTime,
                escapeHtml(
                        customerZone.getId()
                ),
                escapeHtml(booking.getName()),
                escapeHtml(booking.getEmail()),
                escapeHtml(booking.getCompany()),
                escapeHtml(booking.getJobTitle()),
                escapeHtml(
                        booking.getPhone() == null
                                ? "Not provided"
                                : booking.getPhone()
                )
        );
    }

    // ============================================================
    // GOOGLE CALENDAR
    // ============================================================

    private String buildGoogleCalendarUrl(
            Booking booking
    ) {

        String start =
                booking.getStartsAt()
                        .toString()
                        .replace("-", "")
                        .replace(":", "")
                        .replace(".", "");

        String end =
                booking.getEndsAt()
                        .toString()
                        .replace("-", "")
                        .replace(":", "")
                        .replace(".", "");

        String title =
                "QGuard Demo — TrevasQ";

        String details =
                "QGuard quantum cybersecurity demo with TrevasQ.";

        return "https://calendar.google.com/calendar/render"
                + "?action=TEMPLATE"
                + "&text="
                + urlEncode(title)
                + "&dates="
                + start
                + "/"
                + end
                + "&details="
                + urlEncode(details);
    }

    // ============================================================
    // URL ENCODING
    // ============================================================

    private String urlEncode(String value) {

        return URLEncoder
                .encode(
                        value,
                        StandardCharsets.UTF_8
                )
                .replace("+", "%20");
    }

    // ============================================================
    // HTML ESCAPING
    // ============================================================

    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // ============================================================
    // EXCEPTION
    // ============================================================

    public static class NotificationException
            extends RuntimeException {
    }
}