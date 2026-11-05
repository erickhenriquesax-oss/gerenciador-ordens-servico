package com.os.gerenciador.config;

import com.os.gerenciador.Enums.UserRoles;
import com.os.gerenciador.model.TipoUsuarioModel;
import com.os.gerenciador.repository.TipoUsuarioRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class TipoUsuarioDataInitializer implements ApplicationRunner {
    private final TipoUsuarioRepository tipoUsuarioRepository;

    public TipoUsuarioDataInitializer(TipoUsuarioRepository tipoUsuarioRepository) {
        this.tipoUsuarioRepository = tipoUsuarioRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (tipoUsuarioRepository.count() != 0) {
            return;
        }

        for (UserRoles role : UserRoles.values()) {
            TipoUsuarioModel tipo = tipoUsuarioRepository.save(
                    TipoUsuarioModel.builder().descricao(role.name()).build());
            if (tipo.getId() != role.getId()) {
                throw new IllegalStateException("IDs dos tipos de usuário não correspondem às roles configuradas");
            }
        }
    }
}
