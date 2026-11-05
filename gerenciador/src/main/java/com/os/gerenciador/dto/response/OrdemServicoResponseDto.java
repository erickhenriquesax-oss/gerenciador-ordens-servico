package com.os.gerenciador.dto.response;

import com.os.gerenciador.Enums.StatusOS;
import java.io.Serializable;
import java.time.LocalDateTime;

public record OrdemServicoResponseDto(Long id, String descricao, StatusOS status,
                                      Long idSolicitante, Long idExecutor,
                                      LocalDateTime dataCriacao, LocalDateTime dataFinalizacao) implements Serializable {
}
