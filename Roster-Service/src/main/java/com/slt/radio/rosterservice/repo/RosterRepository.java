package com.slt.radio.rosterservice.repo;

import com.slt.radio.rosterservice.document.one.Roster;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RosterRepository extends MongoRepository<Roster, String> {
    Optional<Roster> findByMonthAndYear(int month, int year);
    void deleteByYearAndMonth(int year, int month);
}
