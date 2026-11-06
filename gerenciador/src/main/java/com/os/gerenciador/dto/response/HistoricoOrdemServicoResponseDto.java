package com.os.gerenciador.dto.response;

import java.time.LocalDateTime;
import java.io.Serializable;

public record HistoricoOrdemServicoResponseDto(Long id, LocalDateTime dataHistorico,
                                               Long idUsuario, Long idOrdemServico,
                                               String descricao) implements Serializable {
}
