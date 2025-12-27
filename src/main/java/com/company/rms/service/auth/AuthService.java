package com.company.rms.service.auth;

import com.company.rms.dto.request.LoginRequest;
import com.company.rms.dto.response.JwtResponse;
import com.company.rms.entity.hr.Employee;
import com.company.rms.entity.iam.User;
import com.company.rms.repository.hr.EmployeeRepository;
import com.company.rms.repository.iam.UserRepository;
import com.company.rms.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public JwtResponse login(LoginRequest request) {
        // 1. Authenticate - Spring Security sẽ tự động kiểm tra user và pass (hash)
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 2. Lấy thông tin User sau khi đã xác thực thành công
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found")); // Không bao giờ nhảy vào đây nếu bước 1 thành công

        // 3. Cập nhật lần đăng nhập cuối
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 4. Lấy thông tin nhân viên nếu có
        Long employeeId = employeeRepository.findByUserId(user.getId())
            .map(Employee::getId)
            .orElse(null);

        // 5. Tạo Token
        String jwt = tokenProvider.generateToken(authentication, employeeId);

        List<String> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        return JwtResponse.builder()
            .accessToken(jwt)
            .userId(user.getId())
            .username(user.getUsername())
            .fullName(user.getFullName())
            .employeeId(employeeId)
            .roles(roles)
            .build();
    }
}