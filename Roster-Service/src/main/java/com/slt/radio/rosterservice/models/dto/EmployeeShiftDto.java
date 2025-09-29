package com.slt.radio.rosterservice.models.dto;

import javax.validation.constraints.NotBlank;
import org.springframework.data.annotation.Transient;

public class EmployeeShiftDto {
    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    private int totalShift;
    private int rotShift;
    private int offDay;
    private int dDuty;

    @Transient
    private String name;

    @Transient
    private String mobileNo;

    @Transient
    private String codeName;

    public EmployeeShiftDto() {}

    public EmployeeShiftDto(String employeeId, int totalShift, int rotShift, int offDay,
                            int dDuty, String name, String mobileNo, String codeName) {
        this.employeeId = employeeId;
        this.totalShift = totalShift;
        this.rotShift = rotShift;
        this.offDay = offDay;
        this.dDuty = dDuty;
        this.name = name;
        this.mobileNo = mobileNo;
        this.codeName = codeName;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getCodeName() {
        return codeName;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeShiftDto that = (EmployeeShiftDto) o;
        return totalShift == that.totalShift &&
                rotShift == that.rotShift &&
                offDay == that.offDay &&
                dDuty == that.dDuty &&
                java.util.Objects.equals(employeeId, that.employeeId) &&
                java.util.Objects.equals(name, that.name) &&
                java.util.Objects.equals(mobileNo, that.mobileNo) &&
                java.util.Objects.equals(codeName, that.codeName);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(employeeId, totalShift, rotShift, offDay, dDuty,
                name, mobileNo, codeName);
    }

    @Override
    public String toString() {
        return "EmployeeShiftDto{" +
                "employeeId='" + employeeId + '\'' +
                ", totalShift=" + totalShift +
                ", rotShift=" + rotShift +
                ", offDay=" + offDay +
                ", dDuty=" + dDuty +
                ", name='" + name + '\'' +
                ", mobileNo='" + mobileNo + '\'' +
                ", codeName='" + codeName + '\'' +
                '}';
    }

    // Builder pattern
    public static EmployeeShiftDtoBuilder builder() {
        return new EmployeeShiftDtoBuilder();
    }

    public static class EmployeeShiftDtoBuilder {
        private String employeeId;
        private int totalShift;
        private int rotShift;
        private int offDay;
        private int dDuty;
        private String name;
        private String mobileNo;
        private String codeName;

        public EmployeeShiftDtoBuilder employeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public EmployeeShiftDtoBuilder totalShift(int totalShift) {
            this.totalShift = totalShift;
            return this;
        }

        public EmployeeShiftDtoBuilder rotShift(int rotShift) {
            this.rotShift = rotShift;
            return this;
        }

        public EmployeeShiftDtoBuilder offDay(int offDay) {
            this.offDay = offDay;
            return this;
        }

        public EmployeeShiftDtoBuilder dDuty(int dDuty) {
            this.dDuty = dDuty;
            return this;
        }

        public EmployeeShiftDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public EmployeeShiftDtoBuilder mobileNo(String mobileNo) {
            this.mobileNo = mobileNo;
            return this;
        }

        public EmployeeShiftDtoBuilder codeName(String codeName) {
            this.codeName = codeName;
            return this;
        }

        public EmployeeShiftDto build() {
            return new EmployeeShiftDto(employeeId, totalShift, rotShift, offDay, dDuty,
                    name, mobileNo, codeName);
        }
    }
}