package com.slt.radio.rosterservice.service.employee;


import com.slt.radio.rosterservice.document.one.employeee.Employee;
import com.slt.radio.rosterservice.exception.ResourceAlreadyExistsException;
import com.slt.radio.rosterservice.exception.ResourceNotFoundException;
import com.slt.radio.rosterservice.mapper.EmployeeMapper;
import com.slt.radio.rosterservice.model.dto.EmployeeDto;
import com.slt.radio.rosterservice.repo.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    public List<EmployeeDto> getAllEmployees() {
        return employeeMapper.toDtoList(employeeRepository.findAll());
    }

    public EmployeeDto getEmployeeById(String id) {
        return employeeMapper.toDto(findEmployeeById(id));
    }

    public EmployeeDto getEmployeeByEmployeeId(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId)
                .map(employeeMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with employeeId: " + employeeId));
    }

    public EmployeeDto createEmployee(EmployeeDto employeeDto) {
        if (employeeRepository.existsByEmployeeId(employeeDto.getEmployeeId())) {
            throw new ResourceAlreadyExistsException("Employee already exists with employeeId: " + employeeDto.getEmployeeId());
        }

        Employee employee = employeeMapper.toEntity(employeeDto);
        employee.setActive(true);
        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    public EmployeeDto updateEmployee(String id, EmployeeDto employeeDto) {
        Employee employee = findEmployeeById(id);

        // Check if employeeId is being changed and if it already exists
        if (!employee.getEmployeeId().equals(employeeDto.getEmployeeId()) &&
                employeeRepository.existsByEmployeeId(employeeDto.getEmployeeId())) {
            throw new ResourceAlreadyExistsException("Employee already exists with employeeId: " + employeeDto.getEmployeeId());
        }

        employeeMapper.updateEntityFromDto(employeeDto, employee);
        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    public void deleteEmployee(String id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(id);
    }

    public EmployeeDto deactivateEmployee(String id) {
        Employee employee = findEmployeeById(id);
        employee.setActive(false);
        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    public EmployeeDto activateEmployee(String id) {
        Employee employee = findEmployeeById(id);
        employee.setActive(true);
        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    private Employee findEmployeeById(String id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }
}
