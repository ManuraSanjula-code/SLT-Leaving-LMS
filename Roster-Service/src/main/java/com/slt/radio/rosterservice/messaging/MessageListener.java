package com.slt.radio.rosterservice.messaging;

import com.slt.radio.rosterservice.documents.one.employeee.EmployeeArchive;
import com.slt.radio.rosterservice.repo.EmployeeArchiveRepository;
import javax.jms.JMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Stream;

@Component
public class MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

    @Autowired
    private EmployeeArchiveRepository employeeRepo;

    @Transactional
    @JmsListener(destination = "user.queue.roster")
    public void receiveMessage(@Payload LMSUser message) throws JMSException {
        if (message == null) {
            logger.warn("Received null message");
            return;
        }

        try {
            logger.debug("Processing message for employee: {}", message.getEmployeeId());

            EmployeeArchive employeeEntity = Stream.of(
                            employeeRepo.findBySltId(message.getSltId()),
                            employeeRepo.findByEmployeeId(message.getEmployeeId()),
                            employeeRepo.findByEmail(message.getEmail())
                    )
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .orElseGet(() -> {
                        EmployeeArchive newEmployee = new EmployeeArchive();
                        newEmployee.setActive(1);
                        return newEmployee;
                    });

            updateEmployeeEntityFromMessage(employeeEntity, message);
            employeeRepo.save(employeeEntity);
            
            logger.info("Successfully processed message for employee: {}", message.getEmployeeId());
        } catch (Exception e) {
            logger.error("Error processing message for employee: {}", message.getEmployeeId(), e);
            throw e;
        }
    }

    private void updateEmployeeEntityFromMessage(EmployeeArchive employeeEntity, LMSUser message) {
        if (message.getEmployeeId() != null && !message.getEmployeeId().isEmpty()) {
            employeeEntity.setEmployeeId(message.getEmployeeId());
        }
        if (message.getSltId() != null && !message.getSltId().isEmpty()) {
            employeeEntity.setSltId(message.getSltId());
        }
        if (message.getFirstName() != null && !message.getFirstName().isEmpty()) {
            employeeEntity.setFirstName(message.getFirstName());
        }
        if (message.getLastName() != null && !message.getLastName().isEmpty()) {
            employeeEntity.setLastName(message.getLastName());
        }
        if (message.getEmail() != null && !message.getEmail().isEmpty()) {
            employeeEntity.setEmail(message.getEmail());
        }
        if (message.getJoin_date() != null) {
            employeeEntity.setJoiningDate(message.getJoin_date());
        }
        if (message.getRoaster() != null) {
            employeeEntity.setRoaster(message.getRoaster());
        }
        if (message.getGender() != null && !message.getGender().isEmpty()) {
            employeeEntity.setGender(message.getGender());
        }
        if (message.getPublicId() != null && !message.getPublicId().isEmpty()) {
            employeeEntity.setUserId(message.getPublicId());
        }
    }
}