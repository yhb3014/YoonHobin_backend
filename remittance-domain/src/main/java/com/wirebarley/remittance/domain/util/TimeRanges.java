package com.wirebarley.remittance.domain.util;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class TimeRanges {

    private TimeRanges() {
    };

    public static Instant[] todayRange(Clock clock) {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(clock.withZone(zone));
        Instant from = today.atStartOfDay(zone).toInstant();
        Instant to = today.plusDays(1).atStartOfDay(zone).toInstant();
        return new Instant[] { from, to };
    }
}
