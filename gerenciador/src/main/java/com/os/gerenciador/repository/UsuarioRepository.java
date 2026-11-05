package com.os.gerenciador.repository;

import com.os.gerenciador.model.UsuarioModel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioModel, Long> {
    List<UsuarioModel> findAllByTipoUsuarioModel_Id(Long tipoUsuarioId);

    @EntityGraph(attributePaths = "tipoUsuarioModel")
    Optional<UsuarioModel> findByEmail(String email);
}
