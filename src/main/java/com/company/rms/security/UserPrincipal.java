package com.company.rms.security;

import com.company.rms.entity.iam.Role;
import com.company.rms.entity.iam.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set; // Import Set
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    
    private Long id;
    private String username;
    private String email;
    private String password;
    private Long employeeId;
    
    private Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(User user, Long employeeId) {
        /* * FIX LỖI "Type mismatch" và "Raw type":
         * 1. Lấy roles từ User (đang là Raw Set).
         * 2. Ép kiểu về Set<Role> và dùng @SuppressWarnings để bỏ qua cảnh báo.
         * 3. Stream bây giờ sẽ hiểu kiểu dữ liệu là Role, không phải Object.
         */
        @SuppressWarnings("unchecked")
        Set<Role> roles = (Set<Role>) user.getRoles();

        List<GrantedAuthority> authorities = roles.stream()
            .map(role -> new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toList());

        return new UserPrincipal(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPasswordHash(),
            employeeId,
            authorities
        );
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}