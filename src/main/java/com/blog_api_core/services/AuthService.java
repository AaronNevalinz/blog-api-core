package com.blog_api_core.services;

import com.blog_api_core.exceptions.NotFoundException;
import com.blog_api_core.models.Role;
import com.blog_api_core.models.User;
import com.blog_api_core.repository.RoleRepository;
import com.blog_api_core.repository.UserRepository;
import com.blog_api_core.utils.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;

    public AuthService(PasswordEncoder passwordEncoder, UserRepository userRepository, JwtUtils jwtUtils, RoleRepository roleRepository, AuthenticationManager authenticationManager) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
    }

    public User register(User user) {
        if(userRepository.existsByUsername(user.getUsername())) {
            throw new NotFoundException("Username is already in use");
        }

        Set<Role> roles = new HashSet<>();
        Role userRole;
        if(userRepository.count() == 0) {
            userRole = roleRepository.findByName("ROLE_ADMIN");
            roles.add(userRole);
        }else{
            userRole = roleRepository.findByName("ROLE_USER");
            roles.add(userRole);
        }
        roles.add(userRole);
        user.setRoles(roles);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public String login(User user) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtUtils.generateTokenFromUser(user.getUsername());
    }
}
