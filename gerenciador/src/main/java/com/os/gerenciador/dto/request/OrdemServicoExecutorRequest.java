package com.os.gerenciador.dto.request;

import jakarta.validation.constraints.NotNull;

public record OrdemServicoExecutorRequest(
        @NotNull Long idExecutor) {
}
