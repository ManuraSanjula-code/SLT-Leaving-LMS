package com.slt.radio.rosterservice.Repo;

import com.slt.radio.rosterservice.Model.One.Roster;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RosterRepository extends MongoRepository<Roster, String> {
    Optional<Roster> findByMonthAndYear(int month, int year);
    void deleteByYearAndMonth(int year, int month);
}
