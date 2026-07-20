package com.XAMMER.HRMS.repository;

import com.XAMMER.HRMS.model.Attendance;
import com.XAMMER.HRMS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByUserAndAttendanceDate(User user, LocalDate attendanceDate);
    List<Attendance> findByAttendanceDateOrderByUserUsernameAscCheckInTimeAsc(LocalDate date);
    // Add this method declaration:
     // Added for Optional return type
    Optional<Attendance> findByUserAndCheckOutTimeIsNull(User user);
    Optional<Attendance> findByUserAndAttendanceDateAndCheckOutTimeIsNotNull(User user, LocalDate attendanceDate);
    List<Attendance> findByCheckOutTimeIsNullAndCheckInTimeBefore(LocalDateTime timeoutTime);
    void deleteByUser(User user);
    List<Attendance> findByUserAndCheckInTimeBetweenOrderByCheckInTimeAsc(User user, LocalDateTime startOfDay, LocalDateTime endOfDay);
    List<Attendance> findByUserAndAttendanceDateOrderByCheckInTimeDesc(User user, LocalDate date);
    List<Attendance> findByAttendanceDateAndCheckOutTimeIsNull(LocalDate date);
    List<Attendance> findByCheckInTimeBetweenOrderByUser_UsernameAscCheckInTimeAsc(LocalDateTime startOfDay, LocalDateTime endOfDay);
    Optional<Attendance> findByUserAndAttendanceDateAndCheckOutTimeIsNull(User user, LocalDate today);
    Optional<Attendance> findByUserIdAndAttendanceDate(Long id, LocalDate today);
    
    Optional<Attendance> findByUserIdAndAttendanceDateAndCheckOutTimeIsNull(Long userId, LocalDate date);
}