package com.XAMMER.HRMS.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails; // Import UserDetails
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

import com.XAMMER.HRMS.model.User; // Import your User class

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        Set<String> authorities = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        System.out.println("Authenticated user authorities: " + authorities); // Keep this for debugging

        Object principal = authentication.getPrincipal(); // Get the principal

        if (principal instanceof User) {
            User loggedInUser = (User) principal; // Use your User class
             //  You might not need to do anything here, because the user object is already in the principal
        }
        else if (principal instanceof UserDetails) {
            //This is the fallback, in case the UserDetailsService returns a UserDetails object
            UserDetails userDetails = (UserDetails) principal;
            System.out.println("Principal is instance of UserDetails");
        }
        else {
            System.out.println("principal is not instance of User or UserDetails");
        }

        if (authorities.contains("ROLE_ADMIN")) {
            response.sendRedirect("/admin/dashboard");
        } else {
            response.sendRedirect("/dashboard");
        }
    }
}

// package com.XAMMER.HRMS.config;

// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.authority.AuthorityUtils;
// import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
// import org.springframework.stereotype.Component;

// import java.io.IOException;
// import java.util.Set;

// @Component
// public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

//     @Override
//     public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
//                                         Authentication authentication) throws IOException {

//         Set<String> authorities = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

//         if (authorities.contains("ROLE_ADMIN")) {
//             response.sendRedirect("/admin/dashboard");
//         } else if (authorities.contains("ROLE_USER_MANAGER")) {
//             response.sendRedirect("/manager/dashboard"); // Redirect managers to their dashboard
//         } else if (authorities.contains("ROLE_USER")) {
//             response.sendRedirect("/dashboard");
//         } else {
//             response.sendRedirect("/default"); // Fallback redirect
//         }
//     }
// }