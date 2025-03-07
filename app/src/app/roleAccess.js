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
    ROLE_SUPERVISOR: [...ROLE_EMPLOYEE, "/manage-leave-requests", "/manage-movement-requests", "/unsuccessful-leaves", "/unauthorized-leaves"],
    ROLE_ADMIN: [...ROLE_EMPLOYEE, "/manage-employees"],
    ROLE_CEO: [...ROLE_EMPLOYEE, "/employee-activities", "/absent-employees", "/no-pay-leaves", "/manage-leave-requests", "/manage-movement-requests", "/unsuccessful-leaves", "/unauthorized-leaves"],
    ROLE_CHAIRMAN: [...ROLE_EMPLOYEE, "/employee-activities", "/absent-employees", "/no-pay-leaves", "/manage-leave-requests", "/manage-movement-requests", "/unsuccessful-leaves", "/unauthorized-leaves"],
  };
})();


export const hasAccess = (userRoles, route) => {

  if (!Array.isArray(userRoles)) {
    return false;
  }

  const normalizedRoute = route.replace(/^\/|\/$/g, "");

  return userRoles.some((role) => {
    const routesForRole = roleAccessRules[role];
    if (!routesForRole) {
      return false;
    }
    const normalizedRoutes = routesForRole.map((r) => r.replace(/^\/|\/$/g, ""));
    const hasPermission = normalizedRoutes.includes(normalizedRoute);
    return hasPermission;
  });
};