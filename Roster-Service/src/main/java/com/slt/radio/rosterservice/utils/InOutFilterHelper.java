package com.slt.radio.rosterservice.utils;

import com.slt.radio.rosterservice.documents.one.lms.InOut;
import com.slt.radio.rosterservice.repo.InOutRepository;

import java.util.*;
import java.time.LocalTime;
import java.time.ZoneId;

public class InOutFilterHelper {

    public static InOut getEarliestInOutForShift(List<InOut> inOuts, String shiftTime) {
        // Parse shift time string
        String[] times = shiftTime.split(" - ");
        if (times.length != 2) {
            throw new IllegalArgumentException("Invalid shift time format. Use 'HH:mm - HH:mm'");
        }

        int startHour = Integer.parseInt(times[0].split(":")[0]);
        int endHour = Integer.parseInt(times[1].split(":")[0]);
        InOut earliestInOut = null;
        Date earliestTime = null;

        for (InOut inOut : inOuts) {
            Date candidateTime = null;

            if (inOut.getPunchTime() != null && isTimeInShift(inOut.getPunchTime(), startHour, endHour)) {
                candidateTime = inOut.getPunchTime();
            }
            // Update earliest if this record is earlier
            if (candidateTime != null && (earliestTime == null || candidateTime.before(earliestTime))) {
                earliestTime = candidateTime;
                earliestInOut = inOut;
            }
        }

        return earliestInOut;
    }


    private static boolean isTimeInShift(Date punchTime, int startHour, int endHour) {
        LocalTime time = punchTime.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalTime();

        int hour = time.getHour();

        // Handle overnight shifts (e.g., 22:00 - 06:00)
        if (startHour > endHour) {
            return hour >= startHour || hour < endHour;
        }
        // Normal shifts (e.g., 08:00 - 16:00)
        else {
            return hour >= startHour && hour < endHour;
        }
    }


    public static void filterByShifts(String cleanId, InOutRepository inOutRepository) {
        // Get data from database
        List<InOut> inOuts = inOutRepository.findByEmployeeIdAndDate(cleanId, getYesterdayDate());

        // Filter for Night Shift (00:00 - 08:00)
        InOut nightShiftEarliest = getEarliestInOutForShift(inOuts, "00:00 - 08:00");

        // Filter for Morning Shift (08:00 - 16:00)
        InOut morningShiftEarliest = getEarliestInOutForShift(inOuts, "08:00 - 16:00");

        // Filter for Evening Shift (16:00 - 24:00)
        InOut eveningShiftEarliest = getEarliestInOutForShift(inOuts, "16:00 - 24:00");

        // Use the results
        if (nightShiftEarliest != null) {
            System.out.println("Night shift earliest: " + nightShiftEarliest.getPunchTime());
        }
        if (morningShiftEarliest != null) {
            System.out.println("Morning shift earliest: " + morningShiftEarliest.getPunchTime());
        }
        if (eveningShiftEarliest != null) {
            System.out.println("Evening shift earliest: " + eveningShiftEarliest.getPunchTime());
        }
    }

    // Placeholder for your existing method
    private static Date getYesterdayDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime();
    }
}
