package com.slt.radio.rosterservice.Repo;

import com.slt.radio.rosterservice.Model.Second.DutyRoster;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DutyRosterRepository extends MongoRepository<DutyRoster, String> {

    List<DutyRoster> findByIsActive(Boolean isActive);    // Find roster by week starting date
    Optional<DutyRoster> findByWeekStartingDate(LocalDate weekStartingDate);

    // Find roster by name and week
    Optional<DutyRoster> findByRosterNameAndWeekStartingDate(String rosterName, LocalDate weekStartingDate);

    // Find all rosters for a specific month
    List<DutyRoster> findByWeekStartingDateBetween(LocalDate startDate, LocalDate endDate);

    // Find rosters by employee ID
    @Query("{'dailyDuties.timeSlots.assignedEmployees': ?0}")
    List<DutyRoster> findByEmployeeId(String employeeId);

    // Find current week roster
    @Query("{'weekStartingDate': {$lte: ?0}, 'weekStartingDate': {$gte: ?1}}")
    List<DutyRoster> findCurrentWeekRoster(LocalDate currentDate, LocalDate weekStart);

    void deleteDutyRosterByWeekStartingDate(LocalDate weekStartingDate);

    @Query(value = "{ 'isActive' : true }", sort = "{ 'week_starting_date' : -1 }")
    Optional<DutyRoster> findLatestActiveRoster();
}
