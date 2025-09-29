package com.slt.radio.rosterservice;

import com.nimbusds.jwt.SignedJWT;
import com.slt.radio.rosterservice.documents.one.employeee.EmployeeArchive;
import com.slt.radio.rosterservice.repo.EmployeeArchiveRepository;
import com.slt.radio.rosterservice.service.employee.EmployeeShiftService;
import com.slt.radio.rosterservice.utils.TokenCreator;
import com.slt.radio.rosterservice.feign_client.UserClient;
import com.slt.radio.rosterservice.feign_client.model.UserRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class CommandLine implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(CommandLine.class);

    private final EmployeeArchiveRepository employeeRepo;
    private final UserClient client;
    private final TokenCreator tokenCreator;

    @Value("${ROSTER_BEYOND_TwentyFour:false}")
    private boolean roster_beyond;

    @Autowired
    public CommandLine(EmployeeArchiveRepository employeeRepo, 
                     UserClient client, 
                     TokenCreator tokenCreator) {
        this.employeeRepo = employeeRepo;
        this.client = client;
        this.tokenCreator = tokenCreator;
    }

    @Override
    public void run(String... args) {
        try {
            initializeEmployeeData();
            logConfiguration();
        } catch (Exception e) {
            log.error("Error during application startup: {}", e.getMessage(), e);
        }
    }

    private void initializeEmployeeData() throws Exception {
        if (employeeRepo.findAll().isEmpty()) {
            log.info("Initializing employee data as database is empty");
            
            SignedJWT signToken = tokenCreator.createSignedJWT("lms@slt.com");
            String token = "Bearer " + tokenCreator.encryptToken(signToken);

            List<UserRest> allEmployee = Collections.emptyList();
            try {
                allEmployee = client.getAllEmployee(token);
                log.info("Successfully fetched {} employees from user service", allEmployee.size());
            } catch (Exception e) {
                log.error("Failed to fetch employees from user service. Continuing with empty list. Error: {}", 
                         e.getMessage());
            }

            for (UserRest user : allEmployee) {
                try {
                    if (employeeRepo.findByUserId(user.getUserId()) == null) {
                        EmployeeArchive employee = createEmployeeFromUser(user);
                        employeeRepo.save(employee);
                        log.debug("Saved employee with ID: {}", user.getEmployeeId());
                    }
                } catch (Exception e) {
                    log.error("Failed to process employee with ID: {}. Error: {}", 
                             user.getEmployeeId(), e.getMessage());
                }
            }
        } else {
            log.info("Employee data already exists in database, skipping initialization");
        }
    }

    private EmployeeArchive createEmployeeFromUser(UserRest user) {
        EmployeeArchive employee = new EmployeeArchive();
        employee.setEmail(user.getEmail());
        employee.setEmployeeId(user.getEmployeeId());
        employee.setUserId(user.getUserId());
        employee.setSltId(user.getSltId());
        employee.setFirstName(user.getFirstName());
        employee.setLastName(user.getLastName());
        employee.setRoaster(user.getRoaster());
        employee.setJoiningDate(user.getJoiningDate());
        return employee;
    }

    private void logConfiguration() {
        log.info("********** ROSTER_BEYOND_TwentyFour Configuration *************: {}", roster_beyond);
    }
}