package com.trevasq.qguard.api;
import jakarta.validation.constraints.*; import java.time.*; import java.util.*;
public final class ApiDtos { private ApiDtos(){} public record SlotResponse(Instant startsAt, Instant endsAt, String display){} public record AvailabilityResponse(LocalDate date,String timezone,List<SlotResponse> slots){}
 public record CreateBookingRequest(@NotBlank @Size(max=120) String name,@NotBlank @Email @Size(max=254) String email,@NotBlank @Size(max=160) String company,@NotBlank @Size(max=160) String jobTitle,@Size(max=40) String phone,@NotNull Instant startsAt,@NotBlank String timezone){}
 public record RescheduleRequest(@NotNull Instant startsAt,@NotBlank String timezone){} public record BookingResponse(UUID id,String name,String email,String company,String jobTitle,String phone,Instant startsAt,Instant endsAt,String status,String timezone){} public record CreatedBookingResponse(BookingResponse booking,String managementUrl){} public record ErrorResponse(String code,String message){}
}
