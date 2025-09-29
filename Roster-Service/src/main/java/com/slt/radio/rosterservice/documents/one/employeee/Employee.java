package com.slt.radio.rosterservice.documents.one.employeee;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "employees")
public class Employee {
    @Id
    private String id;

    @Indexed(unique = true)
    private String employeeId;

    private String name;
    private String mobileNo;
    private String shortName;
    private boolean active;
    private String teamId;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private Map<String, Map<String, String>> shiftCodeMap;

    public Employee() {}

    public Employee(String id, String employeeId, String name, String mobileNo,
                    String shortName, boolean active, String teamId,
                    LocalDateTime createdAt, LocalDateTime updatedAt,
                    Map<String, Map<String, String>> shiftCodeMap) {
        this.id = id;
        this.employeeId = employeeId;
        this.name = name;
        this.mobileNo = mobileNo;
        this.shortName = shortName;
        this.active = active;
        this.teamId = teamId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.shiftCodeMap = shiftCodeMap;
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

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Map<String, Map<String, String>> getShiftCodeMap() {
        return shiftCodeMap;
    }

    public void setShiftCodeMap(Map<String, Map<String, String>> shiftCodeMap) {
        this.shiftCodeMap = shiftCodeMap;
    }

    // Builder pattern
    public static EmployeeBuilder builder() {
        return new EmployeeBuilder();
    }

    public static class EmployeeBuilder {
        private String id;
        private String employeeId;
        private String name;
        private String mobileNo;
        private String shortName;
        private boolean active;
        private String teamId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Map<String, Map<String, String>> shiftCodeMap;

        public EmployeeBuilder id(String id) {
            this.id = id;
            return this;
        }

        public EmployeeBuilder employeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public EmployeeBuilder name(String name) {
            this.name = name;
            return this;
        }

        public EmployeeBuilder mobileNo(String mobileNo) {
            this.mobileNo = mobileNo;
            return this;
        }

        public EmployeeBuilder shortName(String shortName) {
            this.shortName = shortName;
            return this;
        }

        public EmployeeBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public EmployeeBuilder teamId(String teamId) {
            this.teamId = teamId;
            return this;
        }

        public EmployeeBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public EmployeeBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public EmployeeBuilder shiftCodeMap(Map<String, Map<String, String>> shiftCodeMap) {
            this.shiftCodeMap = shiftCodeMap;
            return this;
        }

        public Employee build() {
            return new Employee(id, employeeId, name, mobileNo, shortName, active,
                    teamId, createdAt, updatedAt, shiftCodeMap);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return active == employee.active &&
                java.util.Objects.equals(id, employee.id) &&
                java.util.Objects.equals(employeeId, employee.employeeId) &&
                java.util.Objects.equals(name, employee.name) &&
                java.util.Objects.equals(mobileNo, employee.mobileNo) &&
                java.util.Objects.equals(shortName, employee.shortName) &&
                java.util.Objects.equals(teamId, employee.teamId) &&
                java.util.Objects.equals(createdAt, employee.createdAt) &&
                java.util.Objects.equals(updatedAt, employee.updatedAt) &&
                java.util.Objects.equals(shiftCodeMap, employee.shiftCodeMap);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, employeeId, name, mobileNo, shortName,
                active, teamId, createdAt, updatedAt, shiftCodeMap);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id='" + id + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", name='" + name + '\'' +
                ", mobileNo='" + mobileNo + '\'' +
                ", shortName='" + shortName + '\'' +
                ", active=" + active +
                ", teamId='" + teamId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", shiftCodeMap=" + shiftCodeMap +
                '}';
    }
}