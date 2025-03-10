package com.slt.peotv.userservice.lms.shared;

public enum Roles {
    ROLE_USER,
    ROLE_ADMIN,
    ROLE_SUPERVISOR,
    ROLE_HOD,
    ROLE_EMPLOYEE,
    ROLE_CEO,
    ROLE_CHAIRMAN;

    // Method to map a string to an enum
    public static Roles fromString(String role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        switch (role.toUpperCase()) {
            case "EMP":
                return ROLE_EMPLOYEE;
            case "HOD":
                return ROLE_HOD;
            case "SUPAVISOR":
                return ROLE_SUPERVISOR;
            case "SUPERVISOR":  // Corrected from "SUPAVISOR"
                return ROLE_SUPERVISOR;
            case "CEO":
                return ROLE_CEO;
            case "CHAIRMAN":
            	return ROLE_CHAIRMAN;
            default:
                throw new IllegalArgumentException("Unknown role: " + role);
        }
    }
}