package com.slt.radio.rosterservice.Utils;

import com.slt.radio.rosterservice.Model.One.LMS.AccessLog;
import com.slt.radio.rosterservice.Model.One.LMS.Attendance;
import com.slt.radio.rosterservice.Model.One.LMS.InOut;
import com.slt.radio.rosterservice.Repo.AccessLogRepository;
import com.slt.radio.rosterservice.Repo.AttendanceRepository;
import com.slt.radio.rosterservice.Repo.InOutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class Helper {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private InOutRepository inOutRepository;

    @Autowired
    private AccessLogRepository accessLogRepository;

    public Date getYesterdayDate() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public Date getYesterdayDate_() {
        LocalDate yesterday = LocalDate.now().minusDays(2);
        return Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public Date getTomorrowDate() {
        LocalDate yesterday = LocalDate.now().plusDays(1);
        return Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public Date stripTimeFromDate(Date dateWithTime) {
        if (dateWithTime == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateWithTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public Date getDueDate() {
        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.WEEK_OF_YEAR, 1);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }
    
    public String formatDateToString(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }

    public String getFormattedYesterdayDate() {
        Date yesterday = getYesterdayDate();
        Date yesterdayWithoutTime = stripTimeFromDate(yesterday);
        return formatDateToString(yesterdayWithoutTime);
    }

    public boolean isDuplicateAttendance(Attendance newAttendance) {
        if (newAttendance == null || newAttendance.getEmployeeId() == null ||
                newAttendance.getDate() == null) {
            return true;
        }

        List<Attendance> existing = attendanceRepository.findByEmployeeIdAndArrivalDate(
                newAttendance.getEmployeeId(),
                newAttendance.getDate()
        );

        return existing.stream().anyMatch(existingAtt ->
                isTimeMatch(existingAtt.getArrivalTime(), newAttendance.getArrivalTime()) &&
                        isTimeMatch(existingAtt.getLeftTime(), newAttendance.getLeftTime())
        );
    }

    public boolean isTimeMatch(LocalTime time1, LocalTime time2) {
        if (time1 == null && time2 == null) return true;
        if (time1 == null || time2 == null) return false;
        return time1.truncatedTo(ChronoUnit.MINUTES)
                .equals(time2.truncatedTo(ChronoUnit.MINUTES));
    }

    public boolean checkForDuplicateInOut(InOut newInOut) {
        if (newInOut == null || newInOut.getEmployeeId() == null ||
                newInOut.getDate() == null || newInOut.getPunchTypeTime() == null) {
            return true; // consider invalid entries as duplicates to skip
        }

        return inOutRepository.existsByEmployeeIdAndPunchTimeAndPunchTypeTimeAndInOutValue(
                newInOut.getEmployeeId(),
                newInOut.getDate(),
                newInOut.getPunchTypeTime(),
                newInOut.getInOutValue()
        );
    }

    public boolean isDuplicateAccessLog(AccessLog accessLog) {
        if (accessLog == null || accessLog.getEmployeeId() == null ||
                accessLog.getLogDate() == null || accessLog.getLogTime() == null) {
            return true;
        }

        return accessLogRepository.existsByEmployeeIdAndLogDateAndLogTimeAndTerminalIdAndInOut(
                accessLog.getEmployeeId(),
                accessLog.getLogDate(),
                accessLog.getLogTime(),
                accessLog.getTerminalId(),
                accessLog.getInOut()
        );
    }
}
