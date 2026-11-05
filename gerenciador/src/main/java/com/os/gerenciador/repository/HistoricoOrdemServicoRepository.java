package com.os.gerenciador.repository;

import com.os.gerenciador.model.HistoricoOrdemServicoModel;
import com.os.gerenciador.model.OrdemServicoModel;
import com.os.gerenciador.model.UsuarioModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoOrdemServicoRepository extends JpaRepository<HistoricoOrdemServicoModel, Long> {

    List<HistoricoOrdemServicoModel> findAllByUsuarioHistorico(UsuarioModel usuarioHistorico);
    List<HistoricoOrdemServicoModel> findAllByOrdemServico(OrdemServicoModel ordemServico);
}
