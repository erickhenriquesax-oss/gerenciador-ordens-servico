package com.os.gerenciador.dto.response;

import com.os.gerenciador.model.TipoUsuarioModel;
import com.os.gerenciador.model.UsuarioModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TipoUsuarioResponseDto(
        String descricao
) {
    public TipoUsuarioResponseDto (TipoUsuarioModel tipoUsuarioModel) {
        this(
                tipoUsuarioModel.getDescricao()
        );
    }
}
