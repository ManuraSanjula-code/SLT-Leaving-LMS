export const priorityRouteAccess = {
    // Priority 1-9 (Super/System Roles)
    1: [
        "/manage-employees",
    ],

    // Priority 10-29 (Administrative Roles)
    10: [
        "/employee-activities",
        "/absent-employees-admin",
        "/no-pay-leaves-admin",
        "/manage-leave-requests",
        "/manage-movement-requests",
        "/unsuccessful-leaves-admin",
        "/unauthorized-leaves-admin",
    ],

    // 30-49 (HR Roles)
    30:[
        "/employee-activities",
        "/absent-employees-admin",
        "/no-pay-leaves-admin",
    ],

    // Priority 50-99 (Managerial Roles)
    50: [
        "/manage-leave-requests",
        "/manage-movement-requests",
        "/unsuccessful-leaves-admin",
        "/unauthorized-leaves-admin",
        "/no-pay-leaves-admin",
        "/employee-activities",
    ],

    // Priority 100-199 (Standard Users)
    100: [
        "/dashboard",
        "/apply-leave",
        "/request-movement",
        "/profile",
        "/all-leaves",
        "/all-movements",
        "/single-employee-activities",
        "/unsuccessful-leaves",
        "/unauthorized-leaves",
        "/no-pay-leaves",
        "/absent-employee",
        "/"
    ]
};


export const getAccessibleRoutes = (userPriority) => {
    const accessibleRoutes = new Set();

    // Define the range mappings
    const priorityRanges = [
        { min: 1, max: 9, key: 1 },      // Super/System Roles
        { min: 10, max: 29, key: 10 },   // Administrative Roles
        { min: 30, max: 49, key: 30 },    // HR Roles
        { min: 50, max: 99, key: 50 },    // Managerial Roles
        { min: 100, max: 199, key: 100 }, // Standard Users
        { min: 0, max: 499, key: 200 }  // Restricted/Temp
    ];

    // Find the user's priority range
    const userRange = priorityRanges.find(range =>
        userPriority >= range.min && userPriority <= range.max
    );

    if (!userRange) return accessibleRoutes;

    // Add routes from the user's specific role range
    const roleRoutes = priorityRouteAccess[userRange.key] || [];
    roleRoutes.forEach(route => accessibleRoutes.add(route));

    // For all non-temp users, add basic user routes
    if (userRange.key !== 200) {
        const basicUserRoutes = priorityRouteAccess[100] || [];
        basicUserRoutes.forEach(route => accessibleRoutes.add(route));
    }

    // Special case for temp users (200)
    if (userRange.key === 200) {
        accessibleRoutes.add("/manage-employees");
        accessibleRoutes.add("/profile");
        accessibleRoutes.add("/dashboard");
        accessibleRoutes.add("/");
    }

    return accessibleRoutes;
};


export const hasPriorityAccess = (userPriority, route) => {
    const normalizedRoute = route.replace(/^\/|\/$/g, "");

    for (const [priority, routes] of Object.entries(priorityRouteAccess)) {
        if (userPriority <= Number(priority)) {
            const found = routes.some(r =>
                r.replace(/^\/|\/$/g, "") === normalizedRoute
            );
            if (found) return true;
        }
    }

    return false;
};