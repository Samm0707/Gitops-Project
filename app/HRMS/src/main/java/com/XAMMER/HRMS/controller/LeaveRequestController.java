package com.XAMMER.HRMS.controller;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.XAMMER.HRMS.model.LeaveRequest;
import com.XAMMER.HRMS.model.LeaveType;
import com.XAMMER.HRMS.model.Roles;
import com.XAMMER.HRMS.service.LeaveRequestService;
import com.XAMMER.HRMS.service.UserService;

@Controller
@RequestMapping("/leave")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private UserService userService;

    // Display leave application form and the user's leave requests
    @GetMapping("/apply")
    public String showLeaveForm(Model model, Principal principal) {
        model.addAttribute("leaveRequest", new LeaveRequest());
        model.addAttribute("leaveTypes", LeaveType.values());

        userService.findByUsername(principal.getName())
                .ifPresent(user -> {
                    List<LeaveRequest> myRequests = leaveRequestService.getLeaveRequestsByUser(user);
                    // Safely sort requests by submissionDate in descending order
                    myRequests.sort((r1, r2) -> {
                        if (r1.getSubmissionDate() == null && r2.getSubmissionDate() == null) return 0;
                        if (r1.getSubmissionDate() == null) return 1;
                        if (r2.getSubmissionDate() == null) return -1;
                        return r2.getSubmissionDate().compareTo(r1.getSubmissionDate());
                    });
                    model.addAttribute("myLeaveRequests", myRequests);
                });

        return "leave-form";
    }

    // Handle leave application submission
    @PostMapping("/submit")
    public String submitLeaveRequest(@ModelAttribute LeaveRequest leaveRequest, Principal principal) {
        userService.findByUsername(principal.getName())
                .ifPresent(applicant -> {
                    leaveRequestService.submitLeaveRequest(applicant, leaveRequest);
                });
        return "redirect:/dashboard"; // Or wherever you want to redirect after submission
    }

    // View all leave requests (for managers and admins)
    @GetMapping("/requests")
    public String viewLeaveRequests(Model model, Principal principal) {
        userService.findByUsername(principal.getName())
                .ifPresent(user -> {
                    List<LeaveRequest> requests = leaveRequestService.getLeaveRequestsForUser(user);

                    if (user.getRoles() == Roles.ROLE_USER_MANAGER) {
                        List<LeaveRequest> pendingForManager = leaveRequestService.getPendingRequestsForManager(user);
                        model.addAttribute("pendingRequests", pendingForManager);
                    } else if (user.getRoles() == Roles.ROLE_ADMIN) {
                        List<LeaveRequest> allRequests = leaveRequestService.getAllLeaveRequests();
                        model.addAttribute("allRequests", allRequests);
                    }

                    model.addAttribute("leaveRequests", requests);
                    model.addAttribute("user", user);
                });
        return "leave-requests";
    }

    // Approve leave
    @GetMapping("/approve/{id}")
    public String approveLeave(@PathVariable Long id, Principal principal) {
        userService.findByUsername(principal.getName())
                .ifPresent(approver -> {
                    leaveRequestService.approveLeaveRequest(id, approver);
                });
        return "redirect:/leave/requests";
    }

    // Reject leave - Handles rejection reason via request parameter
    @GetMapping("/reject/{id}")
    public String rejectLeave(@PathVariable Long id, Principal principal,
                                     @RequestParam(value = "reason", required = false) String reason) {
        userService.findByUsername(principal.getName())
                .ifPresent(rejector -> {
                    leaveRequestService.rejectLeaveRequest(id, rejector, reason);
                });
        return "redirect:/leave/requests";
    }

    // Get leave request details for modal
    @GetMapping("/details/{id}")
    @ResponseBody
    public LeaveRequest getLeaveRequestDetails(@PathVariable Long id) {
        return leaveRequestService.getLeaveRequestById(id).orElse(null);
    }

    // Cancel leave request
 @PostMapping("/cancel/{id}")
@ResponseBody
public ResponseEntity<String> cancelLeaveRequest(@PathVariable Long id, Principal principal) {
    return userService.findByUsername(principal.getName())
            .map(user -> {
                try {
                    leaveRequestService.cancelLeaveRequest(id, user);
                    return ResponseEntity.ok("Leave request cancelled successfully.");
                } catch (NoSuchElementException e) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Leave request not found.");
                } catch (IllegalStateException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace(); // Log the error properly
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to cancel leave request.");
                }
            })
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
}
}