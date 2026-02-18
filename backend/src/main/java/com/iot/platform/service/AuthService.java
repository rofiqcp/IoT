package com.iot.platform.service;

import com.iot.platform.model.User;
import com.iot.platform.model.UserByUsername;
import com.iot.platform.repository.UserByUsernameRepository;
import com.iot.platform.repository.UserRepository;
import com.iot.platform.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserByUsernameRepository userByUsernameRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       UserByUsernameRepository userByUsernameRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.userByUsernameRepository = userByUsernameRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public Map<String, String> login(String username, String password) {
        Optional<UserByUsername> opt = userByUsernameRepository.findById(username);
        if (opt.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }
        UserByUsername u = opt.get();
        if (!passwordEncoder.matches(password, u.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        String accessToken = tokenProvider.generateAccessToken(
                u.getUserId(), u.getTenantId(), u.getUsername(), u.getRole());
        String refreshToken = tokenProvider.generateRefreshToken(u.getUserId(), u.getTenantId());
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        tokens.put("role", u.getRole());
        tokens.put("tenantId", u.getTenantId().toString());
        return tokens;
    }

    public Map<String, String> refresh(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        UUID userId = tokenProvider.getUserId(refreshToken);
        UUID tenantId = tokenProvider.getTenantId(refreshToken);
        User user = userRepository.findByTenantIdAndUserId(tenantId, userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        String newAccessToken = tokenProvider.generateAccessToken(
                user.getUserId(), user.getTenantId(), user.getUsername(), user.getRole());
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getUserId(), user.getTenantId());
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("refreshToken", newRefreshToken);
        return tokens;
    }

    public User register(UUID tenantId, String username, String email, String password, String role) {
        UUID userId = UUID.randomUUID();
        String hash = passwordEncoder.encode(password);
        User user = new User(tenantId, userId, username, email, hash, role);
        userRepository.save(user);
        UserByUsername ubu = new UserByUsername(username, userId, tenantId, hash, role);
        userByUsernameRepository.save(ubu);
        return user;
    }
}
