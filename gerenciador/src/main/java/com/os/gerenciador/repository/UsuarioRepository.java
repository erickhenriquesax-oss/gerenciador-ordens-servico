package com.os.gerenciador.repository;

import com.os.gerenciador.model.UsuarioModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRepository extends JpaRepository<UsuarioModel, Long> {
    List<UsuarioModel> findAllByTipoUsuarioModel_Id(Long tipoUsuarioId);
}
