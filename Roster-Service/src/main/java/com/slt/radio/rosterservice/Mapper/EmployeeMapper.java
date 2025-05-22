package com.slt.radio.rosterservice.Mapper;

import com.slt.radio.rosterservice.Model.One.Dto.EmployeeDto;
import com.slt.radio.rosterservice.Model.One.Employeee.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {
    Employee toEntity(EmployeeDto dto);
    EmployeeDto toDto(Employee entity);
    List<EmployeeDto> toDtoList(List<Employee> entities);
    void updateEntityFromDto(EmployeeDto dto, @MappingTarget Employee entity);
}
