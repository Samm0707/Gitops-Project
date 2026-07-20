package com.XAMMER.HRMS.service;

import com.XAMMER.HRMS.model.LeaveRequest;
import com.XAMMER.HRMS.model.Roles;
import com.XAMMER.HRMS.model.User;
import com.XAMMER.HRMS.repository.LeaveRequestRepository;
import com.XAMMER.HRMS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public LeaveRequest submitLeaveRequest(User applicant, LeaveRequest leaveRequest) {
        leaveRequest.setUser(applicant);
        leaveRequest.setSubmissionDate(LocalDateTime.now());

        // Calculate the number of days
        long days = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
        leaveRequest.setNumberOfDays((int) days);

        if (applicant.getRoles() == null) {
            throw new IllegalArgumentException("User role cannot be null");
        }

        if (applicant.getRoles() == Roles.ROLE_USER) {
            User manager = applicant.getManager();
            leaveRequest.setManager(manager);
            leaveRequest.setLeaveStatus(LeaveRequest.LeaveStatus.PENDING_MANAGER);

            LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);

            if (manager != null) {
                emailService.sendLeaveRequestNotification(
                        manager.getEmail(),
                        applicant.getUsername() + " " + applicant.getLastName(),
                        leaveRequest.getStartDate(),
                        leaveRequest.getEndDate(),
                        leaveRequest.getReason()
                );
            }

            return savedRequest;

        } else if (applicant.getRoles() == Roles.ROLE_USER_MANAGER) {
            List<User> admins = userRepository.findByRoles(Roles.ROLE_ADMIN);
            leaveRequest.setLeaveStatus(LeaveRequest.LeaveStatus.PENDING_ADMIN);
            leaveRequest.setManagerApprovalStatus(LeaveRequest.ApprovalStatus.APPROVED);
            leaveRequest.setAdmin(admins.isEmpty() ? null : admins.get(0));

            LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);

            notifyAdmins(savedRequest, "(Manager's Request)");
            //emailService.sendLeaveRequestNotification(applicant.getEmail(),
            //                                        "Your Leave Request has been submitted for Admin Approval");

            return savedRequest;
        }

        return null;
    }


    @Override
    public LeaveRequest updateLeaveRequestStatus(Long id, String status, User approver, String rejectionReason) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Leave request not found with id: " + id));

        try {
            LeaveRequest.LeaveStatus newLeaveStatus = LeaveRequest.LeaveStatus.valueOf(status.toUpperCase());
            leaveRequest.setLeaveStatus(newLeaveStatus);

            if (newLeaveStatus == LeaveRequest.LeaveStatus.APPROVED) {
                if (approver.getRoles() == Roles.ROLE_USER_MANAGER) {
                    List<User> admins = userRepository.findByRoles(Roles.ROLE_ADMIN);
                    leaveRequest.setAdmin(admins.isEmpty() ? null : admins.get(0));
                    leaveRequestRepository.save(leaveRequest);
                    notifyAdmins(leaveRequest, "(Approved by Manager)");
                } else if (approver.getRoles() == Roles.ROLE_ADMIN) {
                    leaveRequest.setAdmin(approver);
                    leaveRequestRepository.save(leaveRequest);
                    emailService.sendLeaveRequestApprovalNotification(
                            leaveRequest.getUser().getEmail(),
                            leaveRequest.getUser().getUsername() + " " + leaveRequest.getUser().getLastName());
                }
            } else if (newLeaveStatus == LeaveRequest.LeaveStatus.REJECTED) {
                leaveRequest.setRejectionReason(rejectionReason);
                leaveRequestRepository.save(leaveRequest);
                emailService.sendLeaveRequestRejectionNotification(
                        leaveRequest.getUser().getEmail(),
                        leaveRequest.getUser().getUsername() + " " + leaveRequest.getUser().getLastName(),
                        rejectionReason);
            }

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid LeaveStatus: " + status);
        }

        return leaveRequest;
    }

    @Override
    public LeaveRequest getLeaveRequestById(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Leave request not found with id: " + id));
    }

    @Override
    public List<LeaveRequest> getLeaveRequestsByUser(User user) {
        return leaveRequestRepository.findByUser(user);
    }

    @Override
    public List<LeaveRequest> getPendingRequestsForManager(User manager) {
        return leaveRequestRepository.findByManagerAndLeaveStatus(manager, LeaveRequest.LeaveStatus.PENDING_MANAGER);
    }

    @Override
    public List<LeaveRequest> getAllPendingRequests() {
        return leaveRequestRepository.findByLeaveStatus(LeaveRequest.LeaveStatus.PENDING_ADMIN);
    }

    @Override
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }

    @Override
    public void approveLeaveRequest(Long id, User approver) {
        updateLeaveRequestStatus(id, LeaveRequest.LeaveStatus.APPROVED.name(), approver, null);
    }

    @Override
    public void rejectLeaveRequest(Long id, User rejector, String reason) {
        updateLeaveRequestStatus(id, LeaveRequest.LeaveStatus.REJECTED.name(), rejector, reason);
    }

    @Override
    public List<LeaveRequest> getLeaveRequestsForUser(User user) {
        return leaveRequestRepository.findByUser(user);
    }

    @Override
    public List<LeaveRequest> getLeaveRequestsForManager(Long managerId) {
        List<User> subordinates = userRepository.findByManagerId(managerId);
        return leaveRequestRepository.findByUserIn(subordinates);
    }

    @Override
    public void approveLeaveRequestByManager(Long requestId, User manager) {
        LeaveRequest request = getLeaveRequestById(requestId);
        if (request != null && request.getLeaveStatus() == LeaveRequest.LeaveStatus.PENDING_MANAGER) {
            request.setManagerApprovalStatus(LeaveRequest.ApprovalStatus.APPROVED);
            request.setLeaveStatus(LeaveRequest.LeaveStatus.PENDING_ADMIN);
            request.setManager(manager);
            leaveRequestRepository.save(request);
            notifyAdmins(request, "(Approved by Manager)");
        }
    }

    @Override
    public void rejectLeaveRequestByManager(Long requestId, User manager, String rejectionReason) {
        LeaveRequest request = getLeaveRequestById(requestId);
        if (request != null && request.getLeaveStatus() == LeaveRequest.LeaveStatus.PENDING_MANAGER) {
            request.setManagerApprovalStatus(LeaveRequest.ApprovalStatus.REJECTED);
            request.setLeaveStatus(LeaveRequest.LeaveStatus.REJECTED);
            request.setRejectionReason(rejectionReason);
            request.setManager(manager);
            leaveRequestRepository.save(request);
            emailService.sendLeaveRequestRejectionNotification(
                    request.getUser().getEmail(),
                    request.getUser().getUsername() + " " + request.getUser().getLastName(),
                    rejectionReason);
        }
    }

    @Override
    public void approveLeaveRequestByAdmin(Long requestId, User admin) {
        LeaveRequest request = getLeaveRequestById(requestId);
        if (request != null &&
                request.getLeaveStatus() == LeaveRequest.LeaveStatus.PENDING_ADMIN &&
                request.getManagerApprovalStatus() == LeaveRequest.ApprovalStatus.APPROVED) {
            updateLeaveRequestStatus(requestId, LeaveRequest.LeaveStatus.APPROVED.name(), admin, null);
        }
    }

    @Override
    public void rejectLeaveRequestByAdmin(Long requestId, User admin, String rejectionReason) {
        LeaveRequest request = getLeaveRequestById(requestId);
        if (request != null &&
                request.getLeaveStatus() == LeaveRequest.LeaveStatus.PENDING_ADMIN &&
                request.getManagerApprovalStatus() == LeaveRequest.ApprovalStatus.APPROVED) {
            updateLeaveRequestStatus(requestId, LeaveRequest.LeaveStatus.REJECTED.name(), admin, rejectionReason);
        }
    }

    private void notifyAdmins(LeaveRequest request, String messageSuffix) {
        List<User> admins = userRepository.findByRoles(Roles.ROLE_ADMIN);
        for (User admin : admins) {
            emailService.sendLeaveRequestNotification(
                    admin.getEmail(),
                    request.getUser().getUsername() + " " + request.getUser().getLastName() + " " + messageSuffix,
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getReason()
            );
        }
    }

