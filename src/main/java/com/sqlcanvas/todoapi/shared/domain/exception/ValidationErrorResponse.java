package com.sqlcanvas.todoapi.shared.domain.exception;

import java.time.LocalDateTime;
import java.util.Map;

// レスポンス
record ValidationErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        Map<String, String> details
) {
}