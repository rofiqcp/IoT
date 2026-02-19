package com.iot.platform.controller;

import com.iot.platform.model.User;
import com.iot.platform.repository.UserRepository;
import com.iot.platform.security.TenantContext;
import com.iot.platform.service.AuthService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final AuthService authService;

    public UserController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN','TENANT_ADMIN')")
    public List<User> listUsers(Authentication auth) {
        TenantContext ctx = (TenantContext) auth.getDetails();
        return userRepository.findByTenantId(ctx.getTenantId());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN','TENANT_ADMIN')")
    public Map<String, String> createUser(@RequestBody Map<String, String> body,
                                           Authentication auth) {
        TenantContext ctx = (TenantContext) auth.getDetails();
        User user = authService.register(
                ctx.getTenantId(),
                body.get("username"),
                body.get("email"),
                body.get("password"),
                body.getOrDefault("role", "VIEWER")
        );
        return Map.of("userId", user.getUserId().toString(), "username", user.getUsername());
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','TENANT_ADMIN')")
    public void deleteUser(@PathVariable UUID userId, Authentication auth) {
        TenantContext ctx = (TenantContext) auth.getDetails();
        User user = userRepository.findByTenantIdAndUserId(ctx.getTenantId(), userId);
        if (user != null) {
            userRepository.delete(user);
        }
    }
}
