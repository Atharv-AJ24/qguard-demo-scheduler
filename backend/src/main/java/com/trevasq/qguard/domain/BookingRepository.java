package com.trevasq.qguard.domain;
import java.time.Instant; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface BookingRepository extends JpaRepository<Booking,UUID> { boolean existsByStatusAndStartsAt(BookingStatus status, Instant startsAt); List<Booking> findByStatusAndStartsAtGreaterThanEqualAndStartsAtLessThan(BookingStatus status, Instant from, Instant to); Optional<Booking> findByManagementTokenHash(String hash); }
