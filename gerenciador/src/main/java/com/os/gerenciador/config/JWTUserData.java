package com.os.gerenciador.config;

import lombok.Builder;

@Builder
public record JWTUserData(Long userId, String email, String role) {
    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(role);
    }

    public boolean isExecutor() {
        return "ROLE_EXECUTOR".equals(role);
    }
}
