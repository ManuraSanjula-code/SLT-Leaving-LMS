package com.slt.radio.rosterservice.Service.Employee;

import com.slt.radio.rosterservice.Model.Obj.EmployeeShift;
import com.slt.radio.rosterservice.Model.Dto.EmployeeShiftDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for managing EmployeeShift objects
 * This is a new service to specifically handle mapping of EmployeeShiftDto to EmployeeShift
 * to ensure all fields are properly transferred
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeShiftService {

    /**
     * Maps EmployeeShiftDto to EmployeeShift entity with logging
     * @param dto the dto to map
     * @return the mapped entity
     */
    public EmployeeShift mapDtoToEntity(EmployeeShiftDto dto) {
        log.info("Mapping EmployeeShiftDto to entity: employeeId={}, totalShift={}, rotShift={}, offDay={}, dDuty={}",
                dto.getEmployeeId(), dto.getTotalShift(), dto.getRotShift(), dto.getOffDay(), dto.getDDuty());

        EmployeeShift entity = EmployeeShift.builder()
                .employeeId(dto.getEmployeeId())
                .totalShift(dto.getTotalShift())
                .rotShift(dto.getRotShift())
                .offDay(dto.getOffDay())
                .dDuty(dto.getDDuty())
                .build();

        log.info("Created EmployeeShift entity: employeeId={}, totalShift={}, rotShift={}, offDay={}, dDuty={}",
                entity.getEmployeeId(), entity.getTotalShift(), entity.getRotShift(), entity.getOffDay(), entity.getDDuty());

        return entity;
    }
}