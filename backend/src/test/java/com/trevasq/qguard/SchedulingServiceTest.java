package com.trevasq.qguard;

import com.trevasq.qguard.config.AppProperties;
import com.trevasq.qguard.domain.BookingRepository;
import com.trevasq.qguard.service.SchedulingService;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class SchedulingServiceTest {

    @Test
    void rejects_non_half_hour_slot() {
        var repo = mock(BookingRepository.class);

        var service = new SchedulingService(
                repo,
                new AppProperties(
                        "",
                        ZoneOffset.UTC,
                        LocalTime.of(9, 0),
                        LocalTime.of(17, 0),
                        "",
                        ""
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.assertBookable(
                        Instant.parse("2030-01-01T09:15:00Z")
                )
        );
    }

    @Test
    void timezone_conversion_preserves_instant_across_daylight_saving_time() {
        Instant slot = ZonedDateTime.of(
                2030,
                5,
                1,
                9,
                0,
                0,
                0,
                ZoneId.of("Asia/Kolkata")
        ).toInstant();

        ZonedDateTime londonDisplay =
                slot.atZone(ZoneId.of("Europe/London"));

        assertEquals(
                Instant.parse("2030-05-01T03:30:00Z"),
                slot
        );

        assertEquals(
                9,
                ZonedDateTime.ofInstant(
                        slot,
                        ZoneId.of("Asia/Kolkata")
                ).getHour()
        );

        assertEquals(
                ZoneOffset.ofHours(1),
                londonDisplay.getOffset()
        );

        assertEquals(
                LocalTime.of(4, 30),
                londonDisplay.toLocalTime()
        );
    }
}
