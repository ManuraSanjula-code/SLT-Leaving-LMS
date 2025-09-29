package com.slt.radio.rosterservice.documents.one.lms;

public class EmployeeAttendanceDetail {
    private String employeeId;
    private String employeeName;
    private String teamId;
    private String shiftTime;
    private String attendanceStatus;
    private String arrivalTime;
    private String leftTime;
    private Long lateMinutes;
    private boolean isRotationShift;

    public EmployeeAttendanceDetail() {}

    public EmployeeAttendanceDetail(String employeeId, String employeeName, String teamId,
                                    String shiftTime, String attendanceStatus, String arrivalTime,
                                    String leftTime, Long lateMinutes, boolean isRotationShift) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.teamId = teamId;
        this.shiftTime = shiftTime;
        this.attendanceStatus = attendanceStatus;
        this.arrivalTime = arrivalTime;
        this.leftTime = leftTime;
        this.lateMinutes = lateMinutes;
        this.isRotationShift = isRotationShift;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getShiftTime() {
        return shiftTime;
    }

    public void setShiftTime(String shiftTime) {
        this.shiftTime = shiftTime;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getLeftTime() {
        return leftTime;
    }

    public void setLeftTime(String leftTime) {
        this.leftTime = leftTime;
    }

    public Long getLateMinutes() {
        return lateMinutes;
    }

    public void setLateMinutes(Long lateMinutes) {
        this.lateMinutes = lateMinutes;
    }

    public boolean isRotationShift() {
        return isRotationShift;
    }

    public void setRotationShift(boolean rotationShift) {
        isRotationShift = rotationShift;
    }

    // Builder pattern
    public static EmployeeAttendanceDetailBuilder builder() {
        return new EmployeeAttendanceDetailBuilder();
    }

    public static class EmployeeAttendanceDetailBuilder {
        private String employeeId;
        private String employeeName;
        private String teamId;
        private String shiftTime;
        private String attendanceStatus;
        private String arrivalTime;
        private String leftTime;
        private Long lateMinutes;
        private boolean isRotationShift;

        public EmployeeAttendanceDetailBuilder employeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public EmployeeAttendanceDetailBuilder employeeName(String employeeName) {
            this.employeeName = employeeName;
            return this;
        }

        public EmployeeAttendanceDetailBuilder teamId(String teamId) {
            this.teamId = teamId;
            return this;
        }

        public EmployeeAttendanceDetailBuilder shiftTime(String shiftTime) {
            this.shiftTime = shiftTime;
            return this;
        }

        public EmployeeAttendanceDetailBuilder attendanceStatus(String attendanceStatus) {
            this.attendanceStatus = attendanceStatus;
            return this;
        }

        public EmployeeAttendanceDetailBuilder arrivalTime(String arrivalTime) {
            this.arrivalTime = arrivalTime;
            return this;
        }

        public EmployeeAttendanceDetailBuilder leftTime(String leftTime) {
            this.leftTime = leftTime;
            return this;
        }

        public EmployeeAttendanceDetailBuilder lateMinutes(Long lateMinutes) {
            this.lateMinutes = lateMinutes;
            return this;
        }

        public EmployeeAttendanceDetailBuilder isRotationShift(boolean isRotationShift) {
            this.isRotationShift = isRotationShift;
            return this;
        }

        public EmployeeAttendanceDetail build() {
            return new EmployeeAttendanceDetail(employeeId, employeeName, teamId, shiftTime,
                    attendanceStatus, arrivalTime, leftTime, lateMinutes,
                    isRotationShift);
        }
    }
}