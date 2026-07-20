package com.XAMMER.HRMS.controller;

import com.XAMMER.HRMS.model.Attendance;
import com.XAMMER.HRMS.model.Holiday;
import com.XAMMER.HRMS.model.LeaveRequest;
import com.XAMMER.HRMS.model.Roles;
import com.XAMMER.HRMS.model.User;
import com.XAMMER.HRMS.service.AttendanceService;
import com.XAMMER.HRMS.service.HolidayService;
import com.XAMMER.HRMS.service.LeaveRequestService;
import com.XAMMER.HRMS.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
public class DashboardController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private UserService userService;

    @Autowired
    private HolidayService holidayService;

    @Autowired
    private LeaveRequestService leaveRequestService;

    private final ZoneId kolkataZone = ZoneId.of("Asia/Kolkata");
    // Corrected dateFormatter
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, ''•'' hh:mm a").withZone(kolkataZone);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a").withZone(kolkataZone);
    private final DateTimeFormatter dateTimeFormatterUTC = DateTimeFormatter.ISO_INSTANT; // Use ISO_INSTANT for UTC

    // ✅ Custom Login Page
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Must match src/main/resources/templates/login.html
    }

    // ✅ Dashboard View
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        model.addAttribute("username", username);

        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isPresent()) {
            User loggedInUser = userOptional.get();
            model.addAttribute("employee", loggedInUser); // Changed "user" to "employee"

            ZonedDateTime nowInKolkata = ZonedDateTime.now(kolkataZone);
            // Pass the ZonedDateTime object directly
            model.addAttribute("todayDate", nowInKolkata);
            model.addAttribute("currentTime", timeFormatter.format(nowInKolkata));

            Attendance todayAttendance = attendanceService.getTodayAttendance(username);
            ZonedDateTime checkInTimeZoned = null;
            ZonedDateTime checkOutTimeZoned = null;
            boolean canCheckIn = true;
            boolean canCheckOut = false;
            String checkOutMessage = null;

            if (todayAttendance != null) {
                if (todayAttendance.getCheckInTime() != null) {
                    // Assume the LocalDateTime from the database is already in the desired timezone (or close)
                    // Convert it to ZonedDateTime using the Kolkata zone.
                    checkInTimeZoned = todayAttendance.getCheckInTime().atZone(kolkataZone);
                    log.info("Retrieved and zoned checkInTime: {}", checkInTimeZoned);
                    model.addAttribute("checkInTime", checkInTimeZoned);
                } else {
                    model.addAttribute("checkInTime", null);
                }
                if (todayAttendance.getCheckOutTime() != null) {
                    // Assume the LocalDateTime from the database is already in the desired timezone (or close)
                    // Convert it to ZonedDateTime using the Kolkata zone.
                    checkOutTimeZoned = todayAttendance.getCheckOutTime().atZone(kolkataZone);
                    log.info("Retrieved and zoned checkOutTime: {}", checkOutTimeZoned);
                    model.addAttribute("checkOutTime", checkOutTimeZoned);
                } else {
                    model.addAttribute("checkOutTime", null);
                }

                if (checkInTimeZoned != null && checkOutTimeZoned == null) {
                    canCheckIn = false;
                    canCheckOut = true;
                    // Check if check-in was yesterday and no checkout
                    LocalDate yesterday = LocalDate.now(kolkataZone).minusDays(1);
                    Attendance yesterdayAttendance = attendanceService.getAttendanceByDate(username, yesterday);
                    if (yesterdayAttendance != null && yesterdayAttendance.getCheckInTime() != null && yesterdayAttendance.getCheckOutTime() == null) {
                        canCheckOut = false;
                        checkOutMessage = "Your check-out for yesterday was missed. Please contact the administrator for a reset.";
                    }
                } else if (checkOutTimeZoned != null) {
                    canCheckIn = false; // Already checked in and out today
                }
            } else {
                model.addAttribute("checkInTime", null);
                model.addAttribute("checkOutTime", null);
            }

            List<User> upcomingBirthdays = userService.getUpcomingBirthdays();
            model.addAttribute("upcomingBirthdays", upcomingBirthdays);

            List<Holiday> upcomingHolidays = holidayService.getUpcomingHolidays();
            model.addAttribute("upcomingHolidays", upcomingHolidays);

            // Load leave request data based on the user's role
            if (loggedInUser.getRoles() == Roles.ROLE_USER) {
                List<LeaveRequest> myRequests = leaveRequestService.getLeaveRequestsForUser(loggedInUser);
                model.addAttribute("myLeaveRequests", myRequests);
            } else if (loggedInUser.getRoles() == Roles.ROLE_USER_MANAGER) {
                List<LeaveRequest> pendingRequests = leaveRequestService.getPendingRequestsForManager(loggedInUser);
                model.addAttribute("pendingRequests", pendingRequests); // Changed attribute name
            } else if (loggedInUser.getRoles() == Roles.ROLE_ADMIN) {
                List<LeaveRequest> allPendingRequests = leaveRequestService.getAllPendingRequests();
                model.addAttribute("allPendingLeaveRequests", allPendingRequests);
                List<Attendance> missedCheckouts = attendanceService.getUsersWithMissedCheckoutYesterday();
                model.addAttribute("missedCheckouts", missedCheckouts);
            }

            return "dashboard"; // Use a single dashboard template
        } else {
            return "redirect:/login?error=userNotFound"; // Redirect with an error if user not found
        }
    }

    @PostMapping("/dashboard/checkin")
    @ResponseBody
    public Map<String, Object> checkIn() {
        Map<String, Object> response = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            response.put("status", "error");
            response.put("message", "User not authenticated.");
            return response;
        }
        String username = authentication.getName();
        try {
            Attendance attendance = attendanceService.checkIn(username);
            response.put("status", "success");
            // Format the stored UTC time to ISO 8601 with 'Z'
            //response.put("checkInTime", dateTimeFormatterUTC.format(attendance.getCheckInTime().toInstant(ZoneOffset.UTC)));
            response.put("checkInTime", dateTimeFormatterUTC.format(attendance.getCheckInTime().atZone(kolkataZone).toInstant()));
        } catch (IllegalStateException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Check-in failed. Try again.");
            log.error("Check-in error for user {}: {}", username, e.getMessage());
        }
        return response;
    }

    @PostMapping("/dashboard/checkout")
    @ResponseBody
    public Map<String, Object> checkOut() {
        Map<String, Object> response = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            response.put("status", "error");
            response.put("message", "User not authenticated.");
            return response;
        }
        String username = authentication.getName();
        try {
            Attendance attendance = attendanceService.checkOut(username);
            response.put("status", "success");
            // Format the stored UTC time to ISO 8601 with 'Z'
            //response.put("checkOutTime", dateTimeFormatterUTC.format(attendance.getCheckOutTime().toInstant(ZoneOffset.UTC)));
            response.put("checkOutTime", dateTimeFormatterUTC.format(attendance.getCheckOutTime().atZone(kolkataZone).toInstant()));
        } catch (IllegalStateException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Check-out failed. Try again.");
            log.error("Check-out error for user {}: {}", username, e.getMessage());
        }
        return response;
    }

    @PostMapping("/admin/reset-checkout")
    public String resetCheckout(@RequestParam("userId") Long userId) {
        Optional<User> userOptional = userService.findById(userId);
        if (userOptional.isPresent()) {
            User userToReset = userOptional.get();
            LocalDate yesterday = LocalDate.now(kolkataZone).minusDays(1);
            attendanceService.resetCheckout(userToReset.getUsername(), yesterday);
            return "redirect:/dashboard"; // Redirect back to the dashboard
        } else {
            // Handle user not found scenario
            return "redirect:/dashboard?error=userNotFound";
        }
    }

    @PostMapping("/admin/reset-today-attendance")
    public String resetTodayAttendance(@RequestParam("userId") Long userId) {
        Optional<User> userOptional = userService.findById(userId);
        if (userOptional.isPresent()) {
            User userToReset = userOptional.get();
            attendanceService.resetForTodayCheckIn(userToReset.getUsername());
            return "redirect:/dashboard";
        } else {
            return "redirect:/dashboard?error=userNotFound";
        }
    }
}