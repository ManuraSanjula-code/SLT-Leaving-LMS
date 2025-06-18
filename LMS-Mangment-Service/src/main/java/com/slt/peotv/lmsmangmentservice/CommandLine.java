package com.slt.peotv.lmsmangmentservice;

import com.nimbusds.jwt.SignedJWT;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.feign_client.UserClient;
import com.slt.peotv.lmsmangmentservice.feign_client.model.UserRest;
import com.slt.peotv.lmsmangmentservice.repository.EmployeeRepo;
import com.slt.peotv.lmsmangmentservice.security.TokenCreator;
import com.slt.peotv.lmsmangmentservice.service.AccessLogService;
import com.slt.peotv.lmsmangmentservice.service.Check_Service;
import com.slt.peotv.lmsmangmentservice.utils.service.LeaveManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class CommandLine implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CommandLineRunner.class);

    @Autowired
    private AccessLogService accessLogService;

    @Autowired
    private Check_Service checkService;

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private UserClient userClient;

    @Autowired
    private TokenCreator tokenCreator;

    @Autowired
    private LeaveManagementService leaveManagementService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Starting employee data initialization...");

        try {
            initializeEmployeeData();
            logger.info("Employee data initialization completed successfully.");
        } catch (Exception e) {
            logger.error("Error during employee data initialization: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void initializeEmployeeData() throws Exception {
        // Check if employees already exist in the database
        long employeeCount = employeeRepo.count();

        if (employeeCount > 0) {
            logger.info("Employees already exist in database. Count: {}", employeeCount);
            return;
        }

        logger.info("No employees found in database. Fetching from external service...");

        // Create authentication token
        SignedJWT signToken = tokenCreator.createSignedJWT("lms@slt.com");
        String token = "Bearer " + tokenCreator.encryptToken(signToken);

        // Fetch employees from external service
        List<UserRest> externalEmployees = userClient.getAllEmployee(token);

        if (externalEmployees == null || externalEmployees.isEmpty()) {
            logger.warn("No employees received from external service");
            return;
        }

        logger.info("Fetched {} employees from external service", externalEmployees.size());

        // Process and save employees
        processAndSaveEmployees(externalEmployees);
    }

    private void processAndSaveEmployees(List<UserRest> externalEmployees) {
        int successCount = 0;
        int errorCount = 0;

        for (UserRest userRest : externalEmployees) {
            try {
                EmployeeEntity employee = mapToEmployeeEntity(userRest);
                EmployeeEntity savedEmployee = employeeRepo.save(employee);

                // Allocate leaves for the new employee
                leaveManagementService.allocateLeaves(savedEmployee);

                successCount++;
                logger.debug("Successfully processed employee: {}", userRest.getEmployeeId());

            } catch (Exception e) {
                errorCount++;
                logger.error("Error processing employee {}: {}",
                        userRest != null ? userRest.getEmployeeId() : "unknown",
                        e.getMessage(), e);
            }
        }

        logger.info("Employee processing completed. Success: {}, Errors: {}", successCount, errorCount);
    }

    private EmployeeEntity mapToEmployeeEntity(UserRest userRest) {
        EmployeeEntity employee = new EmployeeEntity();
        employee.setEmail(userRest.getEmail());
        employee.setEmployeeId(userRest.getEmployeeId());
        employee.setPublicId(userRest.getUserId());
        employee.setProfilePic(userRest.getProfilePic());
        employee.setSltId(userRest.getSltId());
        employee.setFirstName(userRest.getFirstName());
        employee.setLastName(userRest.getLastName());
        employee.setJoin_date(userRest.getJoiningDate());
        employee.setRoaster(userRest.getRoaster());
        return employee;
    }
}