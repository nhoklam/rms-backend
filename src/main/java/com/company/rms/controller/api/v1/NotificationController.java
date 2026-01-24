package com.company.rms.controller.api.v1;

import com.company.rms.dto.response.ApiResponse;
import com.company.rms.entity.general.Notification;
import com.company.rms.service.general.NotificationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostConstruct
    public void init() {
        // Dòng này sẽ hiện trong log nếu Controller chạy đúng
        log.error("===============================================================");
        log.error(">>> NOTIFICATION CONTROLLER DA DUOC KHOI TAO THANH CONG <<<");
        log.error("===============================================================");
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Notification>>> getMyNotifications() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getMyNotifications()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}