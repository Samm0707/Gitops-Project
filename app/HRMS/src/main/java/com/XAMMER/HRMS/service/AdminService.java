package com.XAMMER.HRMS.service;

import com.XAMMER.HRMS.dto.DashboardMetricsDTO;

import java.time.LocalDate;
import java.util.List;

public interface AdminService {
    DashboardMetricsDTO getDashboardMetrics();
    List<String> getRecentActivity();
    void resetAttendance(Long userId, LocalDate date);
}