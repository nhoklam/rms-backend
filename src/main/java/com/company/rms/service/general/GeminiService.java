package com.company.rms.service.general;

import com.company.rms.entity.allocation.ViewResourceAvailability;
import com.company.rms.repository.allocation.ViewResourceAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final ViewResourceAvailabilityRepository resourceRepo;
    private final RestTemplate restTemplate = new RestTemplate();

    // [SỬA LẠI THÀNH MÔ HÌNH FLASH - ỔN ĐỊNH NHẤT]
    // Model Flash bản 001 ổn định
    // [SỬA LẠI THÀNH MÔ HÌNH GEMINI 2.5 FLASH - CÓ TRONG DANH SÁCH CỦA BẠN]
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";
    public String processChat(String userMessage) {
        StringBuilder contextData = new StringBuilder();

        // --- 1. LOGIC RAG: Lấy dữ liệu hệ thống để "mớm" cho AI ---
        // Nếu câu hỏi liên quan đến tìm người, rảnh, nhân viên...
        if (userMessage.toLowerCase().contains("tìm") || 
            userMessage.toLowerCase().contains("rảnh") || 
            userMessage.toLowerCase().contains("trống") ||
            userMessage.toLowerCase().contains("nhân viên") ||
            userMessage.toLowerCase().contains("dev")) {
            
            // Lấy danh sách nhân viên đang rảnh (capacity >= 1%)
            List<ViewResourceAvailability> resources = resourceRepo.searchAvailableResources(null, null, BigDecimal.ONE);
            
            contextData.append("Dữ liệu hệ thống về nhân sự đang rảnh:\n");
            int count = 0;
            for (ViewResourceAvailability r : resources) {
                if (count++ > 15) break; // Chỉ lấy 15 người đầu để tránh quá tải
                contextData.append(String.format("- %s (Mã: %s): %s, Level: %s, Skill: [%s], Rảnh: %s%%\n", 
                    r.getFullName(), r.getEmployeeCode(), r.getJobTitle(), r.getLevelName(), r.getSkillsList(), r.getAvailableCapacity()));
            }
        }

        // --- 2. Tạo Prompt (Câu lệnh nhắc) gửi AI ---
        String prompt = "Bạn là trợ lý ảo của hệ thống quản lý nguồn lực (RMS).\n" +
                        "Dữ liệu thực tế từ database:\n" + 
                        contextData.toString() + 
                        "\n\nCâu hỏi: \"" + userMessage + "\"" +
                        "\n\nYêu cầu: Trả lời ngắn gọn bằng tiếng Việt. Nếu có danh sách, hãy format đẹp.";

        // --- 3. Gửi Request sang Google ---
        // Cấu trúc JSON thủ công để không cần thêm thư viện mới
        String requestBody = "{ \"contents\": [{ \"parts\": [{ \"text\": \"" + escapeJson(prompt) + "\" }] }] }";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_URL + apiKey, entity, String.class);
            return extractTextFromGeminiResponse(response.getBody()); 
        } catch (Exception e) {
            e.printStackTrace();
            return "Xin lỗi, tôi đang gặp sự cố kết nối với AI (" + e.getMessage() + ")";
        }
    }

    // Hàm xử lý ký tự đặc biệt trong JSON
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    // Hàm lấy text từ JSON phản hồi của Google
    private String extractTextFromGeminiResponse(String jsonResponse) {
        try {
            // Google trả về dạng: ... "text": "Nội dung trả lời" ...
            String marker = "\"text\": \"";
            int startIndex = jsonResponse.indexOf(marker);
            if (startIndex != -1) {
                startIndex += marker.length();
                // Tìm dấu ngoặc kép đóng, lưu ý bỏ qua các dấu \" (escaped quote)
                int endIndex = startIndex;
                boolean isEscaped = false;
                while (endIndex < jsonResponse.length()) {
                    char c = jsonResponse.charAt(endIndex);
                    if (c == '\\') {
                        isEscaped = !isEscaped;
                    } else if (c == '"' && !isEscaped) {
                        break; // Tìm thấy dấu đóng
                    } else {
                        isEscaped = false;
                    }
                    endIndex++;
                }
                
                String text = jsonResponse.substring(startIndex, endIndex);
                // Unescape ký tự xuống dòng và ngoặc kép để hiển thị đẹp
                return text.replace("\\n", "\n").replace("\\\"", "\"");
            }
        } catch (Exception e) {
            return "Lỗi đọc dữ liệu từ AI.";
        }
        return "Không có câu trả lời."; 
    }
}