/* package com.slt.radio.rosterservice;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.slt.radio.rosterservice.Model.One.LMS.Attendance;
import java.util.List;

@Component
public class AttendanceBatchConsumer {
    
    @JmsListener(destination = "roster.queue", containerFactory = "batchListenerContainerFactory")
    @Transactional
    public void processBatch(List<Attendance> batch) {
        batch.forEach(attendance -> {
            System.out.println("Processing attendance: " + attendance.getId());
        });
    }
} */