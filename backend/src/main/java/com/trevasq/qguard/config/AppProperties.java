package com.trevasq.qguard.config;

import java.time.LocalTime;
import java.time.ZoneId;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="app")
public record AppProperties(
        String publicBaseUrl,
        ZoneId businessZone,
        LocalTime workingHoursStart,
        LocalTime workingHoursEnd,
        String sender,
        String demoRecipientEmail
) {}
