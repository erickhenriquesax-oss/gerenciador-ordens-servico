package com.os.gerenciador.repository;

import com.os.gerenciador.model.OrdemServicoModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdemServicoRepository extends JpaRepository<OrdemServicoModel, Long> {

    List<OrdemServicoModel> findAllByUsuarioSolicitante_Id(Long usuarioId);
}
