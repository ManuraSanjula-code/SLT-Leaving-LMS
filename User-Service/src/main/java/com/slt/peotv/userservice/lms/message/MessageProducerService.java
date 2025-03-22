package com.slt.peotv.userservice.lms.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageProducerService {

    @Autowired
    private JmsTemplate jmsTemplate;

    public void sendMessage(String destination, String message) {
        jmsTemplate.convertAndSend(destination, message);
    }
    public void sendMessage(String destination, LMSUser lmsUser) {
        jmsTemplate.convertAndSend(destination, lmsUser, message -> {
            message.setStringProperty("_type", "com.slt.peotv.lmsmangmentservice.messaging.LMSUser");
            return message;
        });
    }
}
