package com.company.rms.security;

import com.company.rms.entity.iam.User;
import com.company.rms.repository.iam.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // <--- 1. THÊM IMPORT NÀY
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j // <--- 2. THÊM ANNOTATION NÀY
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrId) throws UsernameNotFoundException {
        log.info("--- Đang thử login với input: '{}' ---", usernameOrId); // <--- LOG DEBUG

        // 1. Thử tìm bằng ID
        try {
            Long id = Long.parseLong(usernameOrId);
            User user = userRepository.findById(id).orElse(null);
            if (user != null) {
                log.info("-> Tìm thấy User theo ID: {}", user.getUsername());
                return UserPrincipal.create(user, user.getEmployee() != null ? user.getEmployee().getId() : null);
            }
        } catch (NumberFormatException e) {
            // Không phải ID, bỏ qua
        }

        // 2. Tìm bằng Username
        User 
        user = userRepository.findByUsername(usernameOrId)
                .orElseThrow(() -> {
                    log.error("-> KHÔNG tìm thấy user nào với username: '{}'", usernameOrId); // <--- LOG LỖI
                    return new UsernameNotFoundException("User not found: " + usernameOrId);
                });

        log.info("-> Tìm thấy User trong DB: '{}'", user.getUsername());
        log.info("-> Password Hash trong DB: '{}'", user.getPasswordHash());
        log.info("-> Trạng thái Active: {}", user.getIsActive());
        
        Long employeeId = (user.getEmployee() != null) ? user.getEmployee().getId() : null;
        return UserPrincipal.create(user, employeeId);
    }
}