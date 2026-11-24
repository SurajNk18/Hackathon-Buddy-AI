package com.hackathonbuddy.util;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class HackathonDateUtils {

    private static final Pattern YEAR_PATTERN = Pattern.compile("\\b(20\\d{2})\\b");
    private static final Pattern MONTH_NAME_PATTERN = Pattern.compile("(?i)\\b(Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?)\\b");

    private static final DateTimeFormatter ISO_WITH_SPACE = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd[ ]HH:mm:ss")
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .appendOffset("+HH:MM", "Z")
            .toFormatter(Locale.ENGLISH);

    /**
     * Determines if a hackathon has finished (date or deadline is in the past, or status indicates closed/ended).
     *
     * @param dateOrDeadline date string, deadline string, or range
     * @param status         hackathon status string (e.g., "Open", "Closed", "Ended")
     * @return true if hackathon has finished / expired; false if still ongoing or upcoming
     */
    public static boolean isFinished(String dateOrDeadline, String status) {
        // 1. Explicit status check
        if (status != null && !status.trim().isEmpty()) {
            String s = status.trim().toLowerCase();
            if (s.contains("closed") || s.contains("ended") || s.contains("finished") ||
                s.contains("expired") || s.contains("past") || s.equals("inactive")) {
                return true;
            }
        }

        if (dateOrDeadline == null || dateOrDeadline.trim().isEmpty()) {
            return false;
        }

        String cleaned = dateOrDeadline.trim();

        // Check for text clues like "Ended", "Closed"
        String lowerCleaned = cleaned.toLowerCase();
        if (lowerCleaned.contains("ended") || lowerCleaned.contains("closed") || lowerCleaned.contains("expired")) {
            return true;
        }

        LocalDate today = LocalDate.now();

        // 2. Try Devpost date range: e.g. "Aug 07 - Oct 20, 2026" or "Oct 01 - 19, 2026"
        if (cleaned.contains("-")) {
            LocalDate parsedEndDate = parseEndDateFromRange(cleaned);
            if (parsedEndDate != null) {
                return parsedEndDate.isBefore(today);
            }
        }

        // 3. Try parsing ISO Offset / Zoned DateTime (Unstop: "2026-10-21T17:00:00+05:30")
        try {
            OffsetDateTime odt = OffsetDateTime.parse(cleaned);
            return odt.toLocalDate().isBefore(today);
        } catch (Exception ignored) {
        }

        // 4. Try parsing HackerEarth format: "2026-10-18 18:29:00+00:00"
        try {
            OffsetDateTime odt = OffsetDateTime.parse(cleaned, ISO_WITH_SPACE);
            return odt.toLocalDate().isBefore(today);
        } catch (Exception ignored) {
        }

        // 5. Try parsing ISO Instant: "2026-11-01T07:30:00Z"
        try {
            Instant instant = Instant.parse(cleaned);
            LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
            return date.isBefore(today);
        } catch (Exception ignored) {
        }

        // 6. Try parsing standard LocalDate: "yyyy-MM-dd"
        try {
            LocalDate date = LocalDate.parse(cleaned);
            return date.isBefore(today);
        } catch (Exception ignored) {
        }

        // 7. Try parsing "dd MMM yyyy" (e.g. "18 Aug 2026", "21 Oct 2026")
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
            LocalDate date = LocalDate.parse(cleaned, dtf);
            return date.isBefore(today);
        } catch (Exception ignored) {
        }

        // 8. Try parsing "MMM dd, yyyy" or "MMM d, yyyy" (e.g. "Oct 20, 2026")
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
            LocalDate date = LocalDate.parse(cleaned, dtf);
            return date.isBefore(today);
        } catch (Exception ignored) {
        }

        // If format is unrecognized, do not prematurely remove
        return false;
    }

    /**
     * Parses the end date from a range like "Aug 07 - Oct 20, 2026" or "Oct 01 - 19, 2026".
     */
    public static LocalDate parseEndDateFromRange(String rangeStr) {
        try {
            String[] parts = rangeStr.split("-");
            if (parts.length < 2) return null;

            String startPart = parts[0].trim();
            String endPart = parts[parts.length - 1].trim();

            // Find year (e.g. 2026) in the endPart, or startPart
            String year = null;
            Matcher yearMatcher = YEAR_PATTERN.matcher(endPart);
            if (yearMatcher.find()) {
                year = yearMatcher.group(1);
            } else {
                yearMatcher = YEAR_PATTERN.matcher(startPart);
                if (yearMatcher.find()) {
                    year = yearMatcher.group(1);
                } else {
                    year = String.valueOf(LocalDate.now().getYear());
                }
            }

            // Find month in endPart
            String month = null;
            Matcher monthMatcher = MONTH_NAME_PATTERN.matcher(endPart);
            if (monthMatcher.find()) {
                month = monthMatcher.group(1);
            } else {
                // Borrow month from startPart (e.g. "Oct 01 - 19, 2026")
                Matcher startMonthMatcher = MONTH_NAME_PATTERN.matcher(startPart);
                if (startMonthMatcher.find()) {
                    month = startMonthMatcher.group(1);
                }
            }

            if (month == null) return null;

            // Extract day number from endPart
            Pattern dayPattern = Pattern.compile("(\\d{1,2})");
            Matcher dayMatcher = dayPattern.matcher(endPart);
            String day = null;
            while (dayMatcher.find()) {
                String candidate = dayMatcher.group(1);
                // Exclude the year if 2-digit match happens to be 20
                if (!candidate.equals("20") || !endPart.contains("202")) {
                    day = candidate;
                } else if (endPart.matches(".*\\b" + candidate + "\\b.*") && !endPart.contains("2026")) {
                    day = candidate;
                }
            }

            // If day wasn't cleanly found from end, look for first digits before comma or space
            if (day == null) {
                Matcher fallbackDay = Pattern.compile("\\b(\\d{1,2})(?:st|nd|rd|th)?\\b").matcher(endPart);
                if (fallbackDay.find()) {
                    day = fallbackDay.group(1);
                }
            }

            if (day == null) return null;

            String reconstructed = month.substring(0, 3) + " " + (day.length() == 1 ? "0" + day : day) + " " + year;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH);
            return LocalDate.parse(reconstructed, formatter);

        } catch (Exception e) {
            log.debug("Could not parse end date from range '{}': {}", rangeStr, e.getMessage());
            return null;
        }
    }

    /**
     * Formats a deadline or raw date string into a user-friendly display date (e.g., "21 Oct 2026").
     */
    public static String formatDisplayDate(String dateOrDeadline) {
        if (dateOrDeadline == null || dateOrDeadline.trim().isEmpty()) {
            return "Upcoming";
        }
        String cleaned = dateOrDeadline.trim();

        // If it's a date range with month letters (e.g. "Aug 07 - Oct 20, 2026")
        if (cleaned.contains(" - ") && MONTH_NAME_PATTERN.matcher(cleaned).find()) {
            return cleaned;
        }

        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

        // Try ISO Offset DateTime
        try {
            OffsetDateTime odt = OffsetDateTime.parse(cleaned);
            return odt.format(outputFormat);
        } catch (Exception ignored) {}

        // Try HackerEarth format
        try {
            OffsetDateTime odt = OffsetDateTime.parse(cleaned, ISO_WITH_SPACE);
            return odt.format(outputFormat);
        } catch (Exception ignored) {}

        // Try ISO Instant
        try {
            Instant instant = Instant.parse(cleaned);
            return instant.atZone(ZoneId.systemDefault()).format(outputFormat);
        } catch (Exception ignored) {}

        // Try LocalDate
        try {
            LocalDate date = LocalDate.parse(cleaned);
            return date.format(outputFormat);
        } catch (Exception ignored) {}

        return cleaned;
    }
}
