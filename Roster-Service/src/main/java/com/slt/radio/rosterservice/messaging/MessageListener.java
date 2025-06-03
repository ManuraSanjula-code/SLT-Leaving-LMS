package com.slt.radio.rosterservice.messaging;


import com.slt.radio.rosterservice.Model.One.Employeee.Employee;
import com.slt.radio.rosterservice.Model.One.Employeee.EmployeeArchive;
import com.slt.radio.rosterservice.Repo.EmployeeArchiveRepository;
import com.slt.radio.rosterservice.Repo.EmployeeRepository;
import jakarta.jms.JMSException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {

    @Autowired
    private EmployeeArchiveRepository employeeRepo;


    @JmsListener(destination = "user.queue.roster")
    public void receiveMessage(@Payload LMSUser message) throws JMSException {

        EmployeeArchive employeeEntity = employeeRepo.findByEmail(message.getEmail())
                .orElse(new EmployeeArchive());

        updateEmployeeEntityFromMessage(employeeEntity, message);
        employeeEntity = employeeRepo.save(employeeEntity);
    }

    private void updateEmployeeEntityFromMessage(EmployeeArchive employeeEntity, LMSUser message) {
        employeeEntity.setFirstName(message.getFirstName());
        employeeEntity.setLastName(message.getLastName());
        employeeEntity.setEmail(message.getEmail());
        employeeEntity.setEmployeeId(message.getEmployeeId());
        employeeEntity.setSltId(message.getSltId());
        employeeEntity.setRoaster(message.getRoaster());
        employeeEntity.setJoiningDate(message.getJoin_date());
        employeeEntity.setRoaster(message.getRoaster());
    }
}