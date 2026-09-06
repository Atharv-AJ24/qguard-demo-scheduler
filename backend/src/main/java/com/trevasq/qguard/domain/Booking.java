package com.trevasq.qguard.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String company;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    private String phone;

    @Column(nullable = false)
    private String timezone;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "slot_key", unique = true)
    private String slotKey;

    @Column(name = "management_token_hash", nullable = false, unique = true)
    private String managementTokenHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Booking() {
    }

    public Booking(
            UUID id,
            String name,
            String email,
            String company,
            String jobTitle,
            String phone,
            String timezone,
            Instant startsAt,
            Instant endsAt,
            String tokenHash
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.company = company;
        this.jobTitle = jobTitle;
        this.phone = phone;
        this.timezone = timezone;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.managementTokenHash = tokenHash;
        this.status = BookingStatus.CONFIRMED;
        this.slotKey = startsAt.toString();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void reschedule(Instant start) {
        this.startsAt = start;
        this.endsAt = start.plusSeconds(1800);
        this.slotKey = start.toString();
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
        this.slotKey = null;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCompany() {
        return company;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getPhone() {
        return phone;
    }

    public String getTimezone() {
        return timezone;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public String getManagementTokenHash() {
        return managementTokenHash;
    }
}