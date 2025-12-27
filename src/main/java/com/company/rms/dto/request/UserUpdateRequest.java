package com.company.rms.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Set;

@Data
public class UserUpdateRequest {
    // Không cần validation username vì thường không cho sửa username
    private String username; 
    
    // KHÔNG CÓ @NotBlank hay @Size ở đây để cho phép null
    private String password; 

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private Set<String> roles;
}