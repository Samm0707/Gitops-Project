package com.XAMMER.HRMS.service;

import com.XAMMER.HRMS.dto.DashboardMetricsDTO;
import com.XAMMER.HRMS.model.Attendance;
import com.XAMMER.HRMS.model.User;
import com.XAMMER.HRMS.repository.AttendanceRepository;
import com.XAMMER.HRMS.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;
    private static final String ADMIN_RESET_STATUS = "ADMIN_RESET";
    private static final LocalTime ADMIN_RESET_CHECKOUT_TIME = LocalTime.of(0, 1, 0); // Example: 00:01 AM
    private static final LocalTime DEFAULT_CHECKOUT_TIME = LocalTime.of(18, 0, 0); // Example: 6:00 PM

    public boolean canCheckIn(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found."));
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        LocalDate yesterday = today.minusDays(1);

        // Block check-in if there's an incomplete attendance record from yesterday
        if (attendanceRepository.findByUserIdAndAttendanceDateAndCheckOutTimeIsNull(user.getId(), yesterday).isPresent()) {
            return false;
        }

        // Check if already checked in today
        return !attendanceRepository.findByUserIdAndAttendanceDateAndCheckOutTimeIsNull(user.getId(), today).isPresent();
    }

    public Attendance checkIn(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found."));
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));

        if (attendanceRepository.findByUserIdAndAttendanceDateAndCheckOutTimeIsNull(user.getId(), today).isPresent()) {
            throw new IllegalStateException("Already checked in today.");
        }

        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setUsername(username);
        attendance.setAttendanceDate(today);
        attendance.setCheckInTime(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        return attendanceRepository.save(attendance);
    }

    public Attendance checkOut(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found."));
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));

        Optional<Attendance> todayAttendance = attendanceRepository.findByUserIdAndAttendanceDateAndCheckOutTimeIsNull(user.getId(), today);
        if (todayAttendance.isEmpty()) {
            throw new IllegalStateException("No active check-in found for today. You cannot checkout if you haven't checked in today. Contact admin if you forgot to checkout on a previous day.");
        }

        Attendance attendance = todayAttendance.get();
        attendance.setCheckOutTime(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        return attendanceRepository.save(attendance);
    }

    @Scheduled(cron = "0 5 0 * * ?")
    @Transactional
    public void autoCheckoutForgotten() {
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Kolkata")).minusDays(1);

        List<Attendance> forgottenCheckIns = attendanceRepository.findByAttendanceDateAndCheckOutTimeIsNull(yesterday);
        forgottenCheckIns.forEach(attendance -> {
            log.warn("User {} forgot to checkout on {}", attendance.getUsername(), yesterday);
            // We are no longer automatically setting checkout time.
        });
    }

public Attendance getTodayAttendance(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found."));
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        Optional<Attendance> attendanceOptional = attendanceRepository.findByUserIdAndAttendanceDate(user.getId(), today);
        return attendanceOptional.orElse(null);
    }

    public void resetAttendance(Long userId, LocalDate date) {
        Optional<Attendance> attendanceOptional = attendanceRepository.findByUserIdAndAttendanceDateAndCheckOutTimeIsNull(userId, date);
        attendanceOptional.ifPresent(attendance -> {
            LocalDateTime resetCheckoutTime = date.plusDays(1).atTime(ADMIN_RESET_CHECKOUT_TIME);
            attendance.setCheckOutTime(resetCheckoutTime);
            attendance.setCheckoutStatus(ADMIN_RESET_STATUS);
            attendanceRepository.save(attendance);
            log.info("Admin reset attendance for user {} on {} - set checkout time to {} with status {}",
                     attendance.getUsername(), date, resetCheckoutTime, ADMIN_RESET_STATUS);
        });
        // If no incomplete record is found for the given user and date, no action is taken.
    }

    @Override
    public DashboardMetricsDTO getDashboardMetrics() {
        DashboardMetricsDTO metrics = new DashboardMetricsDTO();
        long totalEmployees = userRepository.count();
        long presentEmployees = attendanceRepository.findByCheckInTimeBetweenOrderByUser_UsernameAscCheckInTimeAsc(
                LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay()
        ).stream()
                .filter(attendance -> attendance.getCheckOutTime() == null)
                .distinct()
                .count();
        long checkedOutEmployees = attendanceRepository.findByCheckInTimeBetweenOrderByUser_UsernameAscCheckInTimeAsc(
                LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay()
        ).stream()
                .filter(attendance -> attendance.getCheckOutTime() != null && attendance.getCheckInTime().toLocalDate().equals(LocalDate.now()))
                .distinct()
                .count();

        metrics.setTotalEmployees(totalEmployees);
        metrics.setPresentEmployees(presentEmployees);
        metrics.setCheckedOutEmployees(checkedOutEmployees);
        metrics.setPendingLeaveRequests(0L);
        return metrics;
    }

    @Override
    public List<String> getRecentActivity() {
        List<String> latestUsersActivity = userRepository.findAll().stream()
                .limit(5)
                .map(user -> "New user registered: " + user.getUsername() + " " + user.getLastName())
                .collect(Collectors.toList());

        List<String> latestCheckInsActivity = attendanceRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(Attendance::getCheckInTime).reversed())
                .limit(5)
                .map(attendance -> attendance.getUser().getUsername() + " checked in at " + attendance.getCheckInTime().toLocalTime())
                .collect(Collectors.toList());

        List<String> recentActivities = new java.util.ArrayList<>();
        recentActivities.addAll(latestUsersActivity);
        recentActivities.addAll(latestCheckInsActivity);
        return recentActivities.subList(0, Math.min(5, recentActivities.size()));
    }
}