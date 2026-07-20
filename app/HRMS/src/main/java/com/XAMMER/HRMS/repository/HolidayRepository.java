package com.XAMMER.HRMS.repository;

import com.XAMMER.HRMS.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    // You can add custom query methods here if needed, e.g.,
    List<Holiday> findByDateBetween(LocalDate startDate, LocalDate endDate);
     @Query("SELECT h FROM Holiday h WHERE h.date >= :today ORDER BY h.date ASC")
    List<Holiday> findUpcomingHolidays(@Param("today") LocalDate today);

     List<Holiday> findByDateGreaterThanEqualOrderByDateAsc(LocalDate today);
}