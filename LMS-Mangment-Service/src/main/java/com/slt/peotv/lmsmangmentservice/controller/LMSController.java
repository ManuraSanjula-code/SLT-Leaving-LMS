package com.slt.peotv.lmsmangmentservice.controller;

import com.slt.peotv.lmsmangmentservice.model.LeaveReq;
import com.slt.peotv.lmsmangmentservice.model.MovementReq;
import com.slt.peotv.lmsmangmentservice.model.dto.*;
import com.slt.peotv.lmsmangmentservice.service.Check_Service;
import com.slt.peotv.lmsmangmentservice.service.LMS_Service;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lms")
public class LMSController {

    @Autowired
    public LMS_Service lmsService;

    @Autowired
    private Check_Service checkService;

    @GetMapping
    public Page<AttendanceDTO> getAllAttendance(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return lmsService.getAllAttendance(page, size);
    }

    @GetMapping("/un-successful")
    public Page<AttendanceDTO> getAllAttendanceThatUn(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return lmsService.getAllAttendanceThatUn(page, size);
    }

    @GetMapping("/un-authorized")
    public Page<AttendanceDTO> getAllAttendanceThatUnA(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return lmsService.getAllAttendanceThatUnA(page, size);
    }

    @GetMapping("/no-pay")
    public Page<NopayDTO> getAllNoPays(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return lmsService.getAllNoPays(page, size);
    }

    @GetMapping("/{userId}")
    public List<AttendanceDTO> getAttendanceByUserId(@PathVariable String userId) {
        return List.of();
    }

    @GetMapping("/leave/{userId}")
    public Page<LeaveDTO> getAllLeaveByUserId(@PathVariable String userId,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return lmsService.getAllLeaveByUserByEmployeeId(userId, page, size);
    }

    @GetMapping("/leave/all")
    public Page<LeaveDTO> getAllLeave(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return lmsService.getAllLeaves(page, size);
    }

    @GetMapping("/movement/{userId}")
    public Page<MovementDTO> getAllMovementByUserId(@PathVariable String userId,@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return lmsService.getAllMovementByUser(userId, page, size);
    }

    @GetMapping("/movement/all")
    public Page<MovementDTO> getAllMovement(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return lmsService.getAllMovements(page, size);
    }

    @GetMapping("/no-pay/{userId}")
    public Page<NopayDTO> getAllNopayByUserId(@PathVariable String userId,@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return lmsService.getAllNoPayByUser(userId, page, size);
    }

    @GetMapping("/nopay/all")
    public List<NopayDTO> getAllNopay() {
        return List.of();
    }

    @GetMapping("/in-out/{userId}")
    public Page<InOutDTO> getAllInOutByUserId(@PathVariable String userId,@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return checkService.getAllInOut(userId, page, size);
    }

    @PostMapping("/management/movement/create")
    public ResponseEntity<Void> manageMovement(@RequestBody MovementReq req) {
        checkService.requestMovement(req);
        return ResponseEntity.ok().build();
    }

    @RequestMapping("/management/leave/create/{userId}")
    public ResponseEntity<Void> manageLeave(@PathVariable String userId, @RequestBody LeaveReq req, HttpServletRequest request) {
        checkService.requestALeave(req, userId, userId, request);
        return ResponseEntity.ok().build();
    }
}
