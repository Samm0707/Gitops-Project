package com.XAMMER.HRMS.service;

import com.XAMMER.HRMS.dto.EducationChangeDTO;
import com.XAMMER.HRMS.dto.EducationDTO;
import com.XAMMER.HRMS.dto.FamilyDetailsDTO;
import com.XAMMER.HRMS.dto.FinancialDetailsDTO;
import com.XAMMER.HRMS.dto.PersonalDetailsUpdateDTO;
import com.XAMMER.HRMS.dto.UserDTO;
import com.XAMMER.HRMS.dto.ProfessionalExperienceChangeDTO; // Ensure this is imported
import com.XAMMER.HRMS.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {

    List<User> findAll();
    void save(User user);
    List<String> getAllUsernames();

    List<User> getUpcomingBirthdays();
    User getUserByUsername(String username);
    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);
    List<User> getAllUsers();
    void saveUser(User user);
    void delete(Long id);
    List<String> searchUsernames(String query);
    List<String> findUsernamesContaining(String query);
    Optional<User> getUserById(Long id);
    List<User> findAllEmployees();
    User addUser(User user);
    List<User> searchAndFilterUsers(String query, String department);
    User updateUser(Long id, User user);

    boolean isUserOwnerOfProfile(String currentUsername, Long id);

    void deleteUser(Long id);
    List<UserDTO> findAllUsersDTO();
    List<String> findUsernamesByQuery(String query);

    UserDTO updatePersonalDetails(Long id, PersonalDetailsUpdateDTO dto);
    UserDTO updateFamilyDetails(Long id, FamilyDetailsDTO dto);
    UserDTO updateFinancialDetails(Long id, FinancialDetailsDTO dto, MultipartFile aadhaarDoc, MultipartFile panDoc);
    UserDTO updateEducationDetails(Long id, List<EducationChangeDTO> changes, Map<String, MultipartFile> files);
    UserDTO updateProfessionalExperience(Long id, List<ProfessionalExperienceChangeDTO> changes, Map<String, MultipartFile> files);

    Optional<User> findByUsernameWithEducations(String username);
    Optional<User> findByUsernameWithEducationsAndTheirUsers(String username);
    Optional<User> findByUsernameWithEducationsAndExperiences(String username);

    // These two are the ONLY overloads of findByUsernameWithAllDetails that should be here
    Optional<User> findByUsernameWithAllDetails(String username); // <--- This is the one it complains about
    Optional<User> findByUsernameWithAllDetails(Long id);
}