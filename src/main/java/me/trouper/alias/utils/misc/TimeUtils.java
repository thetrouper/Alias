package me.trouper.alias.utils.misc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    public static String formatTime(long millis) {
        Instant instant = Instant.ofEpochMilli(millis);
        return DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("UTC")).format(instant);
    }

    public static long deserializeTime(String rfcString) {
        LocalDateTime dateTime = LocalDateTime.parse(rfcString, DateTimeFormatter.RFC_1123_DATE_TIME);
        return dateTime.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
    }
}

