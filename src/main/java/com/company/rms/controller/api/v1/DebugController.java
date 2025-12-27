package com.company.rms.controller.api.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/debug")
public class DebugController {

    @GetMapping("/me")
    public ResponseEntity<?> whoAmI() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> info = new HashMap<>();
        info.put("username", auth.getName());
        info.put("authorities", auth.getAuthorities()); // Xem danh sách quyền
        info.put("isAuthenticated", auth.isAuthenticated());
        return ResponseEntity.ok(info);
    }
}