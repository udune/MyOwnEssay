package com.example.myownessay.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String code;
    private String message;
    private Map<String, String> details;
    
    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }
}