package com.XAMMER.HRMS.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
public class ManagementSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain managementSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}