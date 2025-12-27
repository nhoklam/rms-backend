package com.company.rms.repository.iam;

import com.company.rms.entity.iam.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Spring Data JPA sẽ tự động generate query dựa trên tên hàm
    Optional<User> findByUsername(String username);
    
    // Kiểm tra tồn tại (Dùng cho validation đăng ký nếu cần sau này)
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}