package com.slt.peotv.lmsmangmentservice.entity.card;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Time;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "InOut")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InOutEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String employeeID;
    private Date date;
    private Date punchInMoa; // earliest moaning time -- date
    private Date punchInEv; // earliest eve time -- date

    private Time timeMoa; // earliest moaning time -- time
    private Time timeEve;// earliest eve time -- time

    @Builder.Default
    private Integer InOut = 0;

    @Builder.Default
    private Boolean isMoaning = false;

    @Builder.Default
    private Boolean isEvening = false;
    @Builder.Default
    private Boolean isPast = false; /// CHECK IN PAST DATA (THERE ARE SOME TIMES DATA IN PAST DATA NOT SAVED IN OUR LOCAL-DB ) BUT SOME HOW
                                    /// IN TOMARROW DATA BECAME VALID THEN I PROCESS THE DATA MAKE IT IS PAST IS TRUE
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        InOutEntity that = (InOutEntity) o;
        return Objects.equals(employeeID, that.employeeID);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(employeeID);
    }
}
