package com.haefliger.cryptomonitor.exception.dto;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
public class ApiErrorResponse {
    private ZonedDateTime timestamp;
    private Integer status;
    private String error;
    private List<ApiErrorDetailResponse> errors;
    private String path;
}
