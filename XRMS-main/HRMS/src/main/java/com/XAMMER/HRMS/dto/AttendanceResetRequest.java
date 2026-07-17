package com.XAMMER.HRMS.dto;

import java.time.LocalDate;

public class AttendanceResetRequest {
    private Long userId;
    private LocalDate date;

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
}
