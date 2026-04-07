package com.loglens.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantCreateRequest {
    @NotBlank
    private String name;
}
