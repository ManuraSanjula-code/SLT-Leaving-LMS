package com.slt.radio.rosterservice;

import com.nimbusds.jwt.SignedJWT;
import com.slt.radio.rosterservice.Model.One.Employeee.EmployeeArchive;
import com.slt.radio.rosterservice.Repo.EmployeeArchiveRepository;
import com.slt.radio.rosterservice.Utils.TokenCreator;
import com.slt.radio.rosterservice.feign_client.UserClient;
import com.slt.radio.rosterservice.feign_client.model.UserRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class CommandLine implements CommandLineRunner {

    @Autowired
    private EmployeeArchiveRepository employeeRepo;

    @Autowired
    private UserClient client;

    @Autowired
    private TokenCreator tokenCreator;

    @Value("${ROSTER_BEYOND_TwentyFour:false}")
    private boolean roster_beyond;

    @Override
    public void run(String... args) throws Exception {

        SignedJWT signToken = tokenCreator.createSignedJWT("lms@slt.com");
        String token = "Bearer " + tokenCreator.encryptToken(signToken);


        if(employeeRepo.findAll().isEmpty()){
            List<UserRest> allEmployee = client.getAllEmployee(token);
            allEmployee.forEach(user->{
                if(employeeRepo.findByUserId(user.getUserId()) == null){
                    EmployeeArchive employee = new EmployeeArchive();
                    employee.setEmail(user.getEmail());
                    employee.setEmployeeId(user.getEmployeeId());
                    employee.setUserId(user.getUserId());
                    employee.setSltId(user.getSltId());
                    employee.setFirstName(user.getFirstName());
                    employee.setLastName(user.getLastName());
                    employee.setRoaster(user.getRoaster());
                    employee.setJoiningDate(user.getJoiningDate());
                    employeeRepo.save(employee);
                }
            });
        }

        System.out.println(" ********** ROSTER_BEYOND_TwentyFour ************* " + roster_beyond);

        /*if(accessLogRepository.findAll().isEmpty()){
            List<AccessLogArchiveRest> allAccessLogsToday = lmsClient.getAllAccessLogsToday(helper.getFormattedYesterdayDate(), token);
            allAccessLogsToday.forEach(lms->{
                System.out.println(lms.toString());
                AccessLog accessLog = new AccessLog(lms);
                accessLogRepository.save(accessLog);
            });
        }*/
    }
}
