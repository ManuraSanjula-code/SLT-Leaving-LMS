package com.slt.peotv.lmsmangmentservice.messaging;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.repository.EmployeeRepo;
import jakarta.jms.JMSException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {

    @Autowired
    private EmployeeRepo employeeRepo;

    @JmsListener(destination = "user.queue")
    public void receiveMessage(@Payload LMSUser message) throws JMSException {

        EmployeeEntity employeeEntity = new EmployeeEntity();
        employeeEntity.setFirstName(message.getFirstName());
        employeeEntity.setLastName(message.getLastName());
        employeeEntity.setEmail(message.getEmail());
        employeeEntity.setEmployeeId(message.getEmployeeId());
        employeeEntity.setSltId(message.getSltId());

        EmployeeEntity save = employeeRepo.save(employeeEntity);
    }
}