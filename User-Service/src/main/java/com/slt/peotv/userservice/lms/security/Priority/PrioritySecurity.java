package com.slt.peotv.userservice.lms.security.Priority;

import com.slt.peotv.userservice.lms.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("prioritySecurity")
public class PrioritySecurity {

    public boolean hasPriority(int requiredPriority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return userPrincipal.getHighestRolePriority() <= requiredPriority;
        }
        return false;
    }
    public boolean hasAnyPriority(int... priorities) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
            int userPriority = user.getHighestRolePriority();
            for (int priority : priorities) {
                if (userPriority <= priority) {
                    return true;
                }
            }
        }
        return false;
    }
}