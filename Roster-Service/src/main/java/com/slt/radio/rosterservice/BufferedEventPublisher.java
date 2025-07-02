/* package com.slt.radio.rosterservice;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.slt.radio.rosterservice.Model.One.LMS.Attendance;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class BufferedEventPublisher {
    
    private final ConcurrentLinkedQueue<Attendance> eventBuffer = new ConcurrentLinkedQueue<>();
    private final JmsTemplate jmsTemplate;
    
    // Configurable properties
    private static final int BATCH_SIZE = 100;
    private static final long MAX_BUFFER_AGE_MS = 5000; // 5 seconds
    
    public BufferedEventPublisher(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }
    
    public void addToBuffer(Attendance attendance) {
        eventBuffer.add(attendance);
        checkBufferThreshold();
    }
    
    private void checkBufferThreshold() {
        if (eventBuffer.size() >= BATCH_SIZE) {
            flushBuffer();
        }
    }
    
    @Scheduled(fixedRate = MAX_BUFFER_AGE_MS)
    public void scheduledFlush() {
        if (!eventBuffer.isEmpty()) {
            flushBuffer();
        }
    }
    
    private void flushBuffer() {
        List<Attendance> batch = new ArrayList<>(BATCH_SIZE);
        synchronized (eventBuffer) {
            while (!eventBuffer.isEmpty() && batch.size() < BATCH_SIZE) {
                Attendance event = eventBuffer.poll();
                if (event != null) {
                    batch.add(event);
                }
            }
        }
        
        if (!batch.isEmpty()) {
            sendBatch(batch);
        }
    }
    
    

    private void sendBatch(List<Attendance> batch) {
        try {
            for (Attendance attendance : batch) {
                jmsTemplate.convertAndSend("roster.queue", attendance, message -> {
                    message.setStringProperty("_type", "com.slt.peotv.lmsmangmentservice.messaging.Attendance");
                    return message;
                });
            }
        } catch (Exception e) {
            eventBuffer.addAll(batch);
            throw e;
        }
    }
    
    public int getBufferSize() {
        return eventBuffer.size();
    }
} */