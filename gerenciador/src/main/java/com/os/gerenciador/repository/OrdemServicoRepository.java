package com.os.gerenciador.repository;

import com.os.gerenciador.model.OrdemServicoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrdemServicoRepository extends JpaRepository<OrdemServicoModel, Long> {

    List<OrdemServicoModel> findAllByUsuarioSolicitante_Id(Long usuarioId);

    @Query("select ordem from OrdemServicoModel ordem where ordem.usuarioSolicitante.id = :usuarioId or ordem.usuarioExecutor.id = :usuarioId")
    List<OrdemServicoModel> findAllVisibleToUsuario(@Param("usuarioId") Long usuarioId);

    @Query("select ordem from OrdemServicoModel ordem where ordem.status = com.os.gerenciador.Enums.StatusOS.ABERTA or ordem.usuarioExecutor.id = :usuarioId")
    List<OrdemServicoModel> findAllVisibleToExecutor(@Param("usuarioId") Long usuarioId);
}
