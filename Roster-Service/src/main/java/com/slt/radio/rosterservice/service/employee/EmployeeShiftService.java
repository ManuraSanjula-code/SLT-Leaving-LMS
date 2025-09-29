package com.slt.radio.rosterservice.service.employee;

import com.slt.radio.rosterservice.documents.one.obj.EmployeeShift;
import com.slt.radio.rosterservice.models.dto.EmployeeShiftDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service

public class EmployeeShiftService {
    private static final Logger log = LoggerFactory.getLogger(EmployeeShiftService.class);

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