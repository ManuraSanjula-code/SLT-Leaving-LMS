package com.slt.peotv.lmsmangmentservice.security.Priority;


import com.slt.peotv.lmsmangmentservice.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;


@Component("prioritySecurity")
public class PrioritySecurity {
    private static final Logger logger = LoggerFactory.getLogger(PrioritySecurity.class);


    public boolean hasAnyPriority(Integer... priorities) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.debug("Authentication is null or not authenticated");
            return false;
        }

        UserPrincipal principal = null;
        if (authentication.getPrincipal() instanceof UserPrincipal) {
            principal = (UserPrincipal) authentication.getPrincipal();
        } else {
            logger.debug("Principal is not a UserPrincipal: {}", authentication.getPrincipal().getClass().getName());
            return false;
        }

        Integer userPriority = principal.getHighestRolePriority();
        if (userPriority == null) {
            logger.debug("User has no priority set");
            return false;
        }

        boolean result = Arrays.stream(priorities).anyMatch(priority -> priority.equals(userPriority));
        logger.debug("hasAnyPriority({}) for user with priority {} returned: {}",
                Arrays.toString(priorities), userPriority, result);
        return result;
    }

    public boolean hasPriorityInRange(int minPriority, int maxPriority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.debug("Authentication is null or not authenticated");
            return false;
        }

        UserPrincipal principal = null;
        if (authentication.getPrincipal() instanceof UserPrincipal) {
            principal = (UserPrincipal) authentication.getPrincipal();
        } else {
            logger.debug("Principal is not a UserPrincipal: {}", authentication.getPrincipal().getClass().getName());
            return false;
        }

        Integer userPriority = principal.getHighestRolePriority();
        if (userPriority == null) {
            logger.debug("User has no priority set");
            return false;
        }

        boolean result = userPriority >= minPriority && userPriority <= maxPriority;
        logger.debug("hasPriorityInRange({}, {}) for user with priority {} returned: {}",
                minPriority, maxPriority, userPriority, result);
        return result;
    }

    public boolean isAdminOrManagementOnly() {
        Integer userPriority = getCurrentUserPriority();
        if (userPriority == null) return false;

        boolean isAdmin = userPriority >= PriorityRanges.ADMIN_MIN &&
                userPriority <= PriorityRanges.ADMIN_MAX;

        boolean isManagement = userPriority >= PriorityRanges.MANAGEMENT_MIN &&
                userPriority <= PriorityRanges.MANAGEMENT_MAX;

        boolean hasAccess = isAdmin || isManagement;

        logger.debug("User priority: {} | Admin (10-29): {} | Management (50-99): {} | Access granted: {}",
                userPriority, isAdmin, isManagement, hasAccess);

        return hasAccess;
    }


    public boolean hasAdminOrManagementAccess() {
        return isAdminOrManagementOnly();
    }


    private Integer getCurrentUserPriority() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || !auth.isAuthenticated()) {
                logger.debug("No authenticated user found");
                return null;
            }

            if (!(auth.getPrincipal() instanceof UserPrincipal)) {
                logger.debug("Principal is not UserPrincipal: {}",
                        auth.getPrincipal().getClass().getSimpleName());
                return null;
            }

            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            Integer priority = principal.getHighestRolePriority();

            if (priority == null) {
                logger.debug("User has no priority assigned");
            }

            return priority;

        } catch (Exception e) {
            logger.error("Error getting user priority", e);
            return null;
        }
    }

    public boolean isAdmin() {
        Integer priority = getCurrentUserPriority();
        return priority != null &&
                priority >= PriorityRanges.ADMIN_MIN &&
                priority <= PriorityRanges.ADMIN_MAX;
    }

    public boolean isManagement() {
        Integer priority = getCurrentUserPriority();
        return priority != null &&
                priority >= PriorityRanges.MANAGEMENT_MIN &&
                priority <= PriorityRanges.MANAGEMENT_MAX;
    }
}