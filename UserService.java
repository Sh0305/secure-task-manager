package com.securetask.taskmanager.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.securetask.taskmanager.dto.AuthResponse;
import com.securetask.taskmanager.dto.LoginRequest;
import com.securetask.taskmanager.dto.RegisterRequest;
import com.securetask.taskmanager.exception.DuplicateResourceException;
import com.securetask.taskmanager.exception.ResourceNotFoundException;
import com.securetask.taskmanager.model.RefreshToken;
import com.securetask.taskmanager.model.User;
import com.securetask.taskmanager.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;

    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .build();

        userRepository.save(user);
        return "User registered successfully";
    }

    public AuthResponse login(LoginRequest request) {
    authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    request.getEmail(), request.getPassword()));

    UserDetails userDetails = userDetailsService
            .loadUserByUsername(request.getEmail());

    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    String accessToken = jwtService.generateToken(userDetails);

    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .tokenType("Bearer")
                .build();
        return response;
        
}

    public String logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));
        refreshTokenService.deleteUserTokens(user);
        return "Logged out successfully";
    }
    
}
