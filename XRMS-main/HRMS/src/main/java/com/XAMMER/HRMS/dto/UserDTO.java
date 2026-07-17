package com.XAMMER.HRMS.dto;

import com.XAMMER.HRMS.model.User;
import com.XAMMER.HRMS.model.Roles;
import com.XAMMER.HRMS.model.ProfessionalExperience;
import com.XAMMER.HRMS.model.Education;

import lombok.Data; // Import Lombok's Data annotation
import lombok.NoArgsConstructor; // Recommended for DTOs for default constructor

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.XAMMER.HRMS.service.S3Service; // Import S3Service
import org.springframework.beans.factory.annotation.Autowired; // For Autowired
import java.time.Duration; // For Duration

@Data // This generates all getters, setters, equals, hashCode, and toString automatically
@NoArgsConstructor // Generates the default no-argument constructor
public class UserDTO {
    

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String gender;
    private LocalDate birthDate;
    private String phone;
    private String address;
    private String employeeId;
    private LocalDate joiningDate;
    private String department;
    private String jobTitle;
    private String employmentType;
    private String workLocation;
    private String team;
    private String profilePictureUrl;
    private String nationality;
    private String bloodGroup;
    private String maritalStatus;
    private String fatherName;
    private String motherName;
    private Integer childrenCount;
    private String spouseName;
    private LocalDate spouseDob;
    private String bankName;
    private String accountNumber;
    private String ifsc;
    private String aadhaar;
    private String pan;
    private String aadhaarDocFilename;
    private String panDocFilename;
    private String aadhaarDocUrl; // Assuming you have a way to generate this from the entity
    private String aadhaarDocOriginalFilename;
    private String panDocUrl;     // Assuming you have a way to generate this from the entity
    private String panDocOriginalFilename;
    private int totalLeaves;
    private int leavesTaken;
    private Roles roles;
    private Long managerId;
    private String managerFullName;
    

    private List<EducationDTO> educations = new ArrayList<>(); // Initialize fields
    private List<ProfessionalExperienceDTO> professionalExperiences = new ArrayList<>();
    private String role;
    private String fullName;


    // Custom constructor to convert from User entity to UserDTO
    // We keep this to handle the mapping logic
    public UserDTO(User user, S3Service s3Service) {
        // No need to call this() here if @NoArgsConstructor initializes collections to new ArrayList
        // or if you always ensure they are not null when accessed.
        // If you want to explicitly initialize, you can add `super();` or `this.educations = new ArrayList<>();` etc.
        // For @Data, the fields are initialized when the object is created.

        this.id = user.getId();
        this.username = user.getUsername();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.gender = user.getGender();
        this.birthDate = user.getBirthDate();
        this.phone = user.getPhone();
        this.address = user.getAddress();
        this.employeeId = user.getEmployeeId();
        this.joiningDate = user.getJoiningDate();
        this.department = user.getDepartment();
        this.jobTitle = user.getJobTitle();
        this.employmentType = user.getEmploymentType();
        this.workLocation = user.getWorkLocation();
        this.team = user.getTeam();
        this.profilePictureUrl = user.getProfilePictureUrl();
        this.nationality = user.getNationality();
        this.bloodGroup = user.getBloodGroup();
        this.maritalStatus = user.getMaritalStatus();
        this.fatherName = user.getFatherName();
        this.motherName = user.getMotherName();
        this.childrenCount = user.getChildrenCount();
        this.spouseName = user.getSpouseName();
        this.spouseDob = user.getSpouseDob();
        this.bankName = user.getBankName();
        this.accountNumber = user.getAccountNumber();
        this.ifsc = user.getIfsc();
        this.aadhaar = user.getAadhaar();
        this.pan = user.getPan();
        this.aadhaarDocFilename = user.getAadhaarDocFilename();
        this.panDocFilename = user.getPanDocFilename();

        // Assuming User entity has getAadhaarDocUrl() and getPanDocUrl() methods
        // If not, you need to add these custom getters to your User entity.
        // this.aadhaarDocUrl = user.getAadhaarDocUrl();
        this.aadhaarDocOriginalFilename = user.getAadhaarDocOriginalFilename();
        // this.panDocUrl = user.getPanDocUrl();
        this.panDocOriginalFilename = user.getPanDocOriginalFilename();

        this.aadhaarDocUrl = (user.getAadhaarDocS3Key() != null)
                           ? s3Service.getPresignedUrl(user.getAadhaarDocS3Key(), Duration.ofMinutes(5)) : null;
        this.panDocUrl = (user.getPanDocS3Key() != null)
                       ? s3Service.getPresignedUrl(user.getPanDocS3Key(), Duration.ofMinutes(5)) : null;

        this.totalLeaves = user.getTotalLeaves();
        this.leavesTaken = user.getLeavesTaken();
        this.roles = user.getRoles();

        if (user.getManager() != null) {
            this.managerId = user.getManager().getId();
            this.managerFullName = user.getManager().getFirstName() + " " + user.getManager().getLastName();
        } else {
            this.managerId = null;
            this.managerFullName = null;
        }

        // Convert Set<Education> from User entity to List<EducationDTO>
        if (user.getEducations() != null) {
            this.educations = user.getEducations().stream()
                                  .map(edu -> new EducationDTO(edu, s3Service))
                                  .collect(Collectors.toList()); // <--- CHANGE to toList()
        } else {
            this.educations = new ArrayList<>(); // <--- CHANGE to ArrayList initialization
        }

        // Convert Set<ProfessionalExperience> from User entity to List<ProfessionalExperienceDTO>
        if (user.getProfessionalExperiences() != null) {
            this.professionalExperiences = user.getProfessionalExperiences().stream()
                                                .map(exp -> new ProfessionalExperienceDTO(exp, s3Service))
                                                .collect(Collectors.toList()); // <--- CHANGE to toList()
        } else {
            this.professionalExperiences = new ArrayList<>(); // <--- CHANGE to ArrayList initialization
        }
        // No else needed here, as `professionalExperiences` is initialized by `new ArrayList<>()` or `@NoArgsConstructor`
    }

    public UserDTO(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.gender = user.getGender();
        this.birthDate = user.getBirthDate();
        this.maritalStatus = user.getMaritalStatus();
        this.jobTitle = user.getJobTitle();
        this.joiningDate = user.getJoiningDate();
        this.department = user.getDepartment();
        this.roles = user.getRoles();



        this.username = user.getUsername();
        if (user.getRoles() != null) {
            this.role = user.getRoles().name(); // Get the string representation of the enum
        }
        this.fullName = user.getFirstName() + " " + user.getLastName();

        // Handle manager mapping to avoid LazyInitializationException and circular dependency
        if (user.getManager() != null) {
            this.managerId = user.getManager().getId();
            this.managerFullName = user.getManager().getFirstName() + " " + user.getManager().getLastName();
        }
        // IMPORTANT: DO NOT set the password here when converting from User entity to UserDTO
        // this.password = user.getPassword(); // <-- NO! Passwords should not be sent back to frontend.
    }

    // You DO NOT need to write any explicit getters, setters, or toString methods here with @Data.
}