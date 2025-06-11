package com.slt.peotv.lmsmangmentservice.messaging;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.repository.EmployeeRepo;
import com.slt.peotv.lmsmangmentservice.utils.service.LeaveManagementService;

import jakarta.jms.JMSException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {

    @Autowired
    private EmployeeRepo employeeRepo;
    
    @Autowired
    private LeaveManagementService leaveManagementService;

    @JmsListener(destination = "user.queue")
    public void receiveMessage(@Payload LMSUser message) throws JMSException {
        EmployeeEntity employeeEntity = employeeRepo.findByEmail(message.getEmail())
                .orElse(new EmployeeEntity());

        updateEmployeeEntityFromMessage(employeeEntity, message);
        employeeEntity = employeeRepo.save(employeeEntity);
        leaveManagementService.allocateLeaves(employeeEntity);
    }

    private void updateEmployeeEntityFromMessage(EmployeeEntity employeeEntity, LMSUser message) {
        employeeEntity.setFirstName(message.getFirstName());
        employeeEntity.setLastName(message.getLastName());
        employeeEntity.setEmail(message.getEmail());
        employeeEntity.setEmployeeId(message.getEmployeeId());
        employeeEntity.setSltId(message.getSltId());
        employeeEntity.setJoin_date(message.getJoin_date());
        employeeEntity.setPublicId(message.getPublicId());
        employeeEntity.setRoaster(message.getRoaster());
    }
}