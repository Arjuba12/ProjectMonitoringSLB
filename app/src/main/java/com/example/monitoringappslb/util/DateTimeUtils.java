package com.example.monitoringappslb.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateTimeUtils {
    private static final TimeZone WIB = TimeZone.getTimeZone("Asia/Jakarta");
    private static final Locale ID = new Locale("id", "ID");

    private DateTimeUtils() {}

    public static String formatDate(String value) {
        Date date = parse(value);
        if (date == null) return fallbackDate(value);
        SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy", ID);
        output.setTimeZone(WIB);
        return output.format(date);
    }

    public static String formatDateTime(String value) {
        Date date = parse(value);
        if (date == null) return fallbackDate(value);
        SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", ID);
        output.setTimeZone(WIB);
        return output.format(date);
    }

    public static String formatTime(String value) {
        Date date = parse(value);
        if (date == null) {
            String clean = value == null ? "" : value.trim().replace("T", " ");
            return clean.length() >= 16 ? clean.substring(11, 16) + " WIB" : clean;
        }
        SimpleDateFormat output = new SimpleDateFormat("HH:mm 'WIB'", ID);
        output.setTimeZone(WIB);
        return output.format(date);
    }

    public static String dateKey(String value) {
        if (value == null || value.trim().isEmpty()) return "";
        String clean = value.trim().replace("T", " ");
        return clean.length() >= 10 ? clean.substring(0, 10) : clean;
    }

    private static Date parse(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        String clean = value.trim();
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat input = new SimpleDateFormat(pattern, Locale.US);
                input.setTimeZone(pattern.contains("'Z'") ? TimeZone.getTimeZone("UTC") : WIB);
                return input.parse(clean);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static String fallbackDate(String value) {
        String key = dateKey(value);
        if (key.isEmpty()) return "-";
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            input.setTimeZone(WIB);
            SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy", ID);
            output.setTimeZone(WIB);
            return output.format(input.parse(key));
        } catch (Exception ignored) {
            return key;
        }
    }
}
