package com.slt.radio.rosterservice.repo;

import com.slt.radio.rosterservice.model.one.lms.RosterAttendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RosterAttendanceRepository extends MongoRepository<RosterAttendance, String> {
    Optional<RosterAttendance> findByDate(String date);
    Page<RosterAttendance> findByMonthAndYear(int month, int year, Pageable pageable);
    Page<RosterAttendance> findAll(Pageable pageable);
}
