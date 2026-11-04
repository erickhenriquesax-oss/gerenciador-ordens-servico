package com.os.gerenciador.dto.request;

import com.os.gerenciador.Enums.StatusOS;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record OrdemServicoRequestDto(
        @NotNull @NotEmpty
        String descricao,

        @NotNull Long usuarioSolicitante) {
}
