package com.example.myownessay.validator;

import java.util.Map;

// 슬롯 콘텐츠의 유효성을 검사하는 인터페이스
public interface SlotContentValidator {

    void validate(Map<String, Object> content); // 슬롯 콘텐츠의 유효성을 검사하는 메서드

}
