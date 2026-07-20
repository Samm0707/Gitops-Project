package com.XAMMER.HRMS.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod; // Import HttpMethod
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Lazy
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize ->
                authorize
                    .requestMatchers("/login", "/error", "/css/**", "/js/**", "/images/**", "/sounds/**").permitAll()
                    .requestMatchers("/dashboard").hasAnyRole("USER", "USER_MANAGER")
                    .requestMatchers("/manager/dashboard").hasRole("USER_MANAGER")
                    .requestMatchers("/admin/dashboard").hasRole("ADMIN")
                    .requestMatchers("/admin/attendance/reset/**").hasRole("ADMIN")
                    .requestMatchers("/leave/cancel/**").authenticated()
                    .requestMatchers("/leave/details/**").authenticated()
                    
                    // --- MODIFICATIONS START HERE ---
                    // Allow GET requests to the profile page for authenticated users
                    .requestMatchers(HttpMethod.GET, "/employees/profile").authenticated()
                    // Allow POST requests to update profile sub-resources for authenticated users
                    .requestMatchers(HttpMethod.POST, "/employees/profile/**").authenticated() 
                    // --- MODIFICATIONS END HERE ---
                    
                    .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/perform_login")
                .successHandler(customAuthenticationSuccessHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );
            // If CSRF is causing issues with POST even with the token, you might need to
            // explicitly configure how it integrates with AJAX, or for specific paths.
            // For example, to potentially relax CSRF for these specific AJAX endpoints if needed:
            // .csrf(csrf -> csrf.ignoringRequestMatchers("/employees/profile/**"))
            // However, it's better to send the CSRF token correctly with your fetch requests.
            // The frontend code we updated should be doing this now.

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // ⚠️ IMPORTANT: Replace NoOpPasswordEncoder with BCryptPasswordEncoder in production!
        // return new BCryptPasswordEncoder();
        return NoOpPasswordEncoder.getInstance();
    }
}