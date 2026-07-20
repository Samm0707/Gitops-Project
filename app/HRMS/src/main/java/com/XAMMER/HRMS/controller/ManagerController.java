package com.XAMMER.HRMS.controller;

import com.XAMMER.HRMS.model.LeaveRequest;
import com.XAMMER.HRMS.model.User;
import com.XAMMER.HRMS.service.LeaveRequestService;
import com.XAMMER.HRMS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    private UserService userService;

    @Autowired
    private LeaveRequestService leaveRequestService;

@GetMapping("/dashboard")
public String managerDashboardView(Model model, Authentication authentication) {
    String username = authentication.getName();
    Optional<User> managerOptional = userService.findByUsername(username);

    if (managerOptional.isPresent()) {
        User manager = managerOptional.get();
        model.addAttribute("user", manager);
        model.addAttribute("username", username);
        model.addAttribute("todayDate", java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM dd")));

        // Fetch ONLY the pending leave requests for the manager
        List<LeaveRequest> managedRequests = leaveRequestService.getPendingRequestsForManager(manager);
        model.addAttribute("managedRequests", managedRequests);

        // Fetch the manager's own leave requests for display (this seems correct as is)
        List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsByUser(manager);
        model.addAttribute("leaveRequests", leaveRequests);

        return "manager-dashboard";
    } else {
        return "redirect:/login?error=userNotFound";
    }
}

    @GetMapping("/requests/pending")
    public String viewPendingRequests(Model model, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> managerOptional = userService.findByUsername(username);

        if (managerOptional.isPresent()) {
            User manager = managerOptional.get();
            List<LeaveRequest> pendingRequests = leaveRequestService.getPendingRequestsForManager(manager);
            model.addAttribute("pendingRequests", pendingRequests);
            return "manager-pending-requests";
        } else {
            return "redirect:/login?error=userNotFound";
        }
    }

    @PostMapping("/requests/approve")
    public String approveRequest(@RequestParam("requestId") Long requestId, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        Optional<User> approverOptional = userService.findByUsername(username);
        if (approverOptional.isPresent()) {
            User approver = approverOptional.get();
            LeaveRequest leaveRequest = leaveRequestService.getLeaveRequestById(requestId);
            if (leaveRequest != null && leaveRequest.getLeaveStatus() == LeaveRequest.LeaveStatus.PENDING_MANAGER) {
                leaveRequestService.approveLeaveRequestByManager(requestId, approver);
                redirectAttributes.addFlashAttribute("successMessage", "Leave request approved successfully.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid request or request already processed.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Error approving request. Please try again.");
        }
        return "redirect:/manager/dashboard";
    }

    @PostMapping("/requests/reject")
    public String rejectRequest(@RequestParam("requestId") Long requestId, @RequestParam("rejectionReason") String rejectionReason, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        Optional<User> rejectorOptional = userService.findByUsername(username);
        if (rejectorOptional.isPresent()) {
            User rejector = rejectorOptional.get();
            LeaveRequest leaveRequest = leaveRequestService.getLeaveRequestById(requestId);
            if (leaveRequest != null && leaveRequest.getLeaveStatus() == LeaveRequest.LeaveStatus.PENDING_MANAGER) {
                leaveRequestService.rejectLeaveRequestByManager(requestId, rejector, rejectionReason);
                redirectAttributes.addFlashAttribute("successMessage", "Leave request rejected successfully.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid request or request already processed.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Error rejecting request. Please try again.");
        }
        return "redirect:/manager/dashboard";
    }

    @GetMapping("/requests/view/{id}")
    public String viewRequestDetails(@PathVariable("id") Long id, Model model, Authentication authentication) {
        Optional<LeaveRequest> leaveRequestOptional = Optional.of(leaveRequestService.getLeaveRequestById(id));
        if (leaveRequestOptional.isPresent()) {
            LeaveRequest leaveRequest = leaveRequestOptional.get();
            String username = authentication.getName();
            Optional<User> currentUserOptional = userService.findByUsername(username);
            if (currentUserOptional.isPresent()) {
                User currentUser = currentUserOptional.get();
                List<LeaveRequest> managedRequests = leaveRequestService.getLeaveRequestsForManager(currentUser.getId());
                if (managedRequests.contains(leaveRequest)) {
                    model.addAttribute("leaveRequest", leaveRequest);
                    return "manager-request-details";
                } else {
                    return "error/unauthorized";
                }
            }
        }
        return "error/not-found";
    }
}