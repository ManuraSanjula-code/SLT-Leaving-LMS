package com.slt.radio.rosterservice.Repo;

import com.slt.radio.rosterservice.Model.Shift.ShiftRoster;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ShiftRosterRepository extends MongoRepository<ShiftRoster, String> {
    Optional<ShiftRoster> findByMonthAndYear(String month, int year);
}

