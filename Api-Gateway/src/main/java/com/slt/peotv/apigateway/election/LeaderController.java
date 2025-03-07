package com.slt.peotv.apigateway.election;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leader")
public class LeaderController {

    private final LeaderElection leaderElection;

    public LeaderController(LeaderElection leaderElection) {
        this.leaderElection = leaderElection;
    }

    @GetMapping("/status")
    public String checkLeaderStatus() {
        return "Leader status checked!";
    }
}