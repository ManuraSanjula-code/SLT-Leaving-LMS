/* package com.slt.radio.rosterservice;

import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.stereotype.Component;
import com.slt.radio.rosterservice.Model.One.LMS.Attendance;

@Component
public class MongoEventListener extends AbstractMongoEventListener<Attendance> {
    
    private final BufferedEventPublisher bufferedEventPublisher;
    
    public MongoEventListener(BufferedEventPublisher bufferedEventPublisher) {
        this.bufferedEventPublisher = bufferedEventPublisher;
    }
    
    @Override
    public void onAfterSave(AfterSaveEvent<Attendance> event) {
        bufferedEventPublisher.addToBuffer(event.getSource());
    }
} */