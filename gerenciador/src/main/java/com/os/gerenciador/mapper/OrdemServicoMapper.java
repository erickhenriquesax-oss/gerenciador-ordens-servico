package com.os.gerenciador.mapper;

import com.os.gerenciador.Enums.StatusOS;
import com.os.gerenciador.dto.request.OrdemServicoRequestDto;
import com.os.gerenciador.dto.response.OrdemServicoResponseDto;
import com.os.gerenciador.model.OrdemServicoModel;
import com.os.gerenciador.model.UsuarioModel;
import java.time.LocalDateTime;

public final class OrdemServicoMapper {
    private OrdemServicoMapper() {}

    public static OrdemServicoModel toModel(OrdemServicoRequestDto request, UsuarioModel solicitante) {
        return OrdemServicoModel.builder().descricao(request.descricao()).status(StatusOS.ABERTA)
                .usuarioSolicitante(solicitante).dataCriacao(LocalDateTime.now()).build();
    }

    public static OrdemServicoResponseDto toResponse(OrdemServicoModel model) {
        return new OrdemServicoResponseDto(model.getId(), model.getDescricao(), model.getStatus(),
                model.getUsuarioSolicitante().getId(),
                model.getUsuarioExecutor() == null ? null : model.getUsuarioExecutor().getId(),
                model.getDataCriacao(), model.getDataFinalizacao());
    }
}
