package com.company.rms.dto.response;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {
    private String accessToken;
    
    @Builder.Default
    private String tokenType = "Bearer";
    
    private Long userId;
    private String username;
    private String fullName;
    private Long employeeId;
    
    // FIX: Thêm <String>
    private List<String> roles;
}