package com.XAMMER.HRMS.controller;

import com.XAMMER.HRMS.model.Attendance;
import com.XAMMER.HRMS.model.LeaveRequest;
import com.XAMMER.HRMS.model.User;
import com.XAMMER.HRMS.service.AttendanceService;
import com.XAMMER.HRMS.service.LeaveRequestService;
import com.XAMMER.HRMS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = {"http://localhost:8080", "http://127.0.0.1:5500", "null"})
public class AttendanceAnalyticsController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private UserService userService;

    @Autowired
    private LeaveRequestService leaveRequestService;

    private final ZoneId kolkataZone = ZoneId.of("Asia/Kolkata");
    private final double TARGET_DAILY_HOURS = 8.0;

    @GetMapping("/api/attendance/daily")
    public ResponseEntity<?> getDailyAttendanceData(
            @RequestParam(value = "date", required = false) String dateInput) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username).orElse(null);
            LocalDate date = dateInput != null ? LocalDate.parse(dateInput) : LocalDate.now(kolkataZone);

            Attendance attendance = attendanceService.getAttendanceByDate(username, date);
            double workedHours = 0.0;
            boolean isOnLeave = false;
            boolean adminReset = false;

            if (attendance != null && attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
                workedHours = (double) attendanceService.calculateDuration(attendance) / 60.0;
                adminReset = attendance.isAdminReset();
            }

            if (user != null) {
                isOnLeave = leaveRequestService.getLeaveRequestsForUserAndDate(user, date).stream()
                        .anyMatch(lr -> lr.getLeaveStatus() == LeaveRequest.LeaveStatus.APPROVED ||
                                lr.getLeaveStatus() == LeaveRequest.LeaveStatus.PENDING_ADMIN);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("workedHours", workedHours);
            response.put("targetHours", TARGET_DAILY_HOURS);
            response.put("isOnLeave", isOnLeave);
            response.put("adminReset", adminReset);
            response.put("date", date.toString());

            return ResponseEntity.ok(response);

        } catch (DateTimeException e) {
            return ResponseEntity.badRequest().body("Invalid date format. Please use YYYY-MM-DD");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing request");
        }
    }

    @GetMapping("/api/attendance/weekly")
    public ResponseEntity<?> getWeeklyAttendanceData(
            @RequestParam(value = "week", required = false) String weekInput) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username).orElse(null);
            
            LocalDate startOfWeek;
            if (weekInput != null) {
                String[] parts = weekInput.split("-W");
                int year = Integer.parseInt(parts[0]);
                int week = Integer.parseInt(parts[1]);
                startOfWeek = LocalDate.of(year, 1, 1)
                    .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            } else {
                startOfWeek = LocalDate.now(kolkataZone)
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            }

            LocalDate endOfWeek = startOfWeek.plusDays(6);
            List<Map<String, Object>> dailyDataList = new ArrayList<>();

            for (LocalDate date = startOfWeek; !date.isAfter(endOfWeek); date = date.plusDays(1)) {
                Map<String, Object> dailyData = new HashMap<>();
                dailyData.put("day", date.getDayOfWeek().toString().substring(0, 3));
                dailyData.put("fullDate", date.toString());

                Attendance attendance = attendanceService.getAttendanceByDate(username, date);
                double workedHours = 0.0;
                boolean isOnLeave = false;
                boolean adminReset = false;

                if (attendance != null && attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
                    workedHours = (double) attendanceService.calculateDuration(attendance) / 60.0;
                    adminReset = attendance.isAdminReset();
                }

                if (user != null) {
                    isOnLeave = leaveRequestService.getLeaveRequestsForUserAndDate(user, date).stream()
                            .anyMatch(lr -> lr.getLeaveStatus() == LeaveRequest.LeaveStatus.APPROVED ||
                                    lr.getLeaveStatus() == LeaveRequest.LeaveStatus.PENDING_ADMIN);
                }

                dailyData.put("workedHours", workedHours);
                dailyData.put("isOnLeave", isOnLeave);
                dailyData.put("adminReset", adminReset);
                dailyDataList.add(dailyData);
            }

            double totalHours = dailyDataList.stream()
                    .mapToDouble(d -> (Double) d.get("workedHours"))
                    .sum();
            double targetHours = dailyDataList.size() * TARGET_DAILY_HOURS;

            Map<String, Object> response = new HashMap<>();
            response.put("days", dailyDataList.stream().map(d -> d.get("day")).collect(Collectors.toList()));
            response.put("hours", dailyDataList.stream().map(d -> d.get("workedHours")).collect(Collectors.toList()));
            response.put("leaveStatus", dailyDataList.stream().map(d -> d.get("isOnLeave")).collect(Collectors.toList()));
            response.put("adminResetStatus", dailyDataList.stream().map(d -> d.get("adminReset")).collect(Collectors.toList()));
            response.put("totalHours", totalHours);
            response.put("targetHours", targetHours);

            return ResponseEntity.ok(response);

        } catch (DateTimeException e) {
            return ResponseEntity.badRequest().body("Invalid week format. Please use YYYY-Www");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing request");
        }
    }

    @GetMapping("/api/attendance/monthly")
    public ResponseEntity<?> getMonthlyAttendanceData(
            @RequestParam(value = "month", required = false) String monthInput) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username).orElse(null);
            
            YearMonth yearMonth = monthInput != null ? 
                YearMonth.parse(monthInput) : 
                YearMonth.now(kolkataZone);
            
            LocalDate firstOfMonth = yearMonth.atDay(1);
            LocalDate lastOfMonth = yearMonth.atEndOfMonth();
            List<Map<String, Object>> monthlyDataList = new ArrayList<>();
            double totalMonthlyHours = 0.0;
            int totalWorkingDays = 0;

            for (LocalDate date = firstOfMonth; !date.isAfter(lastOfMonth); date = date.plusDays(1)) {
                if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    totalWorkingDays++;

                    Attendance attendance = attendanceService.getAttendanceByDate(username, date);
                    double workedHours = 0.0;
                    boolean isOnLeave = false;
                    boolean isAdminReset = false;

                    if (attendance != null && attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
                        workedHours = (double) attendanceService.calculateDuration(attendance) / 60.0;
                        isAdminReset = attendance.isAdminReset();
                    }

                    if (user != null) {
                        isOnLeave = leaveRequestService.getLeaveRequestsForUserAndDate(user, date).stream()
                                .anyMatch(lr -> lr.getLeaveStatus() == LeaveRequest.LeaveStatus.APPROVED ||
                                        lr.getLeaveStatus() == LeaveRequest.LeaveStatus.PENDING_ADMIN);
                    }

                    totalMonthlyHours += workedHours;

                    Map<String, Object> dailyDetails = new HashMap<>();
                    dailyDetails.put("date", date.toString());
                    dailyDetails.put("dayOfWeek", date.getDayOfWeek().toString().substring(0, 3));
                    dailyDetails.put("workedHours", workedHours);
                    dailyDetails.put("isOnLeave", isOnLeave);
                    dailyDetails.put("isAdminReset", isAdminReset);
                    monthlyDataList.add(dailyDetails);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("month", yearMonth.toString());
            response.put("labels", monthlyDataList.stream()
                    .map(d -> d.get("dayOfWeek") + " " + ((String)d.get("date")).split("-")[2])
                    .collect(Collectors.toList()));
            response.put("hours", monthlyDataList.stream()
                    .map(d -> d.get("workedHours"))
                    .collect(Collectors.toList()));
            response.put("totalWorkedHours", totalMonthlyHours);
            response.put("totalTargetHours", totalWorkingDays * TARGET_DAILY_HOURS);
            response.put("dailyBreakdown", monthlyDataList);

            return ResponseEntity.ok(response);

        } catch (DateTimeException e) {
            return ResponseEntity.badRequest().body("Invalid month format. Please use YYYY-MM");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing request");
        }
    }
}