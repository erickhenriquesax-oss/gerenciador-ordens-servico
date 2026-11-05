package com.os.gerenciador.service;

import com.os.gerenciador.Enums.StatusOS;
import com.os.gerenciador.dto.request.OrdemServicoExecutorRequest;
import com.os.gerenciador.dto.request.OrdemServicoRequestDto;
import com.os.gerenciador.model.OrdemServicoModel;
import com.os.gerenciador.model.TipoUsuarioModel;
import com.os.gerenciador.model.UsuarioModel;
import com.os.gerenciador.repository.OrdemServicoRepository;
import com.os.gerenciador.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {
    @Mock private OrdemServicoRepository ordemServicoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private OrdemServicoService ordemServicoService;

    @Test
    void cadastraOrdemAbertaParaSolicitante() {
        UsuarioModel solicitante = usuario(10L, 2L);
        given(usuarioRepository.findById(10L)).willReturn(Optional.of(solicitante));
        given(ordemServicoRepository.save(any(OrdemServicoModel.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        var resposta = ordemServicoService.save(new OrdemServicoRequestDto("Consertar torneira", 10L));

        assertEquals(StatusOS.ABERTA, resposta.status());
        assertEquals(10L, resposta.idSolicitante());
    }

    @Test
    void executorAssumeOrdemAberta() {
        UsuarioModel solicitante = usuario(10L, 2L);
        UsuarioModel executor = usuario(20L, 1L);
        OrdemServicoModel ordem = OrdemServicoModel.builder()
                .id(30L).descricao("Consertar torneira").status(StatusOS.ABERTA)
                .usuarioSolicitante(solicitante).dataCriacao(java.time.LocalDateTime.now()).build();
        given(ordemServicoRepository.findById(30L)).willReturn(Optional.of(ordem));
        given(usuarioRepository.findById(20L)).willReturn(Optional.of(executor));
        given(ordemServicoRepository.save(any(OrdemServicoModel.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        var resposta = ordemServicoService.atribuirExecutor(30L, new OrdemServicoExecutorRequest(20L));

        assertEquals(StatusOS.EM_PROCESSO, resposta.status());
        assertEquals(20L, resposta.idExecutor());
    }

    @Test
    void executorVeTodasAsOrdensAbertas() {
        OrdemServicoModel aberta = OrdemServicoModel.builder()
                .id(30L).descricao("Consertar torneira").status(StatusOS.ABERTA)
                .usuarioSolicitante(usuario(10L, 2L)).dataCriacao(java.time.LocalDateTime.now()).build();
        given(ordemServicoRepository.findAllVisibleToExecutor(20L)).willReturn(List.of(aberta));

        var resposta = ordemServicoService.getAllOrdemServicos(20L, false, true);

        assertEquals(1, resposta.size());
        assertEquals(30L, resposta.getFirst().id());
        verify(ordemServicoRepository).findAllVisibleToExecutor(20L);
    }

    @Test
    void naoAtribuiOrdemAQuemNaoEhExecutor() {
        OrdemServicoModel ordem = OrdemServicoModel.builder()
                .id(30L).status(StatusOS.ABERTA).usuarioSolicitante(usuario(10L, 2L)).build();
        given(ordemServicoRepository.findById(30L)).willReturn(Optional.of(ordem));
        given(usuarioRepository.findById(11L)).willReturn(Optional.of(usuario(11L, 2L)));

        assertThrows(IllegalArgumentException.class,
                () -> ordemServicoService.atribuirExecutor(30L, new OrdemServicoExecutorRequest(11L)));
    }

    private UsuarioModel usuario(Long id, Long tipoId) {
        return UsuarioModel.builder().id(id)
                .tipoUsuarioModel(TipoUsuarioModel.builder().id(tipoId).descricao("ROLE").build())
                .build();
    }
}
