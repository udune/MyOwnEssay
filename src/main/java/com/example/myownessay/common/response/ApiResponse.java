package com.example.myownessay.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String timestamp;
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                data,
                Instant.now().toString()
        );
    }
    
    public static <T> ApiResponse<T> error(T error) {
        return new ApiResponse<>(
                false,
                error,
                Instant.now().toString()
        );
    }
}