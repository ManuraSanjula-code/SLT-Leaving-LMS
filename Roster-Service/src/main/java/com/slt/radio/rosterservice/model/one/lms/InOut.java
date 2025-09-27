package com.slt.radio.rosterservice.model.one.lms;

import com.slt.radio.rosterservice.model.enums.InOutType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "in_outs")
@CompoundIndexes({
        @CompoundIndex(
                name = "unique_inout_record",
                def = "{'employeeID': 1, 'punchTime': 1, 'punchInMoa': 1, 'punchInEv': 1}",
                unique = true
        )
})
@ToString
public class InOut {
    @Id
    private String id;
    private String employeeId;

    private Date date;
    private Date punchTime;
    private LocalTime punchTypeTime;

    private String terminalId;

    @Builder.Default
    private Integer inOutValue = -1;

    @Builder.Default
    private Boolean isManual = false;

    private InOutType inOutType;

    private Date createdDate = new Date();
    private Date updatedDate;
    private Boolean isActive = true;
    private Date etlRunTime;
}