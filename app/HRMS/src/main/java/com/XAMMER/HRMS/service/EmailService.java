package com.XAMMER.HRMS.service;

import java.time.LocalDate;

public interface EmailService {
    void sendLeaveRequestNotification(String to, String requesterName);
    void sendLeaveRequestApprovalNotification(String to, String requesterName);
    void sendLeaveRequestRejectionNotification(String to, String requesterName, String reason);
        void sendLeaveRequestNotification(String to, String requesterName, LocalDate appliedDate, String reason);
            void sendLeaveRequestNotification(String to, String requesterName, LocalDate startDate, LocalDate endDate, String reason);
            void sendLeaveRequestCancellationNotification(String email, String string, LocalDate startDate,
                    LocalDate endDate);
            void sendLeaveRequestCancellationNotificationToApprover(String email, String string, LocalDate startDate,
                    LocalDate endDate);
    void sendWelcomeEmail(String to, String firstName, String username, String password, String loginUrl);

}