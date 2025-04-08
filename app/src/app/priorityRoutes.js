/**
 * Defines route access based on priority levels
 * Lower number = higher privilege
 */
export const priorityRouteAccess = {
    // Priority 1-9 (Super/System Roles)
    1: [
        "/manage-employees",
    ],

    // Priority 10-29 (Administrative Roles)
    10: [
        "/employee-activities",
        "/absent-employees",
        "/no-pay-leaves",
        "/manage-leave-requests",
        "/manage-movement-requests",
        "/unsuccessful-leaves",
        "/unauthorized-leaves"
    ],

    // 30-49 (HR Roles)
    30:[
        "/employee-activities",
        "/absent-employees",
        "/no-pay-leaves"
    ],

    // Priority 50-99 (Managerial Roles)
    50: [
        "/manage-leave-requests",
        "/manage-movement-requests",
        "/unsuccessful-leaves",
        "/unauthorized-leaves",
        "/no-pay-leaves",
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
        "/"
    ]
};

/**
 * Gets all routes accessible for a given priority level
 * @param {number} userPriority - The user's highest priority (lowest number)
 * @returns {Set<string>} - Set of accessible routes
 */
export const getAccessibleRoutes = (userPriority) => {
    const accessibleRoutes = new Set();

    // Define the range mappings based on comments
    const priorityRanges = [
        { min: 1, max: 9, key: 1 },      // Super/System Roles
        { min: 10, max: 29, key: 10 },   // Administrative Roles
        { min: 30, max: 49, key: 30 },   // HR Roles
        { min: 50, max: 99, key: 50 },   // Managerial Roles
        { min: 100, max: 199, key: 100 }, // Standard Users
        { min: 200, max: 499, key: 200 }  // Restricted/Temp
    ];

    // Find the user's priority range and all higher priority ranges
    const applicableRanges = priorityRanges.filter(range =>
        userPriority >= range.min && userPriority <= range.max || // User is in this range
        userPriority < range.min // User has higher priority than this range
    );

    // Add routes from all applicable ranges
    applicableRanges.forEach(range => {
        const routes = priorityRouteAccess[range.key] || [];
        routes.forEach(route => accessibleRoutes.add(route));
    });

    console.log(accessibleRoutes);
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