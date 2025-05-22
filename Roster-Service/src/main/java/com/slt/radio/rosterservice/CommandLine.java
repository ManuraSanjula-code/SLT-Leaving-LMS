package com.slt.radio.rosterservice;

import com.nimbusds.jwt.SignedJWT;
import com.slt.radio.rosterservice.Model.One.Employeee.EmployeeArchive;
import com.slt.radio.rosterservice.Model.One.LMS.AccessLog;
import com.slt.radio.rosterservice.Repo.AccessLogRepository;
import com.slt.radio.rosterservice.Repo.EmployeeArchiveRepository;
import com.slt.radio.rosterservice.Utils.TokenCreator;
import com.slt.radio.rosterservice.feign_client.LMSClient;
import com.slt.radio.rosterservice.feign_client.UserClient;
import com.slt.radio.rosterservice.feign_client.model.AccessLogArchiveRest;
import com.slt.radio.rosterservice.feign_client.model.UserRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class CommandLine implements CommandLineRunner {

    @Autowired
    private EmployeeArchiveRepository employeeRepo;

    @Autowired
    private UserClient client;

    @Autowired
    private LMSClient lmsClient;

    @Autowired
    private TokenCreator tokenCreator;

    @Autowired
    private AccessLogRepository accessLogRepository;

    public Date getYesterdayDate() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date stripTimeFromDate(Date dateWithTime) {
        if (dateWithTime == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateWithTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public String formatDateToString(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }

    public String getFormattedYesterdayDate() {
        Date yesterday = getYesterdayDate();
        Date yesterdayWithoutTime = stripTimeFromDate(yesterday);
        return formatDateToString(yesterdayWithoutTime);
    }

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
                    employee.setJoiningDate(user.getJoiningDate());
                    employeeRepo.save(employee);
                }
            });
        }

        if(accessLogRepository.findAll().isEmpty()){
            List<AccessLogArchiveRest> allAccessLogsToday = lmsClient.getAllAccessLogsToday("31/12/2024", token);
            allAccessLogsToday.forEach(lms->{
                System.out.println(lms.toString());
                AccessLog accessLog = new AccessLog(lms);
                accessLogRepository.save(accessLog);
            });
        }

        /// Check all the missing dates and get all of them also get today day and compare to
    }
}
