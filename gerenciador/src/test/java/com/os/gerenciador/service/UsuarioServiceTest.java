package com.os.gerenciador.service;

import com.os.gerenciador.dto.request.UsuarioRequestDto;
import com.os.gerenciador.model.TipoUsuarioModel;
import com.os.gerenciador.model.UsuarioModel;
import com.os.gerenciador.repository.TipoUsuarioRepository;
import com.os.gerenciador.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TipoUsuarioRepository tipoUsuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UsuarioService usuarioService;

    @Test
    void cadastraSolicitanteComSenhaCodificada() {
        TipoUsuarioModel solicitante = TipoUsuarioModel.builder().id(2L).descricao("SOLICITANTE").build();
        given(tipoUsuarioRepository.findById(2L)).willReturn(Optional.of(solicitante));
        given(passwordEncoder.encode("senha-segura")).willReturn("hash-bcrypt");
        given(usuarioRepository.save(any(UsuarioModel.class))).willAnswer(invocation -> invocation.getArgument(0));

        usuarioService.save(usuarioValido());

        ArgumentCaptor<UsuarioModel> captor = ArgumentCaptor.forClass(UsuarioModel.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("hash-bcrypt", captor.getValue().getSenha());
        assertEquals(solicitante, captor.getValue().getTipoUsuarioModel());
    }

    @Test
    void cadastroNaoPermiteCriarUsuarioAdmin() {
        given(tipoUsuarioRepository.findById(3L))
                .willReturn(Optional.of(TipoUsuarioModel.builder().id(3L).descricao("ADMIN").build()));

        assertThrows(AccessDeniedException.class, () -> usuarioService.save(usuarioValidoComTipo(3L)));
    }

    @Test
    void cadastroFalhaQuandoTipoNaoExiste() {
        given(tipoUsuarioRepository.findById(2L)).willReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> usuarioService.save(usuarioValido()));
    }

    private UsuarioRequestDto usuarioValido() {
        return usuarioValidoComTipo(2L);
    }

    private UsuarioRequestDto usuarioValidoComTipo(Long tipoId) {
        return new UsuarioRequestDto("Ana Silva", "ana@example.com", "senha-segura",
                "529.982.247-25", "11999990000", tipoId, LocalDate.of(1995, 4, 12));
    }
}