@Override
public void cancelLeaveRequest(Long requestId, User user) {
    LeaveRequest request = getLeaveRequestById(requestId);

    if (request.getUser().equals(user) &&
        (request.getLeaveStatus() == LeaveRequest.LeaveStatus.PENDING_MANAGER ||
         request.getLeaveStatus() == LeaveRequest.LeaveStatus.PENDING_ADMIN)) {

        LeaveRequest.LeaveStatus originalStatus = request.getLeaveStatus();
        request.setLeaveStatus(LeaveRequest.LeaveStatus.REJECTED);
        request.setRejectionReason("Cancelled by user");
        leaveRequestRepository.save(request);

        // Notify the appropriate approver only
        if (originalStatus == LeaveRequest.LeaveStatus.PENDING_MANAGER && request.getManager() != null) {
            emailService.sendLeaveRequestCancellationNotificationToApprover(
                    request.getManager().getEmail(),
                    request.getUser().getUsername() + " " + request.getUser().getLastName(),
                    request.getStartDate(),
                    request.getEndDate()
            );
        } else if (originalStatus == LeaveRequest.LeaveStatus.PENDING_ADMIN && request.getAdmin() != null) {
            emailService.sendLeaveRequestCancellationNotificationToApprover(
                    request.getAdmin().getEmail(),
                    request.getUser().getUsername() + " " + request.getUser().getLastName(),
                    request.getStartDate(),
                    request.getEndDate()
            );
        }

    } 
}


@Override
public List<LeaveRequest> getLeaveRequestsForUserAndDate(User user, LocalDate date) {
    if (user == null) {
        return List.of(); // Or handle null user appropriately
    }
    return leaveRequestRepository.findByUserAndStartDateLessThanEqualAndEndDateGreaterThanEqual(user, date, date);
}


@Override
public boolean isEmployeeOnLeave(Long id, LocalDate effectiveDate) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'isEmployeeOnLeave'");
}

}