package com.os.gerenciador.service;

import com.os.gerenciador.repository.UsuarioRepository;
import com.os.gerenciador.config.UsuarioPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        var role = usuario.getUserRole();
        return new UsuarioPrincipal(usuario.getId(), usuario.getEmail(), usuario.getSenha(),
                role == null ? null : role.getAuthority());
    }
}
