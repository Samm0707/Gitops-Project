package com.XAMMER.HRMS.repository;

import com.XAMMER.HRMS.model.LeaveRequest;
import com.XAMMER.HRMS.model.LeaveStatus;
import com.XAMMER.HRMS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByUser(User user);
    List<LeaveRequest> findByManagerAndLeaveStatus(User manager, LeaveStatus leaveStatus); // For pending manager requests
    List<LeaveRequest> findByLeaveStatus(com.XAMMER.HRMS.model.LeaveRequest.LeaveStatus pendingAdmin); // For all pending requests (admin)
    List<LeaveRequest> findByUserIn(List<User> subordinates);
      List<LeaveRequest> findByManagerAndLeaveStatus(User manager, LeaveRequest.LeaveStatus leaveStatus);
      List<LeaveRequest> findByUserAndStartDateLessThanEqualAndEndDateGreaterThanEqual(User user, LocalDate date,
            LocalDate date2);


    // // You might still have these if you haven't fully transitioned to LeaveStatus yet
    // List<LeaveRequest> findByManagerAndStatus(User manager, String status);
    // List<LeaveRequest> findByStatus(String status);
}