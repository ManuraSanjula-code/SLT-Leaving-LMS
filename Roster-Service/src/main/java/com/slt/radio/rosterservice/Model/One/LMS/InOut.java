package com.slt.radio.rosterservice.Model.One.LMS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "in_outs")
@CompoundIndexes({
        @CompoundIndex(
                name = "unique_inout_record",
                def = "{'employeeID': 1, 'date': 1, 'punchInMoa': 1, 'punchInEv': 1}",
                unique = true
        )
})
public class InOut {
    @Id
    private String id;

    private String employeeID;
    private String terminalID;
    private Date date;
    private Date punchInMoa; // earliest morning time -- date
    private Date punchInEv; // earliest evening time -- date

    private String timeMoa; // earliest morning time -- time (as string)
    private String timeEve; // earliest evening time -- time (as string)

    @Builder.Default
    private Integer inOut = 0;

    @Builder.Default
    private Boolean isMorning = false;

    @Builder.Default
    private Boolean isEvening = false;

    @Builder.Default
    private Boolean isPast = false;
}