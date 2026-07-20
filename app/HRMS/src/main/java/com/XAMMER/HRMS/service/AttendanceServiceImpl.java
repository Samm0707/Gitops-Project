package com.XAMMER.HRMS.service;

import com.XAMMER.HRMS.model.Attendance;
import com.XAMMER.HRMS.model.User;
import com.XAMMER.HRMS.repository.AttendanceRepository;
import com.XAMMER.HRMS.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.Duration; // Import Duration
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceServiceImpl.class);

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    @Autowired
    private EntityManager entityManager; // Ensure this is injected if used for flush()

    @Autowired
    public AttendanceServiceImpl(AttendanceRepository attendanceRepository, UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
    }

    private User getUserOrThrowException(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    IllegalArgumentException e = new IllegalArgumentException("User not found: " + username);
                    logger.error("User not found: {}", username, e);
                    return e;
                });
    }

    @Override
    public Attendance checkIn(String username) {
        User user = getUserOrThrowException(username);
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));

        // Check if already checked in today with no checkout
        if (attendanceRepository.findByUserAndAttendanceDateAndCheckOutTimeIsNull(user, today).isPresent()) {
            String errorMessage = "User " + username + " is already checked in and hasn't checked out today.";
            logger.warn(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        // Check if already checked in and checked out today
        if (attendanceRepository.findByUserAndAttendanceDateAndCheckOutTimeIsNotNull(user, today).isPresent()) {
            String errorMessage = "User " + username + " has already checked in and checked out today.";
            logger.warn(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        // If no record found for today, check for any existing check-in without checkout
        attendanceRepository.findByUserAndCheckOutTimeIsNull(user).ifPresent(existingAttendance -> {
            String errorMessage = "User " + username + " has a pending check-in from " + existingAttendance.getAttendanceDate() + " at " + existingAttendance.getCheckInTime() + ". Please check out first.";
            logger.warn(errorMessage);
            throw new IllegalStateException(errorMessage);
        });

        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setCheckInTime(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime());
        attendance.setAttendanceDate(today);
        attendance.setUsername(username);
        Attendance savedAttendance = attendanceRepository.save(attendance);
        logger.info("User {} checked in at {}", username, savedAttendance.getCheckInTime());
        return savedAttendance;
    }

    @Override
    public Attendance checkOut(String username) {
        User user = getUserOrThrowException(username);
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        Optional<Attendance> currentAttendanceOptional = attendanceRepository.findByUserAndAttendanceDateAndCheckOutTimeIsNull(user, today);

        if (currentAttendanceOptional.isEmpty()) {
            LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Kolkata")).minusDays(1);
            Optional<Attendance> yesterdayAttendance = attendanceRepository.findByUserAndAttendanceDateAndCheckOutTimeIsNull(user, yesterday);
            if (yesterdayAttendance.isPresent()) {
                String errorMessage = "Cannot checkout today. Your checkout for yesterday was missed. Please contact the administrator for a reset.";
                logger.warn(errorMessage);
                throw new IllegalStateException(errorMessage);
            } else {
                String errorMessage = "No active check-in found for user: " + username + " today.";
                logger.warn(errorMessage);
                throw new IllegalStateException(errorMessage);
            }
        }

        Attendance currentAttendance = currentAttendanceOptional.get();
        if (currentAttendance.getCheckOutTime() != null) {
            String errorMessage = "User " + username + " has already checked out today.";
            logger.warn(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        
        // =================================================================
        // NEW LOGIC: Enforce 8-hour rule for the Business team
        // =================================================================
        if ("Business".equalsIgnoreCase(user.getDepartment()) || "Marketing".equalsIgnoreCase(user.getDepartment())) {
            LocalDateTime checkInTime = currentAttendance.getCheckInTime();
            LocalDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime();
            Duration duration = Duration.between(checkInTime, now);

            if (duration.toHours() < 8) {
                long hours = duration.toHours();
                long minutes = duration.toMinutes() % 60;
                String errorMessage = String.format(
                    "Cannot check out before completing 8 hours. Your current duration is %d hours and %d minutes.",
                    hours, minutes
                );
                logger.warn("Early checkout attempt by {}: {}", username, errorMessage);
                throw new IllegalStateException(errorMessage);
            }
        }
        // =================================================================
        // END OF NEW LOGIC
        // =================================================================

        currentAttendance.setCheckOutTime(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime());
        Attendance updatedAttendance = attendanceRepository.save(currentAttendance);
        logger.info("User {} checked out at {}", username, updatedAttendance.getCheckOutTime());
        return updatedAttendance;
    }

    @Override
    public void recordCheckIn(String username) {
        User user = getUserOrThrowException(username);
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));

        if (isAlreadyCheckedInToday(user)) {
            String errorMessage = "User " + username + " is already checked in today.";
            logger.warn(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setCheckInTime(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime());
        attendance.setAttendanceDate(today);
        attendance.setUsername(username);
        attendanceRepository.save(attendance);
        logger.info("User {} check-in recorded at {}", username, attendance.getCheckInTime());
    }

    @Override
    public void recordCheckOut(String username) {
        User user = getUserOrThrowException(username);
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        Optional<Attendance> currentAttendanceOptional = attendanceRepository.findByUserAndAttendanceDateAndCheckOutTimeIsNull(user, today);
        currentAttendanceOptional.ifPresent(attendance -> {
            attendance.setCheckOutTime(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime());
            attendanceRepository.save(attendance);
            logger.info("User {} check-out recorded at {}", username, attendance.getCheckOutTime());
        });
    }

    @Override
    public void timeoutIncompleteSessions() {
        LocalDateTime timeoutTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).minusHours(12).toLocalDateTime();
        List<Attendance> incompleteSessions = attendanceRepository.findByCheckOutTimeIsNullAndCheckInTimeBefore(timeoutTime);
        incompleteSessions.forEach(session -> {
            session.setCheckOutTime(timeoutTime);
            logger.warn("User {} session timed out at {}", session.getUser().getUsername(), timeoutTime);
        });
        attendanceRepository.saveAll(incompleteSessions);
    }

    @Override
    public List<Attendance> getDailyAttendance(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<Attendance> attendances = attendanceRepository.findByCheckInTimeBetweenOrderByUser_UsernameAscCheckInTimeAsc(startOfDay, endOfDay);
        logger.info("Retrieved daily attendance for date: {}", date);
        return attendances;
    }

    @Override
    @Transactional
    public void resetAttendance(String username) {
        User user = getUserOrThrowException(username);
        attendanceRepository.deleteByUser(user);
        logger.warn("Attendance reset for user: {}", username);
    }

    @Override
    public List<Attendance> getAttendanceByDateRange(String username, LocalDate startDate, LocalDate endDate) {
        User user = getUserOrThrowException(username);
        LocalDateTime startOfDay = startDate.atStartOfDay();
        LocalDateTime endOfDay = endDate.atTime(LocalTime.MAX);
        List<Attendance> attendances = attendanceRepository.findByUserAndCheckInTimeBetweenOrderByCheckInTimeAsc(user, startOfDay, endOfDay);
        logger.info("Retrieved attendance for user {} between {} and {}", username, startDate, endDate);
        return attendances;
    }

    @Override
    public Attendance getTodayAttendance(String username) {
        User user = getUserOrThrowException(username);
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        List<Attendance> attendanceList = attendanceRepository.findByUserAndAttendanceDate(user, today);
        if (!attendanceList.isEmpty()) {
            return attendanceList.stream()
                    .max(Comparator.comparing(Attendance::getCheckInTime))
                    .orElse(null);
        }
        return null;
    }

    @Override
    @Transactional
    public void resetCheckout(String username, LocalDate date) {
        User user = getUserOrThrowException(username);
        List<Attendance> attendanceList = attendanceRepository.findByUserAndAttendanceDate(user, date);

        for (Attendance attendance : attendanceList) {
            if (attendance.getCheckOutTime() == null) {
                attendance.setCheckOutTime(LocalDateTime.of(date.plusDays(1), LocalTime.MIDNIGHT));
                logger.warn("Checkout time reset for user {} on date {} to {}", username, date, LocalDateTime.of(date.plusDays(1), LocalTime.MIDNIGHT));
                attendanceRepository.save(attendance);
            }
        }
    }

    @Override
    @Transactional
    public void deleteAllAttendanceRecords(String username) {
        User user = getUserOrThrowException(username);
        attendanceRepository.deleteByUser(user);
        logger.warn("All attendance records deleted for user: {}", username);
    }

    @Override
    @Transactional
    public void resetForTodayCheckIn(String username) {
        User user = getUserOrThrowException(username);
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        List<Attendance> todayAttendanceList = attendanceRepository.findByUserAndAttendanceDate(user, today);

        if (!todayAttendanceList.isEmpty()) {
            attendanceRepository.deleteAll(todayAttendanceList);
            entityManager.flush(); // Force immediate synchronization with the database
            logger.warn("All attendance records for today deleted for user: {}", username);
        } else {
            logger.info("No attendance record found for user {} today to reset.", username);
        }
    }

    @Override
    public boolean canCheckIn(String username) {
        User user = getUserOrThrowException(username);
        return !isAlreadyCheckedInToday(user);
    }

    private boolean isAlreadyCheckedInToday(User user) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        return attendanceRepository.findByUserAndAttendanceDateAndCheckOutTimeIsNull(user, today).isPresent();
    }

    @Override
    public Object getDailyAttendance() {
        throw new UnsupportedOperationException("Unimplemented method 'getDailyAttendance'");
    }

    @Override
    public Attendance getAttendanceByDate(String username, LocalDate date) {
        User user = getUserOrThrowException(username);
        List<Attendance> attendanceList = attendanceRepository.findByUserAndAttendanceDate(user, date);
        if (!attendanceList.isEmpty()) {
            return attendanceList.stream()
                    .max(Comparator.comparing(Attendance::getCheckInTime))
                    .orElse(null);
        }
        return null;
    }

    @Override
    public List<Attendance> getUsersWithMissedCheckoutYesterday() {
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Kolkata")).minusDays(1);
        return attendanceRepository.findByAttendanceDateAndCheckOutTimeIsNull(yesterday);
    }

    @Override
    @Transactional
    public boolean resetCheckoutTime(String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
                    List<Attendance> attendances = attendanceRepository.findByUserAndAttendanceDateOrderByCheckInTimeDesc(user, today);

                    if (!attendances.isEmpty()) {
                        Attendance latestAttendance = attendances.get(0);
                        latestAttendance.setCheckOutTime(null);
                        attendanceRepository.save(latestAttendance);
                        logger.warn("Checkout time reset for user {} on {}", username, today);
                        return true;
                    } else {
                        logger.warn("No attendance record found for user {} on {}", username, today);
                        return false;
                    }
                })
                .orElse(false);
    }
@Override
// Changed return type from Object to long
public long calculateDuration(Attendance attendance) {
    if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
        Duration duration = Duration.between(attendance.getCheckInTime(), attendance.getCheckOutTime());
        return duration.toMinutes(); // Return duration in minutes
    }
    return 0L; // Return 0 minutes if times are not set
}

    @Override
    @Transactional
    public boolean resetDailyAttendance(String username) {
        User user = getUserOrThrowException(username);
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        List<Attendance> todayAttendanceList = attendanceRepository.findByUserAndAttendanceDate(user, today);
        if (!todayAttendanceList.isEmpty()) {
            attendanceRepository.deleteAll(todayAttendanceList);
            logger.warn("All attendance records for today reset for user: {}", username);
            return true;
        } else {
            logger.info("No attendance records found for user {} today to reset.", username);
            return false;
        }
    }

 // In AttendanceServiceImpl.java

