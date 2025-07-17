package com.slt.radio.rosterservice.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import com.slt.radio.rosterservice.Model.Enum.AttendanceType;

@Service
public class MessageProducerService {

    @Autowired
    private JmsTemplate jmsTemplate;

    public void sendMessage(String destination, String message) {
        jmsTemplate.convertAndSend(destination, message);
    }
    
    public void sendMessage(String destination, AttendanceJSM attendance) {
        jmsTemplate.convertAndSend(destination, attendance, message -> {
            message.setStringProperty("_type", "com.slt.peotv.lmsmangmentservice.messaging.AttendanceJSM");
            return message;
        });
    }
}
