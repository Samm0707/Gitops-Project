package com.XAMMER.HRMS.service;

import com.XAMMER.HRMS.dto.*;
import com.XAMMER.HRMS.model.Education;
import com.XAMMER.HRMS.model.ProfessionalExperience;
import com.XAMMER.HRMS.model.User;
import com.XAMMER.HRMS.repository.EducationRepository;
import com.XAMMER.HRMS.repository.ProfessionalExperienceRepository;
import com.XAMMER.HRMS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EducationRepository educationRepository;
    private final ProfessionalExperienceRepository professionalExperienceRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           EducationRepository educationRepository,
                           ProfessionalExperienceRepository professionalExperienceRepository,
                           EmailService emailService,
                           PasswordEncoder passwordEncoder,
                           S3Service s3Service) {
        this.userRepository = userRepository;
        this.educationRepository = educationRepository;
        this.professionalExperienceRepository = professionalExperienceRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.s3Service = s3Service;
    }
    
    // ... other existing methods (findAll, save, delete, getUpcomingBirthdays, etc.) ...
    // Ensure these methods are complete as per your UserService interface.
    // The following are implementations for the specific profile update methods.

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public void delete(Long id) { // This seems to be for general User, ensure it's not confused with profile part deletion
        userRepository.deleteById(id);
    }

    @Override
    public List<User> getUpcomingBirthdays() {
        LocalDate today = LocalDate.now();
        LocalDate todayPlusTwoWeeks = today.plus(12, ChronoUnit.MONTHS);

        return userRepository.findAll().stream()
                .filter(user -> {
                    if (user.getBirthDate() == null) {
                        return false;
                    }
                    LocalDate birthdayThisYear = user.getBirthDate().withYear(today.getYear());
                    if (birthdayThisYear.isBefore(today)) {
                        birthdayThisYear = birthdayThisYear.plusYears(1);
                    }
                    return !birthdayThisYear.isBefore(today) && !birthdayThisYear.isAfter(todayPlusTwoWeeks);
                })
                .sorted((u1, u2) -> {
                    LocalDate b1 = u1.getBirthDate().withYear(today.getYear());
                    if (b1.isBefore(today)) b1 = b1.plusYears(1);
                    LocalDate b2 = u2.getBirthDate().withYear(today.getYear());
                    if (b2.isBefore(today)) b2 = b2.plusYears(1);
                    return b1.compareTo(b2);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return userRepository.findById(id);
    }
    
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Override
    public void saveUser(User user) { // Consider if this is different from save(User user)
        userRepository.save(user);
    }

    @Override
    public List<String> getAllUsernames() {
        return userRepository.findAllUsernames(); // Assuming a more efficient query in repository
    }

    @Override
    public List<String> searchUsernames(String query) {
         return userRepository.findUsernamesByUsernameContainingIgnoreCase(query);
    }
    
    @Override
    public List<String> findUsernamesContaining(String query) {
        return userRepository.findUsernamesByUsernameContainingIgnoreCase(query);
    }
    
    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    @Override
    public List<User> findAllEmployees() {
        // Define what constitutes an "employee" if different from all users
        return userRepository.findAll(); 
    }

    @Override
    @Transactional
    public User addUser(User user) {
        // ... (your existing addUser logic)
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            user.setUsername(user.getEmail().split("@")[0]);
        }
        String rawPassword = user.getPassword(); 
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        user.setPassword(passwordEncoder.encode(rawPassword));
        if (user.getJoiningDate() == null) {
            user.setJoiningDate(LocalDate.now());
        }
        User savedUser = userRepository.save(user);
        String loginUrl = "https://xrms.xammer.in/login"; // Consider making this configurable
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFirstName(), savedUser.getUsername(), rawPassword, loginUrl);
        return savedUser;
    }

    @Override
    public List<User> searchAndFilterUsers(String query, String department) {
        // ... (your existing searchAndFilterUsers logic)
        if ((query != null && !query.isEmpty()) && (department != null && !department.isEmpty())) {
            return userRepository.findByFirstNameContainingIgnoreCaseAndDepartmentContainingIgnoreCase(query, department);
        } else if (query != null && !query.isEmpty()) {
            return userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query, query);
        } else if (department != null && !department.isEmpty()) {
            return userRepository.findByDepartmentContainingIgnoreCase(department);
        }
        return userRepository.findAll();
    }
    
