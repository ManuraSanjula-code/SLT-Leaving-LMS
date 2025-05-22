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
}