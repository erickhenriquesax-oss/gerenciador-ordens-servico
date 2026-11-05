package com.os.gerenciador.dto.response;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TipoUsuarioResponseDto(
        String descricao
) implements Serializable {}
