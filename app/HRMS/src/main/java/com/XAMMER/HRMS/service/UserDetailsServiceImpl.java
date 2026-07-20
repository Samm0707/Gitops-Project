package com.XAMMER.HRMS.service;

import com.XAMMER.HRMS.model.Roles;
import com.XAMMER.HRMS.model.User;
import com.XAMMER.HRMS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                getAuthorities(user.getRoles()) // Call the correct getAuthorities method
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Roles role) {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    // private Collection<? extends GrantedAuthority> getAuthorities(String roles) {
    //     if (roles != null && !roles.isEmpty()) {
    //         return Arrays.stream(roles.split(","))
    //                 .map(role -> new SimpleGrantedAuthority(roles.trim().toUpperCase()))
    //                 .collect(Collectors.toList());
    //     }
    //     return Collections.emptyList();
    // }

}
