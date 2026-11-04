package com.os.gerenciador.repository;

import com.os.gerenciador.model.TipoUsuarioModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoUsuarioRepository extends JpaRepository<TipoUsuarioModel, Long> {
}
