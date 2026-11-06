package com.os.gerenciador.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
        @NotBlank(message = "Email é obrigatório preencher")
        @Size(max = 100)String email,

        @NotBlank(message = "Senha é obrigatório preencher")
        String senha
) {
}
