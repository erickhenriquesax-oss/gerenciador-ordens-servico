package com.os.gerenciador.dto.response;

import java.time.LocalDateTime;

public record HistoricoOrdemServicoResponseDto(Long id, LocalDateTime dataHistorico,
                                               Long idUsuario, Long idOrdemServico, String descricao) {
}
