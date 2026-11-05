package com.os.gerenciador.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrdemServicoRequestDto(
        @NotBlank @Size(max = 100) String descricao,

        @NotNull Long usuarioSolicitante) {
}
