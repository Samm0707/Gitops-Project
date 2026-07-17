package com.XAMMER.HRMS.controller;

import com.XAMMER.HRMS.dto.*; // Ensure all DTOs are imported
import com.XAMMER.HRMS.model.User;
import com.XAMMER.HRMS.service.UserService;
import com.XAMMER.HRMS.service.S3Service; // <-- Import S3Service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest; // Import this

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional; // Keep if you use Optional directly in this controller


@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final UserService userService;
    private final S3Service s3Service; // <-- Inject S3Service here
    private final DateTimeFormatter displayDateFormatter = DateTimeFormatter.ofPattern("MMMM dd,yyyy"); // Corrected pattern example

    @Autowired
    public EmployeeController(UserService userService, S3Service s3Service) { // <-- Add S3Service to constructor
        this.userService = userService;
        this.s3Service = s3Service; // <-- Assign S3Service
    }

    @GetMapping("/profile")
    public String viewProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // This method relies on userService.findByUsernameWithAllDetails(String username)
        // being defined in UserService and UserRepository and eagerly fetching all collections.
        User employeeEntity = userService.findByUsernameWithAllDetails(username) // This fetches the User entity
                .orElseThrow(() -> new RuntimeException("Employee profile not found for username: " + username));

        // CRITICAL FIX: Pass the s3Service instance to the UserDTO constructor
        UserDTO employeeDTO = new UserDTO(employeeEntity, s3Service); // <-- Correct: Pass s3Service

        model.addAttribute("birthDateFormatted", (employeeDTO.getBirthDate() != null) ? employeeDTO.getBirthDate().format(displayDateFormatter) : "N/A");
        model.addAttribute("joiningDateFormatted", (employeeDTO.getJoiningDate() != null) ? employeeDTO.getJoiningDate().format(displayDateFormatter) : "N/A");
        model.addAttribute("employee", employeeDTO);

        return "employee_profile";
    }

    @PostMapping("/profile/personal/{id}")
    @ResponseBody
    public ResponseEntity<?> updatePersonalDetails(@PathVariable Long id, @RequestBody PersonalDetailsUpdateDTO dto, Authentication auth) {
        if (!isAuthorized(id, auth)) return unauthorizedResponse();
        try {
            UserDTO updatedUser = userService.updatePersonalDetails(id, dto);
            // Assuming userService.updatePersonalDetails already returns a UserDTO that has the S3Service handled.
            return ResponseEntity.ok(Map.of("message", "Personal details updated!", "employee", updatedUser));
        } catch (Exception e) { return errorResponse(e); }
    }

    @PostMapping("/profile/family/{id}")
    @ResponseBody
    public ResponseEntity<?> updateFamilyDetails(@PathVariable Long id, @RequestBody FamilyDetailsDTO dto, Authentication auth) {
        if (!isAuthorized(id, auth)) return unauthorizedResponse();
        try {
            UserDTO updatedUser = userService.updateFamilyDetails(id, dto);
            // Assuming userService.updateFamilyDetails already returns a UserDTO that has the S3Service handled.
            return ResponseEntity.ok(Map.of("message", "Family details updated!", "employee", updatedUser));
        } catch (Exception e) { return errorResponse(e); }
    }

    @PostMapping(value = "/profile/education/{id}", consumes = "multipart/form-data")
    @ResponseBody
    public ResponseEntity<?> updateEducationDetails(
            @PathVariable Long id,
            @RequestPart("educationChanges") List<EducationChangeDTO> changes,
            MultipartHttpServletRequest request,
            Authentication auth) {

        if (!isAuthorized(id, auth)) return unauthorizedResponse();
        try {
            Map<String, MultipartFile> filesMap = request.getFileMap();
            UserDTO updatedUser = userService.updateEducationDetails(id, changes, filesMap);
            // Assuming userService.updateEducationDetails already returns a UserDTO that has the S3Service handled.
            return ResponseEntity.ok(Map.of("message", "Education details updated!", "employee", updatedUser));
        } catch (Exception e) { return errorResponse(e); }
    }

    @PostMapping(value = "/profile/financial/{id}", consumes = "multipart/form-data")
    @ResponseBody
    public ResponseEntity<?> updateFinancialDetails(
            @PathVariable Long id,
            @RequestPart("dto") FinancialDetailsDTO dto,
            @RequestPart(value = "aadhaarDoc", required = false) MultipartFile aadhaarDoc,
            @RequestPart(value = "panDoc", required = false) MultipartFile panDoc,
            Authentication auth) {

        if (!isAuthorized(id, auth)) return unauthorizedResponse();
        try {
            UserDTO updatedUser = userService.updateFinancialDetails(id, dto, aadhaarDoc, panDoc);
            // Assuming userService.updateFinancialDetails already returns a UserDTO that has the S3Service handled.
            return ResponseEntity.ok(Map.of("message", "Financial details updated!", "employee", updatedUser));
        } catch (Exception e) { return errorResponse(e); }
    }

    @PostMapping(value = "/profile/professionalExperience/{id}", consumes = "multipart/form-data")
    @ResponseBody
    public ResponseEntity<?> updateProfessionalExperience(
            @PathVariable Long id,
            @RequestPart("experienceChanges") List<ProfessionalExperienceChangeDTO> changes,
            MultipartHttpServletRequest request,
            Authentication auth) {

        if (!isAuthorized(id, auth)) return unauthorizedResponse();
        try {
            Map<String, MultipartFile> filesMap = request.getFileMap();
            UserDTO updatedUser = userService.updateProfessionalExperience(id, changes, filesMap);
            // Assuming userService.updateProfessionalExperience already returns a UserDTO that has the S3Service handled.
            return ResponseEntity.ok(Map.of("message", "Experience details updated!", "employee", updatedUser));
        } catch (Exception e) { return errorResponse(e); }
    }

    // Helper methods
    private boolean isAuthorized(Long id, Authentication authentication) {
        // This method does a findById, which likely returns a basic User entity.
        // It's okay here as it only accesses username, not lazy collections.
        User user = userService.findById(id).orElse(null);
        return user != null && user.getUsername().equals(authentication.getName());
    }

    private ResponseEntity<Map<String, String>> unauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You are not authorized to perform this action."));
    }

    private ResponseEntity<Map<String, String>> errorResponse(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An error occurred: " + e.getMessage()));
    }
}