package com.XAMMER.HRMS.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;

    @Column(name = "leave_type", nullable = false)
    private String leaveType;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "manager_approval_status", nullable = false)
    private ApprovalStatus managerApprovalStatus = ApprovalStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_approval_status", nullable = false)
    private ApprovalStatus adminApprovalStatus = ApprovalStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_status", nullable = false)
    private LeaveStatus leaveStatus;

    @Column(name = "team_name", nullable = false)
    private String teamName;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    private LocalDate startDate;
    private LocalDate endDate;

    private Integer numberOfDays;

    private LocalDateTime submissionDate;

    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    public enum LeaveStatus {
        PENDING_MANAGER,
        PENDING_ADMIN,
        APPROVED,
        REJECTED
    }

    // Lombok's @Data will generate these, but keeping them explicit for clarity if you prefer
    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LeaveStatus getLeaveStatus() {
        return leaveStatus;
    }

    public void setLeaveStatus(LeaveStatus leaveStatus) {
        this.leaveStatus = leaveStatus;
    }

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }

    public Integer getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(Integer numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public LeaveRequest orElse(Object object) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'orElse'");
    }
}