package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    Optional<Holiday> findByHolidayDate(LocalDate date);

    List<Holiday> findByHolidayDateBetween(LocalDate startDate, LocalDate endDate);

    List<Holiday> findByIsRecurring(boolean isRecurring);

    @Query("SELECT h FROM Holiday h WHERE YEAR(h.holidayDate) = :year")
    List<Holiday> findByYear(int year);

    boolean existsByHolidayDate(LocalDate date);
}