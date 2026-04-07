package com.loglens.auth.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data @Builder
public class UserInfoResponse {
    private String userId;
    private String email;
    private String tenantId;
    private List<String> roles;
}
