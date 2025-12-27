package com.company.rms.service.iam;

import com.company.rms.dto.request.UserCreateRequest;
import com.company.rms.dto.response.UserResponse;
import com.company.rms.entity.iam.Role;
import com.company.rms.entity.iam.User;
import com.company.rms.exception.BusinessException;
import com.company.rms.repository.iam.RoleRepository;
import com.company.rms.repository.iam.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.company.rms.exception.ResourceNotFoundException; 
import org.springframework.util.StringUtils;
import com.company.rms.dto.request.UserUpdateRequest;
import com.company.rms.dto.request.ChangePasswordRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // Cần tạo repository này nếu chưa có (JPA basic)
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        // Map Roles
        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null) {
            request.getRoles().forEach(roleName -> {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new BusinessException("Role not found: " + roleName));
                roles.add(role);
            });
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .roles(roles)
                .build();

        return mapToResponse(userRepository.save(user));
    }
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) { // <-- Đổi thành UserUpdateRequest
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        // Logic này sẽ hoạt động tốt vì password có thể là null
        if (StringUtils.hasText(request.getPassword())) {
             // Có thể validate độ dài thủ công ở đây nếu muốn
             if (request.getPassword().length() < 6) {
                 throw new BusinessException("Password must be at least 6 characters");
             }
             user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        // Cập nhật Roles
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            request.getRoles().forEach(roleName -> {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new BusinessException("Role not found: " + roleName));
                roles.add(role);
            });
            user.setRoles(roles);
        }

        return mapToResponse(userRepository.save(user));
    }
    // Toggle Active Status
    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .isActive(user.getIsActive())
                // FIX: Check if roles is null to avoid NullPointerException
                .roles(user.getRoles() != null 
                    ? user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()) 
                    : new HashSet<>())
                .build();
    }
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 1. Kiểm tra mật khẩu cũ có đúng không
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException("Mật khẩu cũ không chính xác");
        }

        // 2. Kiểm tra mật khẩu mới không được trùng mật khẩu cũ
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException("Mật khẩu mới không được trùng với mật khẩu cũ");
        }

        // 3. Mã hóa và lưu mật khẩu mới
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}