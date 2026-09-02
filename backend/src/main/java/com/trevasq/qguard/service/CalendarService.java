package com.trevasq.qguard.service;

import com.trevasq.qguard.domain.Booking;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

@Service
public class CalendarService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter
                    .ofPattern("yyyyMMdd'T'HHmmss'Z'")
                    .withZone(ZoneOffset.UTC);

    public String invitation(Booking booking) {
        return "BEGIN:VCALENDAR\r\n"
                + "VERSION:2.0\r\n"
                + "PRODID:-//TrevasQ//QGuard//EN\r\n"
                + "BEGIN:VEVENT\r\n"
                + "UID:" + booking.getId() + "@qguard\r\n"
                + "DTSTAMP:" + FORMATTER.format(Instant.now()) + "\r\n"
                + "DTSTART:" + FORMATTER.format(booking.getStartsAt()) + "\r\n"
                + "DTEND:" + FORMATTER.format(booking.getEndsAt()) + "\r\n"
                + "SUMMARY:QGuard Demo\r\n"
                + "DESCRIPTION:Your QGuard quantum cybersecurity demo.\r\n"
                + "END:VEVENT\r\n"
                + "END:VCALENDAR\r\n";
    }
}
