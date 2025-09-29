package com.slt.radio.rosterservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slt.radio.rosterservice.documents.one.Roster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

@Configuration
public class MongoAuditingListener {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(MongoAuditingListener.class);

    @Bean
    public AbstractMongoEventListener<Roster> rosterMongoEventListener() {
        return new AbstractMongoEventListener<Roster>() {
            @Override
            public void onBeforeConvert(BeforeConvertEvent<Roster> event) {
                Roster roster = event.getSource();

                log.info("Saving Roster document with ID: {}, Month: {}, Year: {}",
                        roster.getId(), roster.getMonth(), roster.getYear());

                if (roster.getTeams() != null) {
                    roster.getTeams().forEach(team -> {
                        log.info("Team: {}", team.getTeamId());

                        if (team.getEmployees() != null) {
                            team.getEmployees().forEach(emp -> {
                                try {
                                    // Convert to string to see complete object
                                    String empJson = objectMapper.writeValueAsString(emp);
                                    log.info("Employee data being saved: {}", empJson);
                                } catch (Exception e) {
                                    log.info("Employee: {}, totalShift: {}, rotShift: {}, offDay: {}, dDuty: {}",
                                            emp.getEmployeeId(), emp.getTotalShift(), emp.getRotShift(),
                                            emp.getOffDay(), emp.getDDuty());
                                }
                            });
                        }
                    });
                }
            }
        };
    }
}
