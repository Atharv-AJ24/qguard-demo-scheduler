package com.trevasq.qguard.api;

import com.trevasq.qguard.api.ApiDtos.AvailabilityResponse;
import com.trevasq.qguard.api.ApiDtos.BookingResponse;
import com.trevasq.qguard.api.ApiDtos.CreateBookingRequest;
import com.trevasq.qguard.api.ApiDtos.CreatedBookingResponse;
import com.trevasq.qguard.api.ApiDtos.RescheduleRequest;
import com.trevasq.qguard.api.ApiDtos.SlotResponse;
import com.trevasq.qguard.service.BookingService;
import com.trevasq.qguard.service.SchedulingService;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class SchedulingController {

    private final SchedulingService scheduling;
    private final BookingService bookings;

    public SchedulingController(
            SchedulingService scheduling,
            BookingService bookings
    ) {
        this.scheduling = scheduling;
        this.bookings = bookings;
    }

    @GetMapping("/availability")
    public AvailabilityResponse availability(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @RequestParam
            String timezone
    ) {
        ZoneId zone = zone(timezone);

        List<SlotResponse> slots =
                scheduling.available(date, zone);

        return new AvailabilityResponse(
                date,
                zone.getId(),
                slots
        );
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedBookingResponse create(
            @Valid @RequestBody CreateBookingRequest request
    ) {
        return bookings.create(request);
    }

    @GetMapping("/bookings/manage/{token}")
    public BookingResponse get(
            @PathVariable String token
    ) {
        return bookings.get(token);
    }

    @PostMapping("/bookings/manage/{token}/reschedule")
    public BookingResponse reschedule(
            @PathVariable String token,
            @Valid @RequestBody RescheduleRequest request
    ) {
        return bookings.reschedule(token, request);
    }

    @PostMapping("/bookings/manage/{token}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(
            @PathVariable String token
    ) {
        bookings.cancel(token);
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
}