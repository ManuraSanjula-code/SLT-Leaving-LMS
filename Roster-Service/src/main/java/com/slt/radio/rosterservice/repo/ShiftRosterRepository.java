package com.slt.radio.rosterservice.repo;

import com.slt.radio.rosterservice.model.one.shift.ShiftRoster;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRosterRepository extends MongoRepository<ShiftRoster, String> {
    Optional<ShiftRoster> findByMonthAndYear(String month, int year);
    void deleteByYearAndMonth(int year, String month);
    List<ShiftRoster> findByYearAndMonth(int year, String month);
}

