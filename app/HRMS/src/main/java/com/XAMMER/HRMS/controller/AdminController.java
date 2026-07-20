package com.XAMMER.HRMS.controller;

import com.XAMMER.HRMS.dto.AttendanceDTO;
import com.XAMMER.HRMS.model.Attendance;
import com.XAMMER.HRMS.model.Holiday;
import com.XAMMER.HRMS.model.LeaveRequest;
import com.XAMMER.HRMS.model.User;
import com.XAMMER.HRMS.service.AdminService;
import com.XAMMER.HRMS.service.AttendanceService;
import com.XAMMER.HRMS.service.HolidayService;
import com.XAMMER.HRMS.service.LeaveRequestService;
import com.XAMMER.HRMS.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth; // <-- ADDED IMPORT
import java.util.List;
import java.util.Optional; // <-- ADDED IMPORT
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private HolidayService holidayService;


    // Admin dashboard (initial load or default view)
    @GetMapping("/dashboard")
    public String adminDashboard(@RequestParam(value = "date", required = false) String dateStr,
                                 @RequestParam(value = "user", required = false) String username,
                                 Model model,
                                 Principal principal) {
        userService.findByUsername(principal.getName())
                .ifPresent(admin -> model.addAttribute("username", admin.getUsername()));

        model.addAttribute("todayDate", LocalDateTime.now());
        model.addAttribute("dashboardMetrics", adminService.getDashboardMetrics());
        model.addAttribute("recentActivity", adminService.getRecentActivity());

        List<User> upcomingBirthdays = userService.getUpcomingBirthdays();
        model.addAttribute("upcomingBirthdays", upcomingBirthdays);

        List<Holiday> upcomingHolidays = holidayService.getUpcomingHolidays();
        model.addAttribute("upcomingHolidays", upcomingHolidays);

        LocalDate selectedDate = (dateStr != null) ? LocalDate.parse(dateStr) : LocalDate.now();
        List<Attendance> filteredAttendance = attendanceService.getDailyAttendance(selectedDate);
        if (username != null && !username.isBlank()) {
            filteredAttendance = filteredAttendance.stream()
                    .filter(a -> a.getUser() != null && username.equalsIgnoreCase(a.getUser().getUsername()))
                    .collect(Collectors.toList());
        }

        List<AttendanceDTO> attendanceDTOs = filteredAttendance.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        model.addAttribute("attendances", attendanceDTOs);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("selectedUser", username);
        model.addAttribute("allUsers", userService.getAllUsernames());

        return "dashboard-admin";
    }

    /**
     * REVISED METHOD: This single endpoint now handles both daily and monthly attendance views
     * by checking the 'viewType' parameter.
     */
