package com.slt.peotv.userservice.lms.utils;

import com.slt.peotv.userservice.lms.entity.RoleEntity;
import com.slt.peotv.userservice.lms.repository.RoleRepository;
import com.slt.peotv.userservice.lms.shared.dto.RoleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

@Component
public class RolePriorityValidator implements Validator {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return RoleDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RoleDTO roleDto = (RoleDTO) target;

        // Check priority range
        if (roleDto.getPriority() != 0) {
            if (roleDto.getPriority() < 1 || roleDto.getPriority() > 1000) {
                errors.rejectValue("priority", "priority.range",
                        "Priority must be between 1 and 1000");
            }
        }

        if (roleDto.getId() == 0) { // New role
            List<RoleEntity> existingRoles = (List<RoleEntity>) roleRepository.findAll();

            existingRoles.stream()
                    .filter(r -> Math.abs(r.getPriority() - roleDto.getPriority()) < 5)
                    .findFirst()
                    .ifPresent(conflictingRole -> {
                        errors.rejectValue("priority", "priority.gap",
                                String.format("Priority must be at least 5 apart from %s (priority %d)",
                                        conflictingRole.getName(),
                                        conflictingRole.getPriority()));
                    });
        }
    }
}