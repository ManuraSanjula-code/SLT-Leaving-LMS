package com.slt.peotv.lmsmangmentservice.controller;

import com.slt.peotv.lmsmangmentservice.model.res.DashBoardRes;
import com.slt.peotv.lmsmangmentservice.service.LMS_Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lms/dashboard")
public class DashBoard {

    @Autowired
    private LMS_Service lms_service;

    @GetMapping("/{userId}/{empId}")
    public DashBoardRes getDashBoard(@PathVariable String userId, @PathVariable String empId){
        return lms_service.getDashBoard(userId);
    }
}
