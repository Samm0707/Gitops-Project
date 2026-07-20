// // src/main/java/com/XAMMER/HRMS/controller/UserController.java

// package com.XAMMER.HRMS.controller;

// import com.XAMMER.HRMS.model.User;
// import com.XAMMER.HRMS.service.UserService; // Your UserService interface
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.security.access.prepost.PreAuthorize; // For security

// import java.util.List;
// import java.util.Optional;

// @RestController
// @RequestMapping("/api/users") // Base path for your user API
// public class UserController {

//     private final UserService userService; // Autowire your interface

//     @Autowired
//     public UserController(UserService userService) {
//         this.userService = userService;
//     }

//     // Endpoint for fetching all users (corresponds to frontend's fetchEmployees)
//     @GetMapping("/all") // Changed from just "/" to "/all" to avoid conflict if you add other GETs
//     @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'EMPLOYEE')")
//     public ResponseEntity<List<User>> getAllUsers() {
//         // Your current UserServiceImpl.findAll() returns List<User>
//         List<User> users = userService.findAll();
//         // Be cautious about returning raw User entities as they might contain sensitive data like password.
//         // Ideally, you'd convert them to a DTO here.
//         return ResponseEntity.ok(users);
//     }

//     // Endpoint for saving/updating a user (corresponds to frontend's addEmployeeForm submit)
//     // Your UserServiceImpl.save(User user) handles both new and update based on user.id
//     @PostMapping("/save") // Using POST for both create and update with the same endpoint
//     @PreAuthorize("hasAuthority('ADMIN')") // Only ADMIN can save/update users
//     public ResponseEntity<User> saveUser(@RequestBody User user) {
//         // The User object from frontend will have 'id' (Long) if it's an update, null if new
//         userService.save(user); // Calls your existing save method
//         return ResponseEntity.status(HttpStatus.CREATED).body(user); // Returns the saved user (now with DB ID if new)
//     }

//     // Endpoint for deleting a user by their internal Long ID
//     @DeleteMapping("/{id}") // Matches the frontend's deleteUser, targeting the internalId
//     @PreAuthorize("hasAuthority('ADMIN')") // Only ADMIN can delete
//     public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
//         Optional<User> user = userService.findById(id); // Check if user exists
//         if (user.isPresent()) {
//             userService.delete(id); // Calls your existing delete method
//             return ResponseEntity.noContent().build(); // 204 No Content
//         }
//         return ResponseEntity.notFound().build(); // 404 Not Found
//     }

//     // You might also want an endpoint for getting a single user by internal ID, though frontend doesn't directly call it for details
//     @GetMapping("/{id}")
//     @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'EMPLOYEE') and #id == authentication.principal.id or hasAuthority('ADMIN') or hasAuthority('MANAGER')")
//     public ResponseEntity<User> getUserById(@PathVariable Long id) {
//         return userService.findById(id)
//                 .map(ResponseEntity::ok)
//                 .orElse(ResponseEntity.notFound().build());
//     }

//     // You might want an endpoint to expose searchUsernames
//     @GetMapping("/search-usernames")
//     @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'EMPLOYEE')")
//     public ResponseEntity<List<String>> searchUsernames(@RequestParam String query) {
//         return ResponseEntity.ok(userService.searchUsernames(query));
//     }
// }