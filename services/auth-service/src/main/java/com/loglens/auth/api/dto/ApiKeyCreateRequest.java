package com.loglens.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApiKeyCreateRequest {
    @NotBlank
    private String name;
    private String expiresAt; // ISO-8601, optional
}
