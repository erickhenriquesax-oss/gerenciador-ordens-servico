package com.os.gerenciador.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlternaTipoUsuarioRequest(
        @NotNull @NotBlank Long id
        ) {
}
