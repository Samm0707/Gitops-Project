package com.XAMMER.HRMS.repository;

import com.XAMMER.HRMS.model.Roles;
import com.XAMMER.HRMS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    List<User> findByManager(User manager);
    List<User> findByManagerId(Long managerId);
    List<User> findByRoles(Roles role);

    // For searching/filtering users
    List<User> findByUsernameContainingIgnoreCase(String usernameQuery);
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String firstName, String lastName, String username, String email);
    List<User> findByFirstNameContainingIgnoreCaseAndDepartmentContainingIgnoreCase(String firstName, String department);
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String queryPart1, String queryPart2, String queryPart3);
    List<User> findByDepartmentContainingIgnoreCase(String department);

    @Query("SELECT u.username FROM User u")
    List<String> findAllUsernames();

    @Query("SELECT u.username FROM User u WHERE LOWER(u.username) LIKE LOWER(concat('%', :query, '%'))")
    List<String> findUsernamesByUsernameContainingIgnoreCase(@Param("query") String query);

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.educations " + // Fetch educations
           "LEFT JOIN FETCH u.professionalExperiences " + // Fetch professional experiences
           "LEFT JOIN FETCH u.manager " + // Fetch manager if needed
           "WHERE u.username = :username")
    Optional<User> findByUsernameWithAllDetails(@Param("username") String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.educations WHERE u.username = :username")
    Optional<User> findByUsernameWithEducations(@Param("username") String username);

    default Optional<User> findByUsernameWithEducationsAndTheirUsers(String username) {
        return findByUsernameWithAllDetails(username);
    }
    
    default Optional<User> findByUsernameWithEducationsAndExperiences(String username) {
        return findByUsernameWithAllDetails(username);
    }

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.educations " +
           "LEFT JOIN FETCH u.professionalExperiences " +
           "LEFT JOIN FETCH u.manager " +
           "WHERE u.id = :userId")
    Optional<User> findByUsernameWithAllDetails(@Param("userId") Long userId);

    // *** CRITICAL ADDITION: Method to fetch ALL users with their lazy collections ***
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.educations " +
           "LEFT JOIN FETCH u.professionalExperiences " +
           "LEFT JOIN FETCH u.manager") // Include manager if the DTO needs it
    List<User> findAllWithEducationsAndProfessionalExperiencesAndManager(); // Renamed for clarity

    

    
}