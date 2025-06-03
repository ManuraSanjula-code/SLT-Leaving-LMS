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

import java.util.ArrayList;
import java.util.List;

@Component
public class CommandLine implements CommandLineRunner {

    @Autowired
    private AccessLogService accessLogService;

    @Autowired
    private Check_Service checkService;

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private UserClient client;
    
    @Autowired
    private TokenCreator tokenCreator;

    @Autowired
    private LeaveManagementService leaveManagementService;

    @Override
    public void run(String... args) throws Exception {
        List<EmployeeEntity> employeeEntities = (List<EmployeeEntity>) employeeRepo.findAll();

        if(employeeEntities == null) employeeEntities = new ArrayList<>();

        if(!employeeEntities.isEmpty()){
            /*accessLogService.main();
            checkService.main();*/
        }
        List<EmployeeEntity> employeeRepoAll = (List<EmployeeEntity>) employeeRepo.findAll();

        if(employeeRepoAll.isEmpty()){
            SignedJWT signToken = tokenCreator.createSignedJWT("lms@slt.com");
            String token = "Bearer " + tokenCreator.encryptToken(signToken);
            List<UserRest> allEmployee = client.getAllEmployee(token);
            allEmployee.forEach(user->{
                EmployeeEntity employee = new EmployeeEntity();
                employee.setEmail(user.getEmail());
                employee.setEmployeeId(user.getEmployeeId());
                employee.setPublicId(user.getUserId());
                employee.setSltId(user.getSltId());
                employee.setFirstName(user.getFirstName());
                employee.setLastName(user.getLastName());
                employee.setJoin_date(user.getJoiningDate());
                employee.setRoaster(user.getRoaster());
                EmployeeEntity employeeEntity = employeeRepo.save(employee);

                leaveManagementService.allocateLeaves(employeeEntity);

            });
        }

    }
}
