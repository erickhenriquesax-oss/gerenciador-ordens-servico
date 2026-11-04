package com.os.gerenciador.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TipoUsuarioRequestDto(
        @NotNull @NotBlank
        String descricao
) {
}
