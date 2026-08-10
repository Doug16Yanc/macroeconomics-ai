package com.example.macroeconomics_ai.infrastructure.web.dto;

import java.time.LocalDateTime;

public record JobStatusResponse(
        Long jobExecutionId,
        String status,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
