package com.slt.radio.rosterservice.documents.one.obj;

public class EmployeeShift {
    private String employeeId;
    private int totalShift;
    private int rotShift;
    private int offDay;
    private int dDuty;

    public EmployeeShift() {}

    public EmployeeShift(String employeeId, int totalShift, int rotShift, int offDay, int dDuty) {
        this.employeeId = employeeId;
        this.totalShift = totalShift;
        this.rotShift = rotShift;
        this.offDay = offDay;
        this.dDuty = dDuty;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public int getTotalShift() {
        return totalShift;
    }

    public void setTotalShift(int totalShift) {
        this.totalShift = totalShift;
    }

    public int getRotShift() {
        return rotShift;
    }

    public void setRotShift(int rotShift) {
        this.rotShift = rotShift;
    }

    public int getOffDay() {
        return offDay;
    }

    public void setOffDay(int offDay) {
        this.offDay = offDay;
    }

    public int getDDuty() {
        return dDuty;
    }

    public void setDDuty(int dDuty) {
        this.dDuty = dDuty;
    }

    // Builder pattern
    public static EmployeeShiftBuilder builder() {
        return new EmployeeShiftBuilder();
    }

    public static class EmployeeShiftBuilder {
        private String employeeId;
        private int totalShift;
        private int rotShift;
        private int offDay;
        private int dDuty;

        public EmployeeShiftBuilder employeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public EmployeeShiftBuilder totalShift(int totalShift) {
            this.totalShift = totalShift;
            return this;
        }

        public EmployeeShiftBuilder rotShift(int rotShift) {
            this.rotShift = rotShift;
            return this;
        }

        public EmployeeShiftBuilder offDay(int offDay) {
            this.offDay = offDay;
            return this;
        }

        public EmployeeShiftBuilder dDuty(int dDuty) {
            this.dDuty = dDuty;
            return this;
        }

        public EmployeeShift build() {
            return new EmployeeShift(employeeId, totalShift, rotShift, offDay, dDuty);
        }
    }
}