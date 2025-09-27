package com.slt.radio.rosterservice.feign_client;

import com.slt.radio.rosterservice.feign_client.model.AccessLogArchiveRest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

@FeignClient(name = "lMS-management-service")
public interface LMSClient {

    @GetMapping("/lms/access-log")
    @Retry(name="lMS-management-service")
    @CircuitBreaker(name="lMS-management-service", fallbackMethod="getLms")
    List<AccessLogArchiveRest> getAllAccessLogsToday(@RequestParam("date") String date, @RequestHeader("Authorization") String token);

    default List<AccessLogArchiveRest> getLms(String date, String token, Throwable exception){
        AccessLogArchiveRest accessLogArchiveRest = new AccessLogArchiveRest();
        accessLogArchiveRest.setEmployeeId("A0000");
        return Arrays.asList(accessLogArchiveRest);
    }

}