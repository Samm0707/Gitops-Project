package com.XAMMER.HRMS.dto;

public class DashboardMetricsDTO {
    private long totalEmployees;
    private long presentEmployees;
    private long checkedOutEmployees;
    private long pendingLeaveRequests;

    // Constructors, getters, setters
    public DashboardMetricsDTO() {
    }

    public long getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(long totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public long getPresentEmployees() {
        return presentEmployees;
    }

    public void setPresentEmployees(long presentEmployees) {
        this.presentEmployees = presentEmployees;
    }

    public long getCheckedOutEmployees() {
        return checkedOutEmployees;
    }

    public void setCheckedOutEmployees(long checkedOutEmployees) {
        this.checkedOutEmployees = checkedOutEmployees;
    }

    public long getPendingLeaveRequests() {
        return pendingLeaveRequests;
    }

    public void setPendingLeaveRequests(long pendingLeaveRequests) {
        this.pendingLeaveRequests = pendingLeaveRequests;
    }
}