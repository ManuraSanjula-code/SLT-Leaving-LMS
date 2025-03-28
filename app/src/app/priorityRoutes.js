/**
 * Defines route access based on priority levels
 * Lower number = higher privilege
 */
export const priorityRouteAccess = {
    // Priority 1-9 (Super/System Roles)
    1: [
        "/system-settings",
        "/manage-admins"
    ],

    // Priority 10-49 (Administrative Roles)
    10: [
        "/manage-employees",
        "/employee-activities",
        "/absent-employees",
        "/no-pay-leaves"
    ],

    // Priority 50-99 (Managerial Roles)
    50: [
        "/manage-leave-requests",
        "/manage-movement-requests",
        "/unsuccessful-leaves",
        "/unauthorized-leaves"
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
        "/"
    ],

    // Priority 200-499 (Restricted/Temp)
    200: [
        "/dashboard",
        "/profile",
        "/manage-employees",
    ]
};

/**
 * Gets all routes accessible for a given priority level
 * @param {number} userPriority - The user's highest priority (lowest number)
 * @returns {Set<string>} - Set of accessible routes
 */
export const getAccessibleRoutes = (userPriority) => {
    const accessibleRoutes = new Set();

    // Add all routes where user's priority is <= defined priority
    Object.entries(priorityRouteAccess).forEach(([priority, routes]) => {
        if (userPriority <= Number(priority)) {
            routes.forEach(route => accessibleRoutes.add(route));
        }
    });

    return accessibleRoutes;
};

/**
 * Checks if a user has access to a specific route
 * @param {number} userPriority - The user's highest priority
 * @param {string} route - The route to check
 * @returns {boolean} - Whether access is granted
 */
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