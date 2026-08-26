package com.trevasq.qguard.config;
import java.time.*; import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix="app") public record AppProperties(String publicBaseUrl, ZoneId businessZone, LocalTime workingHoursStart, LocalTime workingHoursEnd, String sender) {}
