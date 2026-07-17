// package com.XAMMER.HRMS.controller;

// import com.XAMMER.HRMS.model.Attendance;
// import com.XAMMER.HRMS.service.AttendanceService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.format.annotation.DateTimeFormat;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.time.LocalDate;
// import java.util.List;
// import java.util.Map;
// import java.util.stream.Collectors;

// @RestController
// @RequestMapping("/admin/attendance")
// public class AdminAttendanceController {

//     @Autowired
//     private AttendanceService attendanceService;

//     @PostMapping("/reset/{username}")
//     public ResponseEntity<?> resetUserAttendance(@PathVariable String username) {
//         try {
//             attendanceService.resetForTodayCheckIn(username);
//             return ResponseEntity.ok(Map.of("status", "success", "message", "System reset for " + username + " to allow today's check-in."));
//         } catch (Exception e) {
//             return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Failed to reset system for " + username + ": " + e.getMessage()));
//         }
//     }

//     @GetMapping("/daily")
//     public ResponseEntity<?> getDailyAttendance(@RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
//         LocalDate requestedDate = date != null ? date : LocalDate.now();
//         List<Attendance> attendanceList = attendanceService.getDailyAttendance(requestedDate);

//         List<Map<String, Object>> formattedAttendance = attendanceList.stream()
//                 .map(attendance -> Map.of(
//                         "username", (Object) attendance.getUsername(),
//                         "checkInTime", (Object) (attendance.getCheckInTime() != null ? attendance.getCheckInTime().toLocalTime() : null),
//                         "checkOutTime", (Object) (attendance.getCheckOutTime() != null ? attendance.getCheckOutTime().toLocalTime() : null)
//                 ))
//                 .collect(Collectors.toList());

//         return ResponseEntity.ok(formattedAttendance);
//     }

//     @GetMapping("/user/{username}/history")
//     public ResponseEntity<?> getUserAttendanceHistory(
//             @PathVariable String username,
//             @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//             @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
//         List<Attendance> attendanceList = attendanceService.getAttendanceByDateRange(username, startDate, endDate);

//         List<Map<String, Object>> formattedAttendance = attendanceList.stream()
//                 .map(attendance -> Map.of(
//                         "date", (Object) (attendance.getCheckInTime() != null ? attendance.getCheckInTime().toLocalDate() : null),
//                         "checkInTime", (Object) (attendance.getCheckInTime() != null ? attendance.getCheckInTime().toLocalTime() : null),
//                         "checkOutTime", (Object) (attendance.getCheckOutTime() != null ? attendance.getCheckOutTime().toLocalTime() : null)
//                 ))
//                 .collect(Collectors.toList());

//         return ResponseEntity.ok(formattedAttendance);
//     }

//     @DeleteMapping("/reset/{username}") // Using DELETE for a complete reset as it's a more destructive action
//     public ResponseEntity<?> resetAllAttendance(@PathVariable String username) {
//         try {
//             attendanceService.resetAttendance(username);
//             return ResponseEntity.ok(Map.of("status", "success", "message", "All attendance records for " + username + " have been reset."));
//         } catch (Exception e) {
//             return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Failed to reset all attendance for " + username + ": " + e.getMessage()));
//         }
//     }
// }