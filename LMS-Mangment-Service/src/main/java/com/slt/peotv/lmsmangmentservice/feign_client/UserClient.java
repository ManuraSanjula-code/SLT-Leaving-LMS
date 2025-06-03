package com.slt.peotv.lmsmangmentservice.feign_client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.slt.peotv.lmsmangmentservice.feign_client.model.UserRest;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/users/{userid}")
    @Retry(name="user-service")
    @CircuitBreaker(name="user-service", fallbackMethod="getCustomerByIdFallback")
    UserRest getEmployeeById(@PathVariable("userid") String userid, @RequestHeader("Authorization") String token);

    default UserRest getCustomerByIdFallback(String userid, String token, Throwable exception){
        UserRest userRest = new UserRest();
        userRest.setUserId("access-log");
        userRest.setEmployeeId("lms@slt.com");
        userRest.setSltId("lms@slt.com");
        userRest.setEmail("lms@slt.com");
        userRest.setFirstName("PEOTV");
        return userRest;
    }

    @GetMapping("/users/{userid}/admins")
    @Retry(name="user-service")
    @CircuitBreaker(name="user-service")
    List<UserRest> getEmployeeAdmins(@PathVariable("userid") String userid, @RequestHeader("Authorization") String token);

    @GetMapping("/users/lms")
    @Retry(name="user-service")
    @CircuitBreaker(name="user-service")
    List<UserRest> getAllEmployee(@RequestHeader("Authorization") String token);
}