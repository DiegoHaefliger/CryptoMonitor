package com.haefliger.cryptomonitor.exception.dto;

import java.time.ZonedDateTime;
import java.util.List;

public record ApiErrorResponse(
        ZonedDateTime timestamp,
        Integer status,
        String error,
        List<ApiErrorDetailResponse> errors,
        String path) {}
