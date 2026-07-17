package com.XAMMER.HRMS.dto;

import com.XAMMER.HRMS.model.ProfessionalExperience;
import lombok.Data;
import lombok.NoArgsConstructor; // Keep this
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.XAMMER.HRMS.service.S3Service; // Import S3Service
import java.time.Duration; // Import Duration

@Data
@NoArgsConstructor // Lombok will generate a no-arg constructor
public class ProfessionalExperienceDTO {

    private Long id;
    private String jobTitle;
    private String companyName;
    private String location;
    private String employmentType;
    private String startDate;
    private String endDate;
    private String description;
    private String industry;
    private String departmentTeam;
    private String username; // Only if present in entity and needed in DTO
    private String experienceLetterFilename;
    private String experienceLetterOriginalFilename;
    private String experienceLetterUrl; // This will hold the pre-signed URL

    // --- CRITICAL FIX: Add this constructor with S3Service parameter ---
    public ProfessionalExperienceDTO(ProfessionalExperience experience, S3Service s3Service) {
        this.id = experience.getId();
        this.jobTitle = experience.getJobTitle();
        this.companyName = experience.getCompanyName();
        this.location = experience.getLocation();
        this.employmentType = experience.getEmploymentType();
        this.startDate = (experience.getStartDate() != null) ? experience.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
        this.endDate = (experience.getEndDate() != null) ? experience.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
        this.description = experience.getDescription();
        this.industry = experience.getIndustry();
        this.departmentTeam = experience.getDepartmentTeam();
        this.username = experience.getUsername(); // Map username from entity
        this.experienceLetterFilename = experience.getExperienceLetterFilename();
        this.experienceLetterOriginalFilename = experience.getExperienceLetterOriginalFilename();

        // Generate the pre-signed URL here using the S3Service
        this.experienceLetterUrl = (experience.getExperienceLetterS3Key() != null)
                                 ? s3Service.getPresignedUrl(experience.getExperienceLetterS3Key(), Duration.ofMinutes(5)) : null;
    }
}