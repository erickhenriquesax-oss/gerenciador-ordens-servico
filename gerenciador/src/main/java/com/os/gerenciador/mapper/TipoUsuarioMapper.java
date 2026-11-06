package com.os.gerenciador.mapper;

import com.os.gerenciador.dto.request.TipoUsuarioRequestDto;
import com.os.gerenciador.dto.response.TipoUsuarioResponseDto;
import com.os.gerenciador.model.TipoUsuarioModel;

public final class TipoUsuarioMapper {
    private TipoUsuarioMapper() {}

    public static TipoUsuarioModel toModel(TipoUsuarioRequestDto request) {
        return TipoUsuarioModel.builder().descricao(request.descricao()).build();
    }

    public static TipoUsuarioResponseDto toResponse(TipoUsuarioModel model) {
        return new TipoUsuarioResponseDto(model.getId(), model.getDescricao());
    }
}
