package com.codementor.controller;

import com.codementor.dto.request.UpdateProfileRequest;
import com.codementor.dto.response.ApiResponse;
import com.codementor.dto.response.DashboardResponse;
import com.codementor.entity.User;
import com.codementor.exception.ResourceNotFoundException;
import com.codementor.repository.UserRepository;
import com.codementor.security.JwtUtil;
import com.codementor.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and dashboard endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("fullName", user.getFullName());
        profile.put("role", user.getRole().name());
        profile.put("memberSince", user.getCreatedAt());

        return ResponseEntity.ok(
                ApiResponse.success("Profile fetched successfully", profile)
        );
    }

    @PutMapping("/profile")
    @Operation(summary = "Update profile",
            description = "Update full name only")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            HttpServletRequest httpRequest) {

        Long userId = extractUserId(httpRequest);
        userService.updateProfile(userId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully")
        );
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard data",
            description = "Returns welcome message, stats and recent submissions")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            HttpServletRequest request) {

        Long userId = extractUserId(request);
        DashboardResponse dashboard = userService.getDashboard(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Dashboard fetched successfully", dashboard)
        );
    }

    private Long extractUserId(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return jwtUtil.extractUserId(bearerToken.substring(7));
        }
        throw new IllegalStateException("No JWT token found");
    }
}