package com.XAMMER.HRMS.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "professional_experiences") // Ensure this matches your actual table name in DB
@Data // This will generate all standard getters, setters, equals, hashCode, and toString
public class ProfessionalExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Placed at the top for convention

    // Core Information
    private String jobTitle;
    private String companyName;
    private String location;
    private String employmentType; // e.g., FULL_TIME, PART_TIME, CONTRACT
    private LocalDate startDate;    // Use LocalDate for dates
    private LocalDate endDate;      // Use LocalDate for dates (can be null for 'Present')

    // Detailed Information
    @Lob // For potentially long descriptions
    @Column(columnDefinition = "TEXT")
    private String description; // Key Responsibilities & Achievements
    private String industry;
    private String departmentTeam; // Renamed from 'Department/Team' to be Java-friendly for field naming

    // Denormalized field (if you still want it, though usually derived from 'user')
    private String username;

    // For file upload (experience letter)
    private String experienceLetterFilename; // Stores the filename in the upload directory
    private String experienceLetterS3Key;
    private String experienceLetterOriginalFilename;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Name of the foreign key column in 'professional_experience' table
    @JsonBackReference("user-professionalExperiences") // Matches @JsonManagedReference in User
    @ToString.Exclude // To prevent circular toString calls
    @EqualsAndHashCode.Exclude // To prevent circular equals/hashCode calls
    private User user; // This field NAME 'user' MUST match mappedBy in User.professionalExperiences

    // Custom getter for S3 URL - KEEP THIS ONE
    // public String getExperienceLetterUrl() {
    //     if (this.experienceLetterS3Key != null && !this.experienceLetterS3Key.isEmpty()) {
    //         // Confirm your S3 region and bucket name
    //         return "https://xrms-document-bucket.s3.ap-south-1.amazonaws.com/" + this.experienceLetterS3Key;
    //     }
    //     return null;
    // }

    // --- NO OTHER MANUAL GETTERS/SETTERS SHOULD BE HERE. LOMBOK HANDLES THEM. ---
    // The default constructor ProfessionalExperience() {} is also not needed with @NoArgsConstructor from Lombok (if you add it)
    // but @Data implies it, so explicitly having it is fine.
}