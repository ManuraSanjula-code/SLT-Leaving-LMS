export const roleAccessRules = (() => {
    const ROLE_EMPLOYEE = [
      "/dashboard",
      "/apply-leave",
      "/request-movement",
      "/profile",
      "/all-leaves",
      "/all-movements",
      "/single-employee-activities",
      "/"
    ];
  
    return {
      ROLE_EMPLOYEE,
      ROLE_HOD: [...ROLE_EMPLOYEE, "/manage-leave-requests", "/manage-movement-requests", "/unsuccessful-leaves", "/unauthorized-leaves"],
      ROLE_SUPERVISOR: [...ROLE_EMPLOYEE, "/manage-leave-requests", "/manage-movement-roles", "/unsuccessful-leaves", "/unauthorized-leaves"],
      ROLE_ADMIN: [...ROLE_EMPLOYEE, "/manage-employees"],
      ROLE_CEO: [...ROLE_EMPLOYEE, "/employee-activities", "/absent-employees", "/no-pay-leaves", "/manage-leave-requests", "/manage-movement-requests", "/unsuccessful-leaves", "/unauthorized-leaves"],
      ROLE_CHAIRMAN: [...ROLE_EMPLOYEE, "/employee-activities", "/absent-employees", "/no-pay-leaves", "/manage-leave-requests", "/manage-movement-requests", "/unsuccessful-leaves", "/unauthorized-leaves"],
    };
  })();