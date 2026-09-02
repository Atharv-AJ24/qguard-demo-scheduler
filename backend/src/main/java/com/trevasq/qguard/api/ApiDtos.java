package com.trevasq.qguard.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class ApiDtos {

    private ApiDtos() {
    }

    public record SlotResponse(
        Instant startsAt,
        Instant endsAt,
        String display,
        boolean available,
        String bookedBy,
        String bookedCompany
) {
}

    public record AvailabilityResponse(
            LocalDate date,
            String timezone,
            List<SlotResponse> slots
    ) {
    }

    public record CreateBookingRequest(
            @NotBlank
            @Size(max = 120)
            String name,

            @NotBlank
            @Email
            @Size(max = 254)
            String email,

            @NotBlank
            @Size(max = 160)
            String company,

            @NotBlank
            @Size(max = 160)
            String jobTitle,

            @Size(max = 40)
            String phone,

            @NotNull
            Instant startsAt,

            @NotBlank
            String timezone
    ) {
    }

    public record RescheduleRequest(
            @NotNull
            Instant startsAt,

            @NotBlank
            String timezone
    ) {
    }

    public record BookingResponse(
            UUID id,
            String name,
            String email,
            String company,
            String jobTitle,
            String phone,
            Instant startsAt,
            Instant endsAt,
            String status,
            String timezone
    ) {
    }

    public record CreatedBookingResponse(
            BookingResponse booking,
            String managementUrl
    ) {
    }

    public record ErrorResponse(
            String code,
            String message
    ) {
    }
}
