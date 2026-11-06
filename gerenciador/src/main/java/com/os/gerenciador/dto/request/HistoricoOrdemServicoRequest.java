package com.os.gerenciador.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HistoricoOrdemServicoRequest(
        @NotNull Long idUsuario,
        @NotNull Long idOrdemServico,
        @NotNull @NotBlank @Size(max = 255) String descricao
) {
}
