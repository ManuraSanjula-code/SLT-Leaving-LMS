package com.slt.radio.rosterservice.Utils;

import com.slt.radio.rosterservice.model.one.lms.InOut;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ShiftBasedInOutFilter {

    // Define shift time ranges
    private static final LocalTime NIGHT_START = LocalTime.of(0, 0);
    private static final LocalTime NIGHT_END = LocalTime.of(8, 0);
    private static final LocalTime DAY_START = LocalTime.of(8, 0);
    private static final LocalTime DAY_END = LocalTime.of(16, 0);
    private static final LocalTime EVENING_START = LocalTime.of(16, 0);
    private static final LocalTime EVENING_END = LocalTime.of(23, 59, 59);

    public enum Shift {
        NIGHT("00:00-08:00"),
        DAY("08:00-16:00"),
        EVENING("16:00-24:00");

        private final String timeRange;

        Shift(String timeRange) {
            this.timeRange = timeRange;
        }

        public String getTimeRange() {
            return timeRange;
        }
    }


    private static LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(timeStr.trim(), DateTimeFormatter.ofPattern("HH:mm:ss"));
        } catch (Exception e) {
            return null;
        }
    }


    private static Shift determineShift(LocalTime time) {
        if (time == null) return null;

        if ((time.equals(NIGHT_START) || time.isAfter(NIGHT_START)) && time.isBefore(NIGHT_END)) {
            return Shift.NIGHT;
        } else if ((time.equals(DAY_START) || time.isAfter(DAY_START)) && time.isBefore(DAY_END)) {
            return Shift.DAY;
        } else if ((time.equals(EVENING_START) || time.isAfter(EVENING_START)) &&
                (time.equals(EVENING_END) || time.isBefore(EVENING_END))) {
            return Shift.EVENING;
        } else {
            return Shift.NIGHT; // Handle edge case for times after 23:59:59
        }
    }

    public static Shift getShiftFromTimeRange(String shiftTime) {
        if (shiftTime == null || shiftTime.trim().isEmpty()) {
            throw new IllegalArgumentException("Shift time cannot be null or empty");
        }

        String normalizedShiftTime = shiftTime.trim();

        // Check against each enum's time range
        for (Shift shift : Shift.values()) {
            if (shift.getTimeRange().equals(normalizedShiftTime)) {
                return shift;
            }
        }

        // If no exact match, try to determine based on start time
        String[] timeRange = normalizedShiftTime.split("-");
        if (timeRange.length == 2) {
            String startTime = timeRange[0].trim();

            switch (startTime) {
                case "00:00":
                    return Shift.NIGHT;
                case "08:00":
                    return Shift.DAY;
                case "16:00":
                    return Shift.EVENING;
                default:
                    // Try to parse and determine shift
                    LocalTime start = parseTimeFromString(startTime + ":00");
                    if (start != null) {
                        if (start.isBefore(LocalTime.of(8, 0))) {
                            return Shift.NIGHT;
                        } else if (start.isBefore(LocalTime.of(16, 0))) {
                            return Shift.DAY;
                        } else {
                            return Shift.EVENING;
                        }
                    }
            }
        }

        throw new IllegalArgumentException("Invalid shift time format: " + shiftTime +
                ". Expected format: HH:mm-HH:mm (e.g., '08:00-16:00')");
    }

    private static LocalTime parseTimeFromString(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        try {
            String cleanTime = timeStr.trim();
            if (cleanTime.matches("\\d{2}:\\d{2}")) {
                cleanTime += ":00";
            }
            return LocalTime.parse(cleanTime, DateTimeFormatter.ofPattern("HH:mm:ss"));
        } catch (Exception e) {
            return null;
        }
    }



    public static List<InOut> filterByShift(List<InOut> inOuts, Shift targetShift) {
        return inOuts.stream()
                .filter(inOut -> {
                    // Check morning punch time
                    if (inOut.getPunchTypeTime() != null) {
                        LocalTime morningTime = inOut.getPunchTypeTime();
                        if (morningTime != null && determineShift(morningTime) == targetShift) {
                            return true;
                        }
                    }

                    // Check evening punch time
                    if (inOut.getPunchTypeTime() != null) {
                        LocalTime eveningTime = inOut.getPunchTypeTime();
                        if (eveningTime != null && determineShift(eveningTime) == targetShift) {
                            return true;
                        }
                    }

                    return false;
                })
                .collect(Collectors.toList());
    }


    public static Map<Shift, List<InOut>> groupByShift(List<InOut> inOuts) {
        Map<Shift, List<InOut>> shiftGroups = new EnumMap<>(Shift.class);

        // Initialize empty lists for each shift
        for (Shift shift : Shift.values()) {
            shiftGroups.put(shift, new ArrayList<>());
        }

        for (InOut inOut : inOuts) {
            Set<Shift> assignedShifts = new HashSet<>();

            // Check morning punch time
            if (inOut.getPunchTypeTime() != null) {
                LocalTime morningTime = inOut.getPunchTypeTime();
                if (morningTime != null) {
                    Shift shift = determineShift(morningTime);
                    assignedShifts.add(shift);
                }
            }

            // Check evening punch time
            if (inOut.getPunchTypeTime() != null) {
                LocalTime eveningTime = inOut.getPunchTypeTime();
                if (eveningTime != null) {
                    Shift shift = determineShift(eveningTime);
                    assignedShifts.add(shift);
                }
            }

            // Add to all applicable shifts
            for (Shift shift : assignedShifts) {
                shiftGroups.get(shift).add(inOut);
            }
        }

        return shiftGroups;
    }


    // Alternative: Simple stream filter approach for your specific use case
    public static void simpleFilterExample(List<InOut> inOuts) {
        // Filter for night shift (00:00-08:00)
        List<InOut> nightShiftRecords = inOuts.stream()
                .filter(inOut -> {
                    return (inOut.getPunchTypeTime() != null && isInTimeRange(inOut.getPunchTypeTime(), "00:00:00", "07:59:59")) ||
                            (inOut.getPunchTypeTime() != null && isInTimeRange(inOut.getPunchTypeTime(), "00:00:00", "07:59:59"));
                })
                .collect(Collectors.toList());

        // Filter for day shift (08:00-16:00)
        List<InOut> dayShiftRecords = inOuts.stream()
                .filter(inOut -> {
                    return (inOut.getPunchTypeTime() != null && isInTimeRange(inOut.getPunchTypeTime(), "08:00:00", "15:59:59")) ||
                            (inOut.getPunchTypeTime() != null && isInTimeRange(inOut.getPunchTypeTime(), "08:00:00", "15:59:59"));
                })
                .collect(Collectors.toList());

        // Filter for evening shift (16:00-24:00)
        List<InOut> eveningShiftRecords = inOuts.stream()
                .filter(inOut -> {
                    return (inOut.getPunchTypeTime() != null && isInTimeRange(inOut.getPunchTypeTime(), "16:00:00", "23:59:59")) ||
                            (inOut.getPunchTypeTime() != null && isInTimeRange(inOut.getPunchTypeTime(), "16:00:00", "23:59:59"));
                })
                .collect(Collectors.toList());
    }

    private static boolean isInTimeRange(String timeStr, String startTime, String endTime) {
        try {
            LocalTime time = parseTime(timeStr);
            LocalTime start = parseTime(startTime);
            LocalTime end = parseTime(endTime);

            return time != null && start != null && end != null &&
                    (time.equals(start) || time.isAfter(start)) &&
                    (time.equals(end) || time.isBefore(end));
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isInTimeRange(LocalTime time, String startTime, String endTime) {
        try {
            LocalTime start = parseTime(startTime);
            LocalTime end = parseTime(endTime);

            return time != null && start != null && end != null &&
                    (time.equals(start) || time.isAfter(start)) &&
                    (time.equals(end) || time.isBefore(end));
        } catch (Exception e) {
            return false;
        }
    }
}
