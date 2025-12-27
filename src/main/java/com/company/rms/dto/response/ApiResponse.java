package com.company.rms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
// FIX: Thêm <T> vào sau tên class
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data; // T được hiểu nhờ khai báo ở trên
    private String errorCode;
    private LocalDateTime timestamp;
    
    // FIX: Thêm <T> trước kiểu trả về cho static method
    public static <T> ApiResponse<T> success(T data) {
        return success(data, null);
    }
    
    // FIX: Thêm <T> trước kiểu trả về
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    // FIX: Thêm <T> trước kiểu trả về
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .errorCode(errorCode)
            .timestamp(LocalDateTime.now())
            .build();
    }
}