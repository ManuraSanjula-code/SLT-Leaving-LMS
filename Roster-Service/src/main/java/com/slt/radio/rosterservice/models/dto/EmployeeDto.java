package com.slt.radio.rosterservice.models.dto;

import javax.validation.constraints.NotBlank;

public class EmployeeDto {
    private String id;

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotBlank(message = "Name is required")
    private String name;
    private String teamId;
    private String mobileNo;
    private String shortName;
    private boolean active;

    public EmployeeDto() {}

    public EmployeeDto(String id, String employeeId, String name, String teamId,
                       String mobileNo, String shortName, boolean active) {
        this.id = id;
        this.employeeId = employeeId;
        this.name = name;
        this.teamId = teamId;
        this.mobileNo = mobileNo;
        this.shortName = shortName;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeDto that = (EmployeeDto) o;
        return active == that.active &&
                java.util.Objects.equals(id, that.id) &&
                java.util.Objects.equals(employeeId, that.employeeId) &&
                java.util.Objects.equals(name, that.name) &&
                java.util.Objects.equals(teamId, that.teamId) &&
                java.util.Objects.equals(mobileNo, that.mobileNo) &&
                java.util.Objects.equals(shortName, that.shortName);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, employeeId, name, teamId, mobileNo, shortName, active);
    }

    @Override
    public String toString() {
        return "EmployeeDto{" +
                "id='" + id + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", name='" + name + '\'' +
                ", teamId='" + teamId + '\'' +
                ", mobileNo='" + mobileNo + '\'' +
                ", shortName='" + shortName + '\'' +
                ", active=" + active +
                '}';
    }

    // Builder pattern
    public static EmployeeDtoBuilder builder() {
        return new EmployeeDtoBuilder();
    }

    public static class EmployeeDtoBuilder {
        private String id;
        private String employeeId;
        private String name;
        private String teamId;
        private String mobileNo;
        private String shortName;
        private boolean active;

        public EmployeeDtoBuilder id(String id) {
            this.id = id;
            return this;
        }

        public EmployeeDtoBuilder employeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public EmployeeDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public EmployeeDtoBuilder teamId(String teamId) {
            this.teamId = teamId;
            return this;
        }

        public EmployeeDtoBuilder mobileNo(String mobileNo) {
            this.mobileNo = mobileNo;
            return this;
        }

        public EmployeeDtoBuilder shortName(String shortName) {
            this.shortName = shortName;
            return this;
        }

        public EmployeeDtoBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public EmployeeDto build() {
            return new EmployeeDto(id, employeeId, name, teamId, mobileNo, shortName, active);
        }
    }
}