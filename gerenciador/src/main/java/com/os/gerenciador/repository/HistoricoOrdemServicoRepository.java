package com.os.gerenciador.repository;

import com.os.gerenciador.model.HistoricoOrdemServicoModel;
import com.os.gerenciador.model.OrdemServicoModel;
import com.os.gerenciador.model.UsuarioModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistoricoOrdemServicoRepository extends JpaRepository<HistoricoOrdemServicoModel, Long> {

    List<HistoricoOrdemServicoModel> findAllByUsuarioHistorico(UsuarioModel usuarioHistorico);
    List<HistoricoOrdemServicoModel> findAllByOrdemServico(OrdemServicoModel ordemServico);

    @Query("select historico from HistoricoOrdemServicoModel historico where historico.usuarioHistorico.id = :usuarioId or historico.ordemServico.usuarioSolicitante.id = :usuarioId or historico.ordemServico.usuarioExecutor.id = :usuarioId")
    List<HistoricoOrdemServicoModel> findAllVisibleToUsuario(@Param("usuarioId") Long usuarioId);
}