@Override
@Transactional
public boolean resetCheckoutTimeForDate(String username, LocalDate resetDate) {
    logger.info("Attempting to reset checkout for user: {} on date {} (ADMIN RESET)", username, resetDate);
    Optional<User> userOptional = userRepository.findByUsername(username);

    if (userOptional.isPresent()) {
        User user = userOptional.get();

        // IMPROVEMENT: Find the latest attendance record for that day,
        // regardless of whether checkout is null or not.
        List<Attendance> attendances = attendanceRepository.findByUserAndAttendanceDateOrderByCheckInTimeDesc(user, resetDate);

        if (!attendances.isEmpty()) {
            // Get the most recent attendance record for that day
            Attendance attendance = attendances.get(0); 
            
            LocalDateTime endOfDay = resetDate.atTime(LocalTime.MAX); // Sets time to 23:59:59.999...
            attendance.setCheckOutTime(endOfDay);
            
            // ✅ THE FIX: Set the status to indicate an admin action.
            // Ensure your Attendance entity has a 'checkoutStatus' field and a 'setCheckoutStatus' method.
            attendance.setCheckoutStatus("admin reset");
            
            attendanceRepository.save(attendance);
            logger.info("Checkout reset successfully for user: {} on date {} to {} (ADMIN RESET)", username, resetDate, endOfDay);
            return true;

        } else {
            logger.warn("No attendance record found for user {} on date {}.", username, resetDate);
            return false;
        }
    } else {
        logger.error("User not found: {}", username);
        return false;
    }
}

    @Override
    public void resetCheckoutTime(String username, LocalDate date) {
        logger.warn("resetCheckoutTime(String username, LocalDate date) called - Consider using resetCheckoutTimeForDate.");
        User user = getUserOrThrowException(username);
        List<Attendance> attendanceList = attendanceRepository.findByUserAndAttendanceDate(user, date);
        for (Attendance attendance : attendanceList) {
            if (attendance.getCheckOutTime() == null) {
                attendance.setCheckOutTime(LocalDateTime.of(date, LocalTime.MAX)); // Set to end of the day
                // Make sure your Attendance model has a 'checkoutStatus' field
                // attendance.setCheckoutStatus("ADMIN_RESET");
                attendanceRepository.save(attendance);
                logger.warn("Checkout time reset for user {} on date {} to {}", username, date, LocalDateTime.of(date, LocalTime.MAX));
            }
        }
    }

    @Override
    public List<Attendance> getAttendanceRecordsForUserAndDate(Long id, LocalDate effectiveDate) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAttendanceRecordsForUserAndDate'");
    }

    @Override
    public List<Attendance> getMonthlyAttendanceForUser(String username, int year, int month) {
        User user = getUserOrThrowException(username);

        // Create the start and end dates for the given month and year
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        logger.info("Fetching monthly attendance for user: {} from {} to {}", username, startDateTime, endDateTime);

        // We can reuse the existing repository method that finds records between two dates.
        return attendanceRepository.findByUserAndCheckInTimeBetweenOrderByCheckInTimeAsc(user, startDateTime, endDateTime);
    }

  @Override
    public List<Attendance> getMonthlyAttendanceForAllUsers(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        logger.info("Fetching monthly attendance for ALL users from {} to {}", startDateTime, endDateTime);

        return attendanceRepository.findByCheckInTimeBetweenOrderByUser_UsernameAscCheckInTimeAsc(startDateTime, endDateTime);
    }
}
