package com.haefliger.cryptomonitor.exception.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiErrorDetailResponse {
    private String defaultMessage;
}
