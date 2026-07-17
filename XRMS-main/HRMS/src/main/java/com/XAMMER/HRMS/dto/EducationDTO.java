package com.XAMMER.HRMS.dto;

import com.XAMMER.HRMS.model.Education; // Import your Education entity
import com.XAMMER.HRMS.service.S3Service;

import java.time.Duration; // <-- ADD THIS LINE


import lombok.Data; // Recommended for DTOs to reduce boilerplate

// Using @Data for clean DTOs (optional, but good practice)
@Data
public class EducationDTO {
    private Long id;
    private String level;
    private String degree;
    private String fieldOfStudy;
    private String institute;
    private String year; // Storing as String in DTO as you had it
    private String score;
    // Removed certFilename, as it maps to certOriginalFilename now
    private String certOriginalFilename;
    private String certUrl; // This is the S3 URL

    // IMPORTANT: Removed 'private String username;' from DTO.
    // If you need the username, it should come from the UserDTO or be retrieved from the associated User entity.
    // If you absolutely must have it here, it should be derived from the 'employee' relationship:
    // private String username;

    // Default constructor (required by Jackson for deserialization from frontend)
    // Lombok's @NoArgsConstructor (if added) handles this
    public EducationDTO() {}

    // Constructor to convert from Education entity to EducationDTO
    public EducationDTO(Education education, S3Service s3Service) {
        this.id = education.getId();
        this.level = education.getLevel();
        this.degree = education.getDegree();
        this.fieldOfStudy = education.getFieldOfStudy();
        this.institute = education.getInstitute();
        // Convert Integer year from entity to String year for DTO
        this.year = (education.getYear() != null) ? education.getYear().toString() : null;
        this.score = education.getScore();
        // Mapped certOriginalFilename from entity
        this.certOriginalFilename = education.getCertOriginalFilename();
        // Populate the certUrl by calling the new method on the entity
        this.certUrl = (education.getCertS3Key() != null)
                     ? s3Service.getPresignedUrl(education.getCertS3Key(), Duration.ofMinutes(5)) : null;

        // If you need username in DTO, and it's removed from entity, derive it from the 'employee' relationship
        // Ensure the 'employee' field is loaded or available in the entity.
        // This line assumes 'employee' is available on the 'education' object passed here.
        // if (education.getEmployee() != null) {
        //     this.username = education.getEmployee().getUsername();
        // } else {
        //     this.username = null; // Or handle as appropriate
        // }
    }

    // --- Getters and Setters are now handled by @Data if you add it. ---
    // If not using @Data, you would explicitly list all getters/setters as you had before.
    // Given the previous Lombok-related issues, using @Data on DTOs is generally cleaner.

    // @Override
    // public String toString() {
    //     // @Data handles toString as well. If you provide your own, it won't override.
    //     return "EducationDTO{" +
    //             "id=" + id +
    //             ", level='" + level + '\'' +
    //             ", degree='" + degree + '\'' +
    //             ", fieldOfStudy='" + fieldOfStudy + '\'' +
    //             ", institute='" + institute + '\'' +
    //             ", year='" + year + '\'' +
    //             ", score='" + score + '\'' +
    //             ", certOriginalFilename='" + certOriginalFilename + '\'' +
    //             ", certUrl='" + certUrl + '\'' +
    //             '}';
    // }
}