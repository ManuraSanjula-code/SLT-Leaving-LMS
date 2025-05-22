package com.slt.radio.rosterservice.Mapper.Impl;

import com.slt.radio.rosterservice.Mapper.EmployeeMapper;
import com.slt.radio.rosterservice.Model.One.Dto.EmployeeDto;
import com.slt.radio.rosterservice.Model.One.Employeee.Employee;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class EmployeeMapperImpl implements EmployeeMapper {

    @Override
    public Employee toEntity(EmployeeDto dto) {
        if (dto == null) {
            return null;
        }
        int yearFromLocalDate = LocalDate.now().getYear();

        Map<String, Map<String, String>> shiftCodeMap = new HashMap<>();
        Map<String, String> teams = new HashMap<>();

        Employee employee = new Employee();
        employee.setId(dto.getId());
        employee.setTeamId(dto.getTeamId());
        employee.setEmployeeId(dto.getEmployeeId());
        employee.setName(dto.getName());
        employee.setMobileNo(dto.getMobileNo());
        employee.setShortName(dto.getShortName());
        employee.setActive(dto.isActive());

        String year = String.valueOf(yearFromLocalDate);
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
        String currentMonth = monthFormat.format(new Date());

        teams.put(currentMonth, dto.getTeamId());
        shiftCodeMap.put(year, teams);

        employee.setShiftCodeMap(shiftCodeMap);

        return employee;
    }

    @Override
    public EmployeeDto toDto(Employee entity) {
        if (entity == null) {
            return null;
        }

        EmployeeDto dto = new EmployeeDto();
        dto.setId(entity.getId());
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setName(entity.getName());
        dto.setMobileNo(entity.getMobileNo());
        dto.setShortName(entity.getShortName());
        dto.setActive(entity.isActive());

        return dto;
    }

    @Override
    public List<EmployeeDto> toDtoList(List<Employee> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(EmployeeDto dto, Employee entity) {
        if (dto == null) {
            return;
        }

        if (dto.getEmployeeId() != null) {
            entity.setEmployeeId(dto.getEmployeeId());
        }
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getMobileNo() != null) {
            entity.setMobileNo(dto.getMobileNo());
        }
        if (dto.getShortName() != null) {
            entity.setShortName(dto.getShortName());
        }
        entity.setActive(dto.isActive());
    }
}