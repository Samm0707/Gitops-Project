package com.XAMMER.HRMS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@Setter
@Entity
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime checkInTime;

    @Column(name = "username")
    private String username;

    @Column(name = "checkout_status")
    private String checkoutStatus = "USER"; // Default value set

    private LocalDateTime checkOutTime;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate = LocalDate.now(); // Set default value

    // Constructors
    public Attendance() {
    }

    public Attendance(User user, LocalDateTime checkInTime) {
        this.user = user;
        this.checkInTime = checkInTime;
        this.attendanceDate = LocalDate.now(); // Initialize attendanceDate in constructor as well
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCheckoutStatus() {
        return checkoutStatus;
    }

    public void setCheckoutStatus(String checkoutStatus) {
        this.checkoutStatus = checkoutStatus;
    }

    // Corrected implementation for isAdminReset()
    public boolean isAdminReset() {
        return "ADMIN_RESET".equals(this.checkoutStatus);
    }

    public Object getAdminReset() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAdminReset'");
    }

    public Attendance getCheckIn() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCheckIn'");
    }

    public Object getCheckOut() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCheckOut'");
    }
    
}