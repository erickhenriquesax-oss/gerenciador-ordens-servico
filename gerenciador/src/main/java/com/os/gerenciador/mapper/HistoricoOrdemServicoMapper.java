package com.os.gerenciador.mapper;

import com.os.gerenciador.dto.request.HistoricoOrdemServicoRequest;
import com.os.gerenciador.dto.response.HistoricoOrdemServicoResponseDto;
import com.os.gerenciador.model.HistoricoOrdemServicoModel;
import com.os.gerenciador.model.OrdemServicoModel;
import com.os.gerenciador.model.UsuarioModel;
import java.time.LocalDateTime;

public final class HistoricoOrdemServicoMapper {
    private HistoricoOrdemServicoMapper() {}

    public static HistoricoOrdemServicoModel toModel(HistoricoOrdemServicoRequest request, UsuarioModel usuario, OrdemServicoModel ordem) {
        return HistoricoOrdemServicoModel.builder().dataHistorico(LocalDateTime.now())
                .usuarioHistorico(usuario).ordemServico(ordem).descricao(request.descricao()).build();
    }

    public static HistoricoOrdemServicoResponseDto toResponse(HistoricoOrdemServicoModel model) {
        return new HistoricoOrdemServicoResponseDto(model.getId(), model.getDataHistorico(),
                model.getUsuarioHistorico().getId(), model.getOrdemServico().getId(), model.getDescricao());
    }
}