@GetMapping("/attendance")
public String showAttendancePage(
        @RequestParam("viewType") Optional<String> viewTypeOpt,
        @RequestParam("date") Optional<String> dateStrOpt,
        @RequestParam("user") Optional<String> usernameOpt,
        @RequestParam("year") Optional<Integer> yearOpt,
        @RequestParam("month") Optional<Integer> monthOpt,
        Model model,
        Principal principal) {

    // Add common model attributes
    userService.findByUsername(principal.getName())
                   .ifPresent(admin -> model.addAttribute("username", admin.getUsername()));
    model.addAttribute("allUsers", userService.getAllUsernames());

    String viewType = viewTypeOpt.orElse("daily");
    String username = usernameOpt.filter(u -> !u.isBlank()).orElse(null); // Treat blank as null

    List<Attendance> attendanceData = new java.util.ArrayList<>();

    if ("monthly".equals(viewType)) {
        // --- MONTHLY VIEW LOGIC ---
        int year = yearOpt.orElse(YearMonth.now().getYear());
        int month = monthOpt.orElse(YearMonth.now().getMonthValue());
        
        if (username != null) {
            // A specific user IS selected
            attendanceData = attendanceService.getMonthlyAttendanceForUser(username, year, month);
        } else {
            // NO user is selected, get for ALL users
            attendanceData = attendanceService.getMonthlyAttendanceForAllUsers(year, month);
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        String monthName = yearMonth.getMonth().name();
        model.addAttribute("selectedMonth", monthName.substring(0, 1) + monthName.substring(1).toLowerCase());
        model.addAttribute("selectedYear", year);

    } else {
        // --- DAILY VIEW LOGIC (Default) ---
        LocalDate selectedDate = dateStrOpt.map(LocalDate::parse).orElse(LocalDate.now());

        if (username != null) {
            // A specific user IS selected
            // We first get all records for the day, then filter by the specified user
            attendanceData = attendanceService.getDailyAttendance(selectedDate)
                .stream()
                .filter(a -> a.getUser() != null && username.equalsIgnoreCase(a.getUser().getUsername()))
                .collect(Collectors.toList());
        } else {
            // NO user is selected, get for ALL users
            attendanceData = attendanceService.getDailyAttendance(selectedDate);
        }
        
        model.addAttribute("selectedDate", selectedDate);
    }
    
    // --- Common logic for both views ---
    List<AttendanceDTO> attendanceDTOs = attendanceData.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

    model.addAttribute("attendances", attendanceDTOs);
    model.addAttribute("selectedUser", username); // Will be null if no user was selected
    model.addAttribute("viewType", viewType);

    return "admin-attendance";
}

    @GetMapping("/users/suggestions")
    @ResponseBody
    public ResponseEntity<List<String>> getUserSuggestions(@RequestParam("query") String query) {
        List<String> suggestions = userService.findUsernamesContaining(query);
        return ResponseEntity.ok(suggestions);
    }

    @PostMapping("/attendance/reset/checkout/{username}")
    public String resetCheckout(@PathVariable String username,
                                @RequestParam("resetDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate resetDate,
                                RedirectAttributes redirectAttributes) {
        logger.info("Admin requested to reset checkout for user: {} on date: {}", username, resetDate);
        boolean success = attendanceService.resetCheckoutTimeForDate(username, resetDate);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Checkout time reset successfully for " + username + " on " + resetDate);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to reset checkout time for " + username + " on " + resetDate);
        }
        // Redirect back with parameters to maintain the view
        String referer = "?viewType=daily&date=" + resetDate + "&user=" + username;
        return "redirect:/admin/attendance" + referer;
    }

    @PostMapping("/attendance/reset/checkin/{username}")
    public String resetToday(@PathVariable String username, 
                             @RequestParam("resetDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate resetDate,
                             RedirectAttributes redirectAttributes) {
        logger.warn("Admin requested to reset entire day's attendance for user: {}", username);
        // Note: The service method seems to reset for 'today', you might want to adapt it to use `resetDate`
        boolean success = attendanceService.resetDailyAttendance(username); 
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Day's attendance reset successfully for " + username);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to reset day's attendance for " + username);
        }
        String referer = "?viewType=daily&date=" + resetDate + "&user=" + username;
        return "redirect:/admin/attendance" + referer;
    }

    @GetMapping("/requests/pending")
    public String viewPendingAdminRequests(Model model) {
        List<LeaveRequest> pendingRequests = leaveRequestService.getAllPendingRequests();
        model.addAttribute("pendingRequests", pendingRequests);
        return "admin-pending-requests";
    }

    @PostMapping("/requests/approve")
    public String approveLeaveRequestByAdmin(@RequestParam("requestId") Long requestId, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        userService.findByUsername(username).ifPresent(admin -> {
            LeaveRequest request = leaveRequestService.getLeaveRequestById(requestId);
            if (request != null && request.getLeaveStatus() == LeaveRequest.LeaveStatus.PENDING_ADMIN && request.getManagerApprovalStatus().equals(LeaveRequest.ApprovalStatus.APPROVED)) {
                leaveRequestService.approveLeaveRequestByAdmin(requestId, admin);
                redirectAttributes.addFlashAttribute("successMessage", "Leave request approved successfully.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid request or not pending admin approval.");
            }
        });
        return "redirect:/admin/requests/pending";
    }

    @PostMapping("/requests/reject")
    public String rejectLeaveRequestByAdmin(@RequestParam("requestId") Long requestId, @RequestParam("rejectionReason") String rejectionReason, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        userService.findByUsername(username).ifPresent(admin -> {
            LeaveRequest request = leaveRequestService.getLeaveRequestById(requestId);
            if (request != null && request.getLeaveStatus() == LeaveRequest.LeaveStatus.PENDING_ADMIN && request.getManagerApprovalStatus().equals(LeaveRequest.ApprovalStatus.APPROVED)) {
                leaveRequestService.rejectLeaveRequestByAdmin(requestId, admin, rejectionReason);
                redirectAttributes.addFlashAttribute("successMessage", "Leave request rejected successfully.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid request or not pending admin approval.");
            }
        });
        return "redirect:/admin/requests/pending";
    }

    private AttendanceDTO convertToDTO(Attendance attendance) {
        AttendanceDTO dto = new AttendanceDTO();
        if (attendance.getUser() != null) {
            dto.setUsername(attendance.getUser().getUsername());
        }
        dto.setCheckInTime(attendance.getCheckInTime());
        dto.setCheckOutTime(attendance.getCheckOutTime());
        dto.setAttendanceDate(attendance.getAttendanceDate());
        // No need to log here, it can be very noisy
        // logger.info("Converting Attendance for user: {}, date: {}", ...); 
        return dto;
    }
}