package com.slt.radio.rosterservice.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slt.radio.rosterservice.Model.Roster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

/**
 * Listener to audit MongoDB operations
 */
@Configuration
@Slf4j
public class MongoAuditingListener {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create a listener that logs document content before saving
     */
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
