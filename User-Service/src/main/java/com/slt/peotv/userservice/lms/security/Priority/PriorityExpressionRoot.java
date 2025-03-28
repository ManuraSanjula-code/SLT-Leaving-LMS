package com.slt.peotv.userservice.lms.security.Priority;

import com.slt.peotv.userservice.lms.security.UserPrincipal;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class PriorityExpressionRoot extends SecurityExpressionRoot
        implements MethodSecurityExpressionOperations {

    private Object filterObject;
    private Object returnObject;
    private Object target;

    public PriorityExpressionRoot(Authentication authentication) {
        super(authentication);
    }

    // Your custom methods
    public boolean hasPriority(int requiredPriority) {
        if (getAuthentication().getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) getAuthentication().getPrincipal();
            return userPrincipal.getHighestRolePriority() <= requiredPriority;
        }
        return false;
    }
    public boolean hasAnyPriority(int... priorities) {
        if (getAuthentication().getPrincipal() instanceof UserPrincipal) {
            UserPrincipal user = (UserPrincipal) getAuthentication().getPrincipal();
            int userPriority = user.getHighestRolePriority();

            for (int priority : priorities) {
                if (userPriority <= priority) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasAuthorityWithWeight(String authority, int requiredWeight) {
        if (getAuthentication().getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) getAuthentication().getPrincipal();
            Integer userWeight = userPrincipal.getAuthorityWeights().get(authority);
            return userWeight != null && userWeight >= requiredWeight;
        }
        return false;
    }

    // Required MethodSecurityExpressionOperations methods
    @Override
    public void setFilterObject(Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return this.filterObject;
    }

    @Override
    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
        return this.returnObject;
    }

    @Override
    public Object getThis() {
        return this.target;
    }

    public void setThis(Object target) {
        this.target = target;
    }
}