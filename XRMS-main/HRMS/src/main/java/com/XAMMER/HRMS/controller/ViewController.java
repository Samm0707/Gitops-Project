package com.XAMMER.HRMS.controller;

import com.XAMMER.HRMS.model.User;
import com.XAMMER.HRMS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller; // IMPORTANT: Use @Controller here
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller // This annotation indicates that this controller returns view names
public class ViewController {

    @Autowired
    private UserService userService; // Inject UserService to get user details for the view

    /**
     * Handles the request for the attendance analytics view page.
     * It fetches the authenticated user's details and adds them to the model,
     * then returns the name of the HTML template file.
     *
     * @param model The Spring Model to add attributes for the view.
     * @return The name of the view template (e.g., "attendance_view.html").
     */
    @GetMapping("/attendance/view")
    public String showAttendanceAnalyticsPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if user is authenticated
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            // Redirect to login page if not authenticated
            return "redirect:/login"; // Assuming you have a /login endpoint
        }

        String username = authentication.getName();
        Optional<User> userOptional = userService.findByUsername(username);

        userOptional.ifPresent(user -> {
            model.addAttribute("employee", user);
            // You can add more user-specific data to the model if needed for the view
            // e.g., model.addAttribute("employeeName", user.getFirstName() + " " + user.getLastName());
        });

        // This will resolve to src/main/resources/templates/attendance_view.html
        return "attendance_view";
    }
}