package com.XAMMER.HRMS.controller;

import com.XAMMER.HRMS.model.User;
import com.XAMMER.HRMS.service.UserService;
import com.XAMMER.HRMS.service.S3Service; // <-- Make sure this is imported
import com.XAMMER.HRMS.dto.UserDTO;
import com.XAMMER.HRMS.model.Roles; // Keep if still relevant for setting roles
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate; // Keep if used elsewhere, otherwise can be removed
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function; // Ensure this import is present for stream map
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/employees")
public class AdminEmployeeController {

    private final UserService userService;
    private final S3Service s3Service; // S3Service is correctly injected here

    @Autowired
    public AdminEmployeeController(UserService userService, S3Service s3Service) {
        this.userService = userService;
        this.s3Service = s3Service;
    }

    @GetMapping
    public String showEmployeeManagementPage(Model model) {
        // This method calls userService.findAllUsersDTO() which is already configured
        // to return List<UserDTO> by calling new UserDTO(user, s3Service) internally.
        model.addAttribute("employees", userService.findAllUsersDTO());
        return "employee-admin"; // Assuming your admin page is named 'employees.html'
    }

    // Get all employees (users with employee roles) - Returns List<UserDTO>
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<UserDTO>> getAllEmployeesApi() {
        // userService.findAllUsersDTO() already returns List<UserDTO>, so this is fine.
        List<UserDTO> employees = userService.findAllUsersDTO();
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }

    // Add a new employee (which means creating a new User)
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<UserDTO> addEmployeeApi(@RequestBody User user) {
        try {
            User newEmployee = userService.addUser(user);
            // FIX: Pass s3Service to the UserDTO constructor
            return new ResponseEntity<>(new UserDTO(newEmployee, s3Service), HttpStatus.CREATED);
        } catch (Exception e) {
            // Log the error
            e.printStackTrace();
            // Return a null body with an error status if the method signature expects UserDTO
            // If the client explicitly needs an error message, consider a generic ErrorDTO.
            // For compilation, returning null is acceptable if the client handles it.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Get a single employee by ID - Returns UserDTO
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<UserDTO> getEmployeeApi(@PathVariable Long id) {
        // This method relies on userService.findByUsernameWithAllDetails(Long id)
        // being defined in UserService and UserRepository and eagerly fetching all collections.
        Optional<User> userEntityOptional = userService.findByUsernameWithAllDetails(id);

        if (userEntityOptional.isPresent()) {
            User userEntity = userEntityOptional.get();
            // FIX: Pass s3Service to the UserDTO constructor
            return new ResponseEntity<>(new UserDTO(userEntity, s3Service), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Handles not found, no UserDTO needed
        }
    }

    // Search and filter employees - Returns List<UserDTO>
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<UserDTO>> searchAndFilterEmployeesApi(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String department) {
        // userService.searchAndFilterUsers returns List<User>. Map them to UserDTO.
        // CRITICAL: Ensure searchAndFilterUsers fetches all collections eagerly if UserDTO needs them.
        List<User> employees = userService.searchAndFilterUsers(query, department);
        List<UserDTO> employeeDTOs = employees.stream()
                .map(user -> new UserDTO(user, s3Service)) // FIX: Pass s3Service
                .collect(Collectors.toList());
        return new ResponseEntity<>(employeeDTOs, HttpStatus.OK);
    }

    // Update an existing employee
    // Client sends a User entity, but the response should be a DTO
    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<UserDTO> updateEmployeeApi(@PathVariable Long id, @RequestBody User user) {
        try {
            User updatedEmployee = userService.updateUser(id, user); // updateUser likely returns a User entity
            // Convert the returned User entity to a UserDTO before sending the response
            return new ResponseEntity<>(new UserDTO(updatedEmployee), HttpStatus.OK);
        } catch (RuntimeException e) {
            // Log the exception
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Delete an employee - Returns Void
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteEmployeeApi(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
        } catch (EmptyResultDataAccessException ex) {
            System.err.println("Attempted to delete non-existent employee with ID: " + id);
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
        } catch (Exception e) {
            System.err.println("Error deleting employee with ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
        }
    }

    @GetMapping("/api/suggestions")
    @ResponseBody
    public ResponseEntity<List<String>> getUserSuggestions(@RequestParam("query") String query) {
        List<String> usernames = userService.findUsernamesByQuery(query);
        if (usernames.isEmpty()) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        }
        return new ResponseEntity<>(usernames, HttpStatus.OK);
    }
}