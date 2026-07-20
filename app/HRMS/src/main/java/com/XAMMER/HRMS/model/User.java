package com.XAMMER.HRMS.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collections;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.XAMMER.HRMS.service.S3Service; // Import S3Service
import org.springframework.beans.factory.annotation.Autowired; 
import java.time.Duration; 

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Core & Security Fields ---
    @Column(nullable = false, unique = true)
    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private Roles roles;

    // --- Basic Info ---
    private String firstName;
    private String lastName;
    private String email;
    private String gender;
    private LocalDate birthDate;
    private String phone;
    private String address;

    // --- Professional Details ---
    private String employeeId;
    private LocalDate joiningDate;
    private String department;
    private String jobTitle;
    private String employmentType;
    private String workLocation;
    private String team;
    private String profilePictureUrl;

    // --- Personal & Family Fields ---
    private String nationality;
    private String bloodGroup;
    private String maritalStatus;
    private String fatherName;
    private String motherName;
    private Integer childrenCount;
    private String spouseName;
    private LocalDate spouseDob;

    // --- Financial Fields ---
    private String bankName;
    private String accountNumber;
    private String ifsc;
    private String aadhaar;
    private String pan;
    private String aadhaarDocFilename;
    private String panDocFilename;

    // --- Leave Management ---
    private int totalLeaves = 20;
    private int leavesTaken = 0;

    private String aadhaarDocS3Key;
    private String aadhaarDocOriginalFilename;
    private String panDocS3Key;
    private String panDocOriginalFilename;

    // --- RELATIONSHIPS ---

// Old User.java
@ManyToOne
@JoinColumn(name = "manager_id")
private User manager;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendances = new ArrayList<>(); // Initialize the list to avoid NullPointerException

    // *** CRITICAL CHANGES HERE ***
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeaveRequest> leaveRequests = new ArrayList<>(); // Initialize the list to avoid NullPointerException

    @OneToMany(mappedBy = "manager") // No cascade on delete for subordinates, as they are not "owned" and should not be deleted with the manager.
    private List<User> subordinates = new ArrayList<>(); // Initialize the list

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("user-educations")
    private Set<Education> educations = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("user-professionalExperiences")
    private Set<ProfessionalExperience> professionalExperiences = new HashSet<>();

    // --- Custom Getter Methods for S3 URLs (KEEP THESE) ---
    // public String getAadhaarDocUrl() {
    //     if (this.aadhaarDocS3Key != null && !this.aadhaarDocS3Key.isEmpty()) {
    //         return "https://xrms-document-bucket.s3.ap-south-1.amazonaws.com/" + this.aadhaarDocS3Key;
    //     }
    //     return null;
    // }

    // public String getPanDocUrl() {
    //     if (this.panDocS3Key != null && !this.panDocS3Key.isEmpty()) {
    //         return "https://xrms-document-bucket.s3.ap-south-1.amazonaws.com/" + this.panDocS3Key;
    //     }
    //     return null;
    // }

    // --- HELPER METHODS FOR BIDIRECTIONAL RELATIONSHIPS (ADD THESE BACK) ---
    // These methods ensure the inverse side of the relationship is also set/unset,
    // which is crucial for Hibernate's understanding and for avoiding orphaned records.
    public void addEducation(Education education) {
        if (this.educations == null) {
            this.educations = new HashSet<>();
        }
        this.educations.add(education);
        education.setEmployee(this); // IMPORTANT: Set the inverse side
    }

    public void removeEducation(Education education) {
        if (this.educations != null) {
            this.educations.remove(education);
            education.setEmployee(null); // IMPORTANT: Disassociate the child from the parent
        }
    }

    public void addProfessionalExperience(ProfessionalExperience experience) {
        if (this.professionalExperiences == null) {
            this.professionalExperiences = new HashSet<>();
        }
        this.professionalExperiences.add(experience);
        experience.setUser(this); // IMPORTANT: Set the inverse side
    }

    public void removeProfessionalExperience(ProfessionalExperience experience) {
        if (this.professionalExperiences != null) {
            this.professionalExperiences.remove(experience);
            experience.setUser(null); // IMPORTANT: Disassociate the child from the parent
        }
    }


    // --- UserDetails Methods (REQUIRED by Spring Security, so keep these) ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(roles != null ? roles.name() : "ROLE_ANONYMOUS"));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }

    // --- Standard equals/hashCode/toString (Lombok handles these by default,
    // so explicit ones are usually removed unless you need custom logic) ---
    // Since you have @Data, and the equals/hashCode/toString methods here are simple,
    // you *could* remove them and let Lombok generate them. Keeping them is fine if you prefer.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    // --- DELETE THESE MANUALLY WRITTEN GETTERS/SETTERS (Lombok handles them) ---
    // public Long getId() { return id; }
    // public void setId(Long id) { this.id = id; }
    // public String getUsername() { return username; }
    // public void setUsername(String username) { this.username = username; }
    // ... all other default getters/setters that Lombok generates ...
}