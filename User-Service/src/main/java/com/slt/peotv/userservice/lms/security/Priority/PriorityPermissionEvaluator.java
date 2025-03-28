package com.slt.peotv.userservice.lms.security.Priority;

import com.slt.peotv.userservice.lms.entity.UserEntity;
import com.slt.peotv.userservice.lms.repository.UserRepository;
import com.slt.peotv.userservice.lms.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

@Component
public class PriorityPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean hasPermission(
            Authentication authentication,
            Object targetDomainObject,
            Object permission) {

        // 1. Handle unauthenticated users
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // 2. Handle UserPrincipal
        if (authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            // 3. Check if target is a domain object we support
            if (targetDomainObject instanceof UserEntity) {
                return checkUserPermission_(userPrincipal, (UserEntity) targetDomainObject, permission.toString());
            }

            // Add other domain object types here as needed
            // else if (targetDomainObject instanceof OtherEntity) {...}
        }

        return false;
    }

    @Override
    public boolean hasPermission(
            Authentication authentication,
            Serializable targetId,
            String targetType,
            Object permission) {

        // 1. Handle unauthenticated users
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // 2. Resolve the actual domain object
        Object targetDomainObject = resolveTargetDomainObject(targetId, targetType);
        if (targetDomainObject != null) {
            return hasPermission(authentication, targetDomainObject, permission);
        }

        return false;
    }

    private boolean checkUserPermission(UserPrincipal userPrincipal, UserEntity targetUser, String permission) {
        // 1. Get the current user's highest priority and authorities
        int userPriority = userPrincipal.getHighestRolePriority();
        Map<String, Integer> authorities = userPrincipal.getAuthorityWeights();

        // 2. Permission logic based on permission string
        switch (permission.toUpperCase()) {
            case "READ":
                // Users can read their own profile or if they have READ_AUTHORITY authority
                return (targetUser.getId() == userPrincipal.getId()) ||
                        (authorities.containsKey("READ_AUTHORITY") && authorities.get("READ_AUTHORITY") >= 1);

            case "UPDATE":
                // Admins can update any user, others only their own profile
                return userPriority <= 10 || // ADMIN priority or better
                        ((targetUser.getId() == userPrincipal.getId()) &&
                                authorities.containsKey("WRITE_AUTHORITY") &&
                                authorities.get("WRITE_AUTHORITY") >= 2);

            case "DELETE":
                // Only admins with sufficient authority weight can delete
                return userPriority <= 10 &&
                        authorities.containsKey("DELETE_AUTHORITY") &&
                        authorities.get("DELETE_AUTHORITY") >= 3;
            default:
                return false;
        }
    }

    private boolean checkUserPermission_(UserPrincipal userPrincipal, UserEntity targetUser, String permission) {
        return true;
    }

    private Object resolveTargetDomainObject(Serializable targetId, String targetType) {
        try {
            if ("User".equalsIgnoreCase(targetType)) {
                return userRepository.findById((Long) targetId).orElse(null);
            }
            // Add other domain types as needed
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}