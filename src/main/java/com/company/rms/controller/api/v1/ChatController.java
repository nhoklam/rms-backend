package com.company.rms.controller.api.v1;

import com.company.rms.dto.response.ApiResponse;
import com.company.rms.service.general.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final GeminiService geminiService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> chat(@RequestBody Map<String, String> payload) {
        // Lấy tin nhắn từ JSON { "message": "..." }
        String message = payload.get("message");
        
        // Gọi Service xử lý
        String response = geminiService.processChat(message);
        
        // Trả về kết quả
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}