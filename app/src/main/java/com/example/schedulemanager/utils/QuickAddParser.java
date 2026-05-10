package com.example.schedulemanager.utils;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuickAddParser {

    public static class Result {
        public String title;
        public Calendar calendar;

        public Result(String title, Calendar calendar) {
            this.title = title;
            this.calendar = calendar;
        }
    }

    public static Result parse(String input, String language) {
        String title = input;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        String lowerInput = input.toLowerCase();
        boolean isVietnamese = "vi".equalsIgnoreCase(language);

        if (isVietnamese) {
            if (lowerInput.contains("mai")) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                title = title.replaceAll("(?i)\\bmai\\b", "");
            } else if (lowerInput.contains("mốt")) {
                calendar.add(Calendar.DAY_OF_YEAR, 2);
                title = title.replaceAll("(?i)\\bmốt\\b", "");
            } else if (lowerInput.contains("hôm nay")) {
                title = title.replaceAll("(?i)\\bhôm nay\\b", "");
            }
        } else {
            // English
            if (lowerInput.contains("tomorrow")) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                title = title.replaceAll("(?i)\\btomorrow\\b", "");
            } else if (lowerInput.contains("today")) {
                title = title.replaceAll("(?i)\\btoday\\b", "");
            }
            // Add "day after tomorrow" if needed
            if (lowerInput.contains("day after tomorrow")) {
                calendar.add(Calendar.DAY_OF_YEAR, 2);
                title = title.replaceAll("(?i)\\bday after tomorrow\\b", "");
            }
        }

        String timeRegex = isVietnamese 
                ? "(?:lúc|vào lúc)?\\s*(\\d{1,2})(?:[:h g])?(\\d{1,2})?"
                : "(?:at)?\\s*(\\d{1,2})(?:[:h])?(\\d{1,2})?";
        
        Pattern timePattern = Pattern.compile(timeRegex);
        Matcher matcher = timePattern.matcher(lowerInput);

        if (matcher.find()) {
            String hourStr = matcher.group(1);
            String minuteStr = matcher.group(2);
            
            if (hourStr != null) {
                int hour = Integer.parseInt(hourStr);
                int minute = 0;
                if (minuteStr != null && !minuteStr.isEmpty()) {
                    try {
                        minute = Integer.parseInt(minuteStr);
                    } catch (NumberFormatException ignored) {}
                }

                boolean isPM = false;
                boolean isAM = false;

                if (isVietnamese) {
                    if (lowerInput.contains("chiều") || lowerInput.contains("tối")) isPM = true;
                    if (lowerInput.contains("sáng")) isAM = true;
                } else {
                    if (lowerInput.contains("pm") || lowerInput.contains("afternoon") || lowerInput.contains("evening") || lowerInput.contains("night")) isPM = true;
                    if (lowerInput.contains("am") || lowerInput.contains("morning")) isAM = true;
                }

                if (isPM && hour < 12) hour += 12;
                if (isAM && hour == 12) hour = 0;

                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);

                title = title.replace(matcher.group(0), "");
            }
        }

        if (isVietnamese) {
            title = title.replaceAll("(?i)\\b(chiều|tối|sáng|trưa|lúc|vào)\\b", "");
        } else {
            title = title.replaceAll("(?i)\\b(am|pm|morning|afternoon|evening|night|at|on|in)\\b", "");
        }

        title = title.replaceAll("\\s+", " ").trim();
        
        if (title.isEmpty()) {
            title = isVietnamese ? "Sự kiện mới" : "New Event";
        }

        return new Result(title, calendar);
    }
}
