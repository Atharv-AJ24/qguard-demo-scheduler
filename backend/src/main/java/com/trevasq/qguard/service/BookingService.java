package com.trevasq.qguard.service;

import com.trevasq.qguard.api.ApiDtos.*;
import com.trevasq.qguard.config.AppProperties;
import com.trevasq.qguard.domain.*;

import jakarta.transaction.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    private final BookingRepository repo;
    private final SchedulingService schedule;
    private final TokenService tokens;
    private final NotificationService notify;
    private final AppProperties properties;

    public BookingService(
            BookingRepository repo,
            SchedulingService schedule,
            TokenService tokens,
            NotificationService notify,
            AppProperties properties
    ) {
        this.repo = repo;
        this.schedule = schedule;
        this.tokens = tokens;
        this.notify = notify;
        this.properties = properties;
    }

    @Transactional
    public CreatedBookingResponse create(CreateBookingRequest request) {

        schedule.assertBookable(request.startsAt());

        String token = tokens.create();

        Booking booking = new Booking(
                UUID.randomUUID(),
                request.name().trim(),
                request.email().trim().toLowerCase(),
                request.company().trim(),
                request.jobTitle().trim(),
                blankToNull(request.phone()),
                zone(request.timezone()).getId(),
                request.startsAt(),
                request.startsAt().plusSeconds(1800),
                tokens.hash(token)
        );

        try {
            repo.saveAndFlush(booking);
        } catch (DataIntegrityViolationException e) {
            throw new SchedulingService.SlotTakenException();
        }

        String managementUrl = managementUrl(token);

        notify.confirmation(
                booking,
                managementUrl
        );

        return new CreatedBookingResponse(
                dto(
                        booking,
                        zone(request.timezone())
                ),
                managementUrl
        );
    }

    @Transactional
    public BookingResponse reschedule(
            String token,
            RescheduleRequest request
    ) {

        Booking booking = managed(token);

        // Remember the old booking time before changing it.
        Instant previousStart = booking.getStartsAt();

        // Make sure the new slot is still available.
        schedule.assertBookable(request.startsAt());

        try {
            booking.reschedule(request.startsAt());
            repo.saveAndFlush(booking);
        } catch (DataIntegrityViolationException e) {
            throw new SchedulingService.SlotTakenException();
        }

        /*
         * Temporary:
         * We will replace this with a dedicated reschedule
         * notification in the next step.
         */
        notify.confirmation(
                booking,
                managementUrl(token)
        );

        return dto(
                booking,
                zone(request.timezone())
        );
    }

    @Transactional
    public void cancel(String token) {

        Booking booking = managed(token);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return;
        }

        booking.cancel();

        repo.save(booking);
    }

    @Transactional
    public BookingResponse get(String token) {

        return dto(
                managed(token),
                properties.businessZone()
        );
    }

    private Booking managed(String token) {

        return repo.findByManagementTokenHash(
                tokens.hash(token)
        ).orElseThrow(
                UnknownManagementTokenException::new
        );
    }

    private String managementUrl(String token) {

        return properties.publicBaseUrl()
                + "/manage/"
                + token;
    }

    private ZoneId zone(String timezone) {

        try {
            return ZoneId.of(timezone);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid IANA timezone"
            );
        }
    }

    private BookingResponse dto(
            Booking booking,
            ZoneId zone
    ) {

        return new BookingResponse(
                booking.getId(),
                booking.getName(),
                booking.getEmail(),
                booking.getCompany(),
                booking.getJobTitle(),
                booking.getPhone(),
                booking.getStartsAt(),
                booking.getEndsAt(),
                booking.getStatus().name(),
                zone.getId()
        );
    }

    private String blankToNull(String value) {

        return value == null || value.isBlank()
                ? null
                : value.trim();
    }

    public static class UnknownManagementTokenException
            extends RuntimeException {
    }
}