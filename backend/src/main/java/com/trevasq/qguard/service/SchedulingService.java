package com.trevasq.qguard.service;

import com.trevasq.qguard.api.ApiDtos;
import com.trevasq.qguard.config.AppProperties;
import com.trevasq.qguard.domain.Booking;
import com.trevasq.qguard.domain.BookingRepository;
import com.trevasq.qguard.domain.BookingStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class SchedulingService {

    private final BookingRepository repo;
    private final AppProperties properties;

    public SchedulingService(
            BookingRepository repo,
            AppProperties properties
    ) {
        this.repo = repo;
        this.properties = properties;
    }

    public List<ApiDtos.SlotResponse> available(
            LocalDate date,
            ZoneId viewerZone
    ) {
        ZoneId businessZone = properties.businessZone();

        Instant from = date
                .atStartOfDay(businessZone)
                .toInstant();

        Instant to = date
                .plusDays(1)
                .atStartOfDay(businessZone)
                .toInstant();

        // Get all confirmed bookings for the selected date
        List<Booking> bookings =
                repo.findByStatusAndStartsAtGreaterThanEqualAndStartsAtLessThan(
                        BookingStatus.CONFIRMED,
                        from,
                        to
                );

        // Map each booked slot to its Booking
        Map<Instant, Booking> bookedSlots = new HashMap<>();

        bookings.forEach(booking ->
                bookedSlots.put(
                        booking.getStartsAt(),
                        booking
                )
        );

        List<ApiDtos.SlotResponse> slots = new ArrayList<>();

        // Generate 30-minute slots
        for (
                LocalTime time = properties.workingHoursStart();
                !time.plusMinutes(30)
                        .isAfter(properties.workingHoursEnd());
                time = time.plusMinutes(30)
        ) {

            Instant slot = ZonedDateTime
                    .of(date, time, businessZone)
                    .toInstant();

            // Don't show slots that have already passed
            if (slot.isAfter(Instant.now())) {

                Booking booking = bookedSlots.get(slot);

                boolean isAvailable = booking == null;

                String bookedBy = null;
                String bookedCompany = null;

                // If slot is already booked, expose booking information
                if (booking != null) {
                    bookedBy = booking.getName();
                    bookedCompany = booking.getCompany();
                }

                slots.add(
                        new ApiDtos.SlotResponse(
                                slot,
                                slot.plusSeconds(1800),
                                slot.atZone(viewerZone)
                                        .toLocalTime()
                                        .toString(),
                                isAvailable,
                                bookedBy,
                                bookedCompany
                        )
                );
            }
        }

        return slots;
    }

    public void assertBookable(Instant start) {

        // Booking must start on a 30-minute boundary
        if (start.getEpochSecond() % 1800 != 0) {
            throw new IllegalArgumentException(
                    "A demo must start on a 30-minute boundary"
            );
        }

        ZoneId businessZone = properties.businessZone();

        LocalTime time = start
                .atZone(businessZone)
                .toLocalTime();

        boolean outsideWorkingHours =
                time.isBefore(properties.workingHoursStart())
                        || time.plusMinutes(30)
                        .isAfter(properties.workingHoursEnd());

        boolean isInThePast =
                start.isBefore(Instant.now());

        if (outsideWorkingHours || isInThePast) {
            throw new IllegalArgumentException(
                    "The selected slot is outside available working hours"
            );
        }

        // Prevent booking an already confirmed slot
        if (repo.existsByStatusAndStartsAt(
                BookingStatus.CONFIRMED,
                start
        )) {
            throw new SlotTakenException();
        }
    }

    public static class SlotTakenException
            extends RuntimeException {
    }
}