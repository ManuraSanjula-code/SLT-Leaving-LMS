package com.slt.peotv.lmsmangmentservice.service.impl;

import com.slt.peotv.lmsmangmentservice.entity.AccessLog.AccessLogEntity;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import com.slt.peotv.lmsmangmentservice.repository.AccessLogRepo;
import com.slt.peotv.lmsmangmentservice.repository.InOutRepo;
import com.slt.peotv.lmsmangmentservice.service.AccessLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AccessLogServiceImpl implements AccessLogService {

    @Autowired
    private AccessLogRepo accessLogRepository;
    @Autowired
    private InOutRepo inOutRepository;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    @Override
    public void processLogEntry() throws ParseException {
        List<AccessLogEntity> logs = accessLogRepository.findAll();
        for (AccessLogEntity log : logs) {
            processAccessLog(log);
        }
    }


    @Transactional
    public void processAccessLog(AccessLogEntity log) throws ParseException {
        // Convert String date and time from AccessLog to Date and Time objects
        Date punchDate = new Date(dateFormat.parse(log.getLogDate()).getTime());
        Time punchTime = new Time(timeFormat.parse(log.getLogTime()).getTime());

        // Fetch all InOutEntity records for the same employee and date
        List<InOutEntity> inOutList = inOutRepository.findByEmployeeIDAndDate(log.getEmployeeID(), punchDate);

        // Get the earliest morning punch (if exists)
        Optional<InOutEntity> inOutOptional = inOutList.stream()
                .min(Comparator.comparing(InOutEntity::getPunchInMoa, Comparator.nullsLast(Comparator.naturalOrder())));

        // Get the earliest evening punch (if exists)
        Optional<InOutEntity> inOutEveningOptional = inOutList.stream()
                .min(Comparator.comparing(InOutEntity::getPunchInEv, Comparator.nullsLast(Comparator.naturalOrder())));

        // Use existing record or create a new one
        InOutEntity inOut = inOutOptional.orElseGet(() -> {
            InOutEntity newEntry = new InOutEntity();
            newEntry.setEmployeeID(log.getEmployeeID());
            newEntry.setDate(punchDate);
            return newEntry;
        });

        // Determine if it's a morning or evening punch
        boolean isMorning = punchTime.before(Time.valueOf("12:00:00"));
        boolean isEvening = !isMorning;

        if (isMorning) {
            // If morning punch exists, update only if it's earlier than the current one
            if (inOut.getPunchInMoa() == null || punchTime.before(inOut.getTimeMoa())) {
                inOut.setPunchInMoa(punchTime);
                inOut.setTimeMoa(punchTime);
                inOut.setIsMoaning(true);
            }
        } else if (isEvening) {
            // If evening punch exists, update only if it's earlier than the current one
            if (inOutEveningOptional.isEmpty() || punchTime.before(inOutEveningOptional.get().getTimeEve())) {
                inOut.setPunchInEv(punchTime);
                inOut.setTimeEve(punchTime);
                inOut.setIsEvening(true);
            }
        }

        // Save the updated or new record
        inOutRepository.save(inOut);
    }

    @Override
    public void main() throws ParseException {
        prerequisite();
        /// WE PULLING FROM DATA FROM ACCESS-LOG SERVER ---- EVERY SINGLE 2 HOURS
        processLogEntry();
    }

    @Override
    public void prerequisite() {
        /// WE PULLING FROM DATA FROM MAIN SERVER ---- EVERY SINGLE 15 MIN -- FROM SLT SERVER
        /// IS THERE ANY NEW RECORDS SAVE IT

        /// ALSO CHECK PAST RECORDS ---- DUE TO THE DATA - ERRORS
        /// WHICH MEANS IN PAST IF THERE ARE ANY DATA ERROR SO DATA WOULD NOT SAVE -- SO FIRST SAVE
        /// AND MAKE IT PAST IS TRUE SO LATER WE CAN MAKE FETCH ALL TEH RECORDS IS PAST IS TRUE

    }

    @Override
    public void processLogEntry(AccessLogEntity log_) throws ParseException {

    }
}