@Override
    @Transactional // Add @Transactional for methods that modify data
    public User updateUser(Long id, User userDetails) {
        Optional<User> existingUserOptional = userRepository.findById(id);

        if (existingUserOptional.isPresent()) {
            User existingUser = existingUserOptional.get();

            // Update all the fields that can be edited from the frontend
            existingUser.setFirstName(userDetails.getFirstName());
            existingUser.setLastName(userDetails.getLastName());
            existingUser.setEmail(userDetails.getEmail());
            existingUser.setDepartment(userDetails.getDepartment());
            existingUser.setJobTitle(userDetails.getJobTitle());
            existingUser.setPhone(userDetails.getPhone());
            existingUser.setBirthDate(userDetails.getBirthDate()); 
            // Assuming you have this field and want to update
            existingUser.setJoiningDate(userDetails.getJoiningDate()); // Uncomment if updatable
            // existingUser.setPassword(userDetails.getPassword()); 
            existingUser.setRoles(userDetails.getRoles()); 
            


            // Be careful with password, roles, etc. - usually not updated via employee edit
            // If username is derived from email, update it
            existingUser.setUsername(userDetails.getEmail().split("@")[0]);

            // Save the updated entity
            return userRepository.save(existingUser);
        } else {
            // Throw an exception that your controller can catch and convert to a 404
            throw new RuntimeException("User not found with ID: " + id);
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) { // General user deletion
        // ... (your existing deleteUser logic with manager reassignment) ...
         User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found for deletion: " + id));
        userRepository.findByManager(userToDelete).forEach(subordinate -> {
            subordinate.setManager(null); // Or reassign to another default manager
            userRepository.save(subordinate);
        });
        userRepository.delete(userToDelete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findAllUsersDTO() {
        List<User> users = userRepository.findAllWithEducationsAndProfessionalExperiencesAndManager();
        return users.stream()
                .map(user -> new UserDTO(user, s3Service))
                .collect(Collectors.toList());
    }

    
    @Override
    public List<String> findUsernamesByQuery(String query) {
        return userRepository.findUsernamesByUsernameContainingIgnoreCase(query);
    }
    
    @Override
    public boolean isUserOwnerOfProfile(String currentUsername, Long id) {
        return userRepository.findById(id)
            .map(user -> user.getUsername().equals(currentUsername))
            .orElse(false);
    }


     @Override
    @Transactional
    public UserDTO updatePersonalDetails(Long id, PersonalDetailsUpdateDTO dto) {
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        if (dto.getBirthDate() != null && !dto.getBirthDate().isEmpty()) {
            userToUpdate.setBirthDate(LocalDate.parse(dto.getBirthDate()));
        } else {
            userToUpdate.setBirthDate(null);
        }
        userToUpdate.setGender(dto.getGender());
        userToUpdate.setPhone(dto.getPhone());
        userToUpdate.setAddress(dto.getAddress());
        userToUpdate.setNationality(dto.getNationality());
        userToUpdate.setBloodGroup(dto.getBloodGroup());

        User savedUser = userRepository.save(userToUpdate);
        return new UserDTO(savedUser, s3Service); // <-- Correct: Pass s3Service
    }


    @Override
    @Transactional
    public UserDTO updateFamilyDetails(Long id, FamilyDetailsDTO dto) {
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        userToUpdate.setFatherName(dto.getFatherName());
        userToUpdate.setMotherName(dto.getMotherName());
        userToUpdate.setMaritalStatus(dto.getMaritalStatus());
        userToUpdate.setChildrenCount(dto.getChildrenCount());
        
        if ("Married".equalsIgnoreCase(dto.getMaritalStatus())) {
            userToUpdate.setSpouseName(dto.getSpouseName());
            if (dto.getSpouseDob() != null && !dto.getSpouseDob().isEmpty()) {
                userToUpdate.setSpouseDob(LocalDate.parse(dto.getSpouseDob()));
            } else {
                userToUpdate.setSpouseDob(null);
            }
        } else {
            userToUpdate.setSpouseName(null);
            userToUpdate.setSpouseDob(null);
        }
        
        User savedUser = userRepository.save(userToUpdate);
        return new UserDTO(savedUser, s3Service);
    }

    @Override
    @Transactional
    public UserDTO updateFinancialDetails(Long id, FinancialDetailsDTO dto, MultipartFile aadhaarDocFile, MultipartFile panDocFile) {
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        userToUpdate.setBankName(dto.getBankName());
        userToUpdate.setAccountNumber(dto.getAccountNumber());
        userToUpdate.setIfsc(dto.getIfsc());
        userToUpdate.setAadhaar(dto.getAadhaar()); // Aadhaar number
        userToUpdate.setPan(dto.getPan());       // PAN number

        try {
            String s3Directory = "financial-docs/" + userToUpdate.getId() + "/";

            if (aadhaarDocFile != null && !aadhaarDocFile.isEmpty()) {
                if (userToUpdate.getAadhaarDocS3Key() != null) { // Assuming field is aadhaarDocS3Key
                    s3Service.deleteFile(userToUpdate.getAadhaarDocS3Key());
                }
                S3UploadResult aadhaarUpload = s3Service.uploadFileAndGetDetails(aadhaarDocFile, s3Directory + "aadhaar/");
                userToUpdate.setAadhaarDocS3Key(aadhaarUpload.getStorageKey()); // Store S3 Key
                userToUpdate.setAadhaarDocOriginalFilename(aadhaarUpload.getOriginalFilename());
                // The URL can be derived in UserDTO or by s3Service.getFileUrl(storageKey)
            }
            if (panDocFile != null && !panDocFile.isEmpty()) {
                if (userToUpdate.getPanDocS3Key() != null) { // Assuming field is panDocS3Key
                    s3Service.deleteFile(userToUpdate.getPanDocS3Key());
                }
                S3UploadResult panUpload = s3Service.uploadFileAndGetDetails(panDocFile, s3Directory + "pan/");
                userToUpdate.setPanDocS3Key(panUpload.getStorageKey()); // Store S3 Key
                userToUpdate.setPanDocOriginalFilename(panUpload.getOriginalFilename());
            }
        } catch (IOException e) {
            // Consider a more specific exception or logging
            throw new RuntimeException("Failed to store financial document in S3: " + e.getMessage(), e);
        }
        
        User savedUser = userRepository.save(userToUpdate);
        return new UserDTO(savedUser, s3Service); // UserDTO needs to construct S3 URLs from keys if not stored directly
    }

    @Override
    @Transactional
    public UserDTO updateEducationDetails(Long userId, List<EducationChangeDTO> educationChanges, Map<String, MultipartFile> filesMap) {
        User userToUpdate = userRepository.findByUsernameWithAllDetails(userId) // Fetch with collections
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        String s3DirectoryBase = "education-certs/" + userToUpdate.getId() + "/";

        for (EducationChangeDTO change : educationChanges) {
            try {
                String filePartKeyPrefix;
                String entityIdForFile;

                if (EducationChangeDTO.Operation.CREATE.equals(change.getOperation())) {
                    filePartKeyPrefix = "educationFiles-";
                    entityIdForFile = change.getTempId();
                } else { // UPDATE or DELETE
                    filePartKeyPrefix = "educationFiles-";
                    entityIdForFile = change.getId();
                }
                
                MultipartFile file = filesMap.get(filePartKeyPrefix + entityIdForFile);

                switch (change.getOperation()) {
                case CREATE:
                    Education newEducation = new Education();
                    mapEducationDtoToEntity(change.getData(), newEducation); // Map textual data

                    // CRITICAL FIX: Set the denormalized username field
                    newEducation.setUsername(userToUpdate.getUsername()); // <-- ADD THIS LINE

                    userToUpdate.addEducation(newEducation); // Assumes User entity handles adding to its list

                    if (file != null && !file.isEmpty()) {
                        S3UploadResult uploadResult = s3Service.uploadFileAndGetDetails(file, s3DirectoryBase + "new/");
                        newEducation.setCertS3Key(uploadResult.getStorageKey());
                        newEducation.setCertOriginalFilename(uploadResult.getOriginalFilename());
                    }
                    break;
               case UPDATE:
                    Long eduIdUpdate = Long.parseLong(change.getId());
                    Education eduToUpdate = userToUpdate.getEducations().stream()
                            .filter(e -> e.getId().equals(eduIdUpdate)).findFirst()
                            .orElseThrow(() -> new RuntimeException("Education not found for update: " + eduIdUpdate));

                    mapEducationDtoToEntity(change.getData(), eduToUpdate);

                    // CRITICAL FIX: Update the denormalized username field on update too
                    eduToUpdate.setUsername(userToUpdate.getUsername()); // <-- ADD THIS LINE

                    if (file != null && !file.isEmpty()) {
                        if (eduToUpdate.getCertS3Key() != null) {
                            s3Service.deleteFile(eduToUpdate.getCertS3Key());
                        }
                        S3UploadResult uploadResult = s3Service.uploadFileAndGetDetails(file, s3DirectoryBase + eduIdUpdate + "/");
                        eduToUpdate.setCertS3Key(uploadResult.getStorageKey());
                        eduToUpdate.setCertOriginalFilename(uploadResult.getOriginalFilename());
                    }
                    break;

                    case DELETE:
                        Long eduIdDelete = Long.parseLong(change.getId());
                        Education eduToDelete = userToUpdate.getEducations().stream()
                            .filter(e -> e.getId().equals(eduIdDelete)).findFirst()
                            .orElse(null); // Be careful with modifying list while iterating if not using iterator.remove()

                        if (eduToDelete != null) {
                            if (eduToDelete.getCertS3Key() != null) {
                                s3Service.deleteFile(eduToDelete.getCertS3Key());
                            }
                            userToUpdate.removeEducation(eduToDelete); // Assumes User entity handles removal
                            // educationRepository.delete(eduToDelete); // If not managed by cascade
                        }
                        break;
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to process education file: " + e.getMessage(), e);
            }
        }
        
        User savedUser = userRepository.save(userToUpdate); // Save user to persist changes in collections
        return new UserDTO(savedUser, s3Service); // UserDTO constructor must populate EducationDTOs with S3 URLs
    }

    private void mapEducationDtoToEntity(EducationDTO data, Education education) {
        education.setLevel(data.getLevel());
        education.setDegree(data.getDegree());
        education.setFieldOfStudy(data.getFieldOfStudy());
        education.setInstitute(data.getInstitute());
        if (data.getYear() != null && !data.getYear().trim().isEmpty()) {
             try {
                education.setYear(Integer.parseInt(data.getYear().trim()));
             } catch (NumberFormatException e) {
                education.setYear(null); // Or handle error
             }
        } else {
            education.setYear(null);
        }
        education.setScore(data.getScore());
        // Do NOT map certS3Key or certOriginalFilename here, as they are handled by file upload logic
    }


    @Override
    @Transactional
    public UserDTO updateProfessionalExperience(Long userId, List<ProfessionalExperienceChangeDTO> experienceChanges, Map<String, MultipartFile> filesMap) {
        User userToUpdate = userRepository.findByUsernameWithAllDetails(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        String s3DirectoryBase = "experience-letters/" + userToUpdate.getId() + "/";

        for (ProfessionalExperienceChangeDTO change : experienceChanges) {
             try {
                String filePartKeyPrefix;
                String entityIdForFile;

                if (ProfessionalExperienceChangeDTO.Operation.CREATE.equals(change.getOperation())) {
                    filePartKeyPrefix = "experienceLetterFiles-";
                    entityIdForFile = change.getTempId();
                } else { // UPDATE or DELETE
                    filePartKeyPrefix = "experienceLetterFiles-";
                    entityIdForFile = change.getId();
                }
                
                MultipartFile file = filesMap.get(filePartKeyPrefix + entityIdForFile);

                switch (change.getOperation()) {
                case CREATE:
                    ProfessionalExperience newExp = new ProfessionalExperience();
                    mapProfessionalExperienceDtoToEntity(change.getData(), newExp);
                    newExp.setUser(userToUpdate); // Link to user

                    // CRITICAL FIX: Set the denormalized username field for professional experience
                    newExp.setUsername(userToUpdate.getUsername()); // <-- ADD THIS LINE

                    if (file != null && !file.isEmpty()) {
                        S3UploadResult uploadResult = s3Service.uploadFileAndGetDetails(file, s3DirectoryBase + "new/");
                        newExp.setExperienceLetterS3Key(uploadResult.getStorageKey());
                        newExp.setExperienceLetterOriginalFilename(uploadResult.getOriginalFilename());
                    }
                    userToUpdate.addProfessionalExperience(newExp);
                    break;

                 case UPDATE:
                    Long expIdUpdate = Long.parseLong(change.getId());
                    ProfessionalExperience expToUpdate = userToUpdate.getProfessionalExperiences().stream()
                            .filter(e -> e.getId().equals(expIdUpdate)).findFirst()
                            .orElseThrow(() -> new RuntimeException("Professional Experience not found for update: " + expIdUpdate));

                    mapProfessionalExperienceDtoToEntity(change.getData(), expToUpdate);

                    // CRITICAL FIX: Update the denormalized username field on update too
                    expToUpdate.setUsername(userToUpdate.getUsername()); // <-- ADD THIS LINE

                    if (file != null && !file.isEmpty()) {
                        if (expToUpdate.getExperienceLetterS3Key() != null) {
                            s3Service.deleteFile(expToUpdate.getExperienceLetterS3Key());
                        }
                        S3UploadResult uploadResult = s3Service.uploadFileAndGetDetails(file, s3DirectoryBase + expIdUpdate + "/");
                        expToUpdate.setExperienceLetterS3Key(uploadResult.getStorageKey());
                        expToUpdate.setExperienceLetterOriginalFilename(uploadResult.getOriginalFilename());
                    }
                    break;

                    case DELETE:
                        Long expIdDelete = Long.parseLong(change.getId());
                         ProfessionalExperience expToDelete = userToUpdate.getProfessionalExperiences().stream()
                            .filter(e -> e.getId().equals(expIdDelete)).findFirst()
                            .orElse(null);

                        if (expToDelete != null) {
                             if (expToDelete.getExperienceLetterS3Key() != null) {
                                s3Service.deleteFile(expToDelete.getExperienceLetterS3Key());
                            }
                            userToUpdate.removeProfessionalExperience(expToDelete);
                            // professionalExperienceRepository.delete(expToDelete); // If not managed by cascade
                        }
                        break;
                }
             } catch (IOException e) {
                throw new RuntimeException("Failed to process professional experience file: " + e.getMessage(), e);
            }
        }
        
        User savedUser = userRepository.save(userToUpdate);
        return new UserDTO(savedUser, s3Service); // UserDTO constructor must populate ProfExpDTOs with S3 URLs
    }

    private void mapProfessionalExperienceDtoToEntity(ProfessionalExperienceDTO data, ProfessionalExperience experience) {
        experience.setJobTitle(data.getJobTitle());
        experience.setCompanyName(data.getCompanyName());
        if (data.getStartDate() != null && !data.getStartDate().trim().isEmpty()) {
            experience.setStartDate(LocalDate.parse(data.getStartDate().trim()));
        } else {
            experience.setStartDate(null);
        }
        if (data.getEndDate() != null && !data.getEndDate().trim().isEmpty()) {
            experience.setEndDate(LocalDate.parse(data.getEndDate().trim()));
        } else {
            experience.setEndDate(null);
        }
        experience.setDescription(data.getDescription());
        // Do NOT map S3Key or OriginalFilename here
    }

    @Override
    public Optional<User> findByUsernameWithEducations(String username) {
        return userRepository.findByUsernameWithEducations(username); // Ensure this JPQL/Criteria fetches educations
    }

    @Override
    public Optional<User> findByUsernameWithEducationsAndTheirUsers(String username) {
        // This method name is a bit confusing. "TheirUsers" suggests users of educations?
        // Usually, educations belong to one user.
        // Assuming it's meant to be similar to findByUsernameWithAllDetails or specific eager fetching.
        return userRepository.findByUsernameWithEducations(username); // Adjust if specific join is needed
    }

    @Override
    @Transactional(readOnly = true) // Crucial for eager fetching
    public Optional<User> findByUsernameWithAllDetails(String username) {
        // This method should call the UserRepository method that fetches by username with all details.
        return userRepository.findByUsernameWithAllDetails(username); // Ensure this method exists in UserRepository.
    }
    
    @Override
    public Optional<User> findByUsernameWithEducationsAndExperiences(String username) {
        return userRepository.findByUsernameWithEducationsAndExperiences(username); // Ensure repository method exists and fetches eagerly
    }
    @Override
    @Transactional(readOnly = true) // Apply transactional as it performs a fetch from DB
    public Optional<User> findByUsernameWithAllDetails(Long id) {
        // This calls the UserRepository method that fetches by ID with all details.
        return userRepository.findByUsernameWithAllDetails(id);
    }

    @Override
    public User getUserByUsername(String username) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserByUsername'");
    }




  


}