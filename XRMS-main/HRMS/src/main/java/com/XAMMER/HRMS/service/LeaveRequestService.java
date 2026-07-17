package com.XAMMER.HRMS.service;

import com.XAMMER.HRMS.model.LeaveRequest;
import com.XAMMER.HRMS.model.User;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestService {
    LeaveRequest submitLeaveRequest(User applicant, LeaveRequest leaveRequest);
    LeaveRequest updateLeaveRequestStatus(Long id, String status, User approver, String rejectionReason);
    LeaveRequest getLeaveRequestById(Long id);
    List<LeaveRequest> getLeaveRequestsByUser(User user);
    List<LeaveRequest> getPendingRequestsForManager(User manager);
    List<LeaveRequest> getAllPendingRequests();
    List<LeaveRequest> getAllLeaveRequests();
    void approveLeaveRequest(Long id, User approver);
    void rejectLeaveRequest(Long id, User rejector, String reason);
    List<LeaveRequest> getLeaveRequestsForUser(User user);
    List<LeaveRequest> getLeaveRequestsForManager(Long id);

    // New methods for manager-specific approval/rejection
    void approveLeaveRequestByManager(Long requestId, User manager);
    void rejectLeaveRequestByManager(Long requestId, User manager, String rejectionReason);
    void approveLeaveRequestByAdmin(Long requestId, User admin);
    void rejectLeaveRequestByAdmin(Long requestId, User admin, String rejectionReason);
    void cancelLeaveRequest(Long id, User user);
    List<LeaveRequest> getLeaveRequestsForUserAndDate(User orElse, LocalDate date);
    boolean isEmployeeOnLeave(Long id, LocalDate effectiveDate);
}