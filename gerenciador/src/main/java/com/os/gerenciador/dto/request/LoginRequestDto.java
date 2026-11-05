package com.os.gerenciador.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank(message = "Email é obrigatório preencher")
        String email,

        @NotBlank(message = "Senha é obrigatório preencher")
        String senha
) {
}
