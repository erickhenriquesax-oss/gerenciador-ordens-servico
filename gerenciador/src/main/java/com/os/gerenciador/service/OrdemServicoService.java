package com.os.gerenciador.service;

import com.os.gerenciador.Enums.StatusOS;
import com.os.gerenciador.Enums.UserRoles;
import com.os.gerenciador.dto.request.OrdemServicoExecutorRequest;
import com.os.gerenciador.dto.request.OrdemServicoRequestDto;
import com.os.gerenciador.model.OrdemServicoModel;
import com.os.gerenciador.model.UsuarioModel;
import com.os.gerenciador.repository.OrdemServicoRepository;
import com.os.gerenciador.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrdemServicoService {

    @Autowired
    private OrdemServicoRepository ordemServicoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public OrdemServicoModel save(OrdemServicoRequestDto ordemServicoDto) {

        UsuarioModel solicitante = usuarioRepository.findById(ordemServicoDto.usuarioSolicitante())
                .orElseThrow(() -> new EntityNotFoundException("Solicitante não encontrado"));
        if (solicitante.getTipoUsuarioModel().toUserRole() != UserRoles.SOLICITANTE) {
            throw new IllegalArgumentException(
                    "Somente usuários com papel SOLICITANTE podem abrir ordens."
            );
        }
        OrdemServicoModel ordem = new OrdemServicoModel();
        ordem.setDescricao(ordemServicoDto.descricao());
        ordem.setStatus(StatusOS.ABERTA);
        ordem.setUsuarioSolicitante(solicitante);
        ordem.setDataCriacao(LocalDateTime.now());
        return ordemServicoRepository.save(ordem);
    }

    @Transactional
    public OrdemServicoModel atribuirExecutor(Long ordemId, OrdemServicoExecutorRequest ordemServicoExecutorRequest) {
        OrdemServicoModel ordem = ordemServicoRepository.findById(ordemId).orElseThrow(() -> new EntityNotFoundException("Ordem não encontrada"));
        UsuarioModel executor = usuarioRepository.findById(ordemServicoExecutorRequest.idExecutor()).orElseThrow(() -> new EntityNotFoundException("Executor não encontrado"));

        if (executor.getTipoUsuarioModel().toUserRole() != UserRoles.EXECUTOR) {
            throw new IllegalArgumentException("Somente usuários com papel EXECUTOR podem colocar ordem em processo.");
        }
        if (ordem.getStatus() != StatusOS.ABERTA) {
            throw new IllegalStateException("Apenas ordens abertas podem ser assumidas.");
        }

        ordem.setStatus(StatusOS.EM_PROCESSO);
        ordem.setUsuarioExecutor(executor);
        return ordemServicoRepository.save(ordem);
    }

    @Transactional
    public OrdemServicoModel finalizarOrdemServico(Long ordemId) {
        OrdemServicoModel ordem = ordemServicoRepository.findById(ordemId).orElseThrow(() -> new EntityNotFoundException("Ordem não encontrada"));
        if(ordem.getStatus() != StatusOS.EM_PROCESSO) {
            throw new IllegalStateException("Apenas ordens de serviço em processo podem ser finalizadas.");
        }
        ordem.setStatus(StatusOS.ENCERRADA);
        ordem.setDataFinalizacao(LocalDateTime.now());

        return ordemServicoRepository.save(ordem);
    }

    @Transactional
    public void deletarOrdemServico(Long ordemId) {
        ordemServicoRepository.deleteById(ordemId);
    }

    @Transactional
    public Optional<OrdemServicoModel> getOrdemServicoById(Long ordemId) {
        return ordemServicoRepository.findById(ordemId);
    }

    @Transactional
    public List<OrdemServicoModel> getAllOrdemServicos() {
        return ordemServicoRepository.findAll();
    }

    @Transactional
    public List<OrdemServicoModel> getOrdemServicosByUsuario(Long usuarioId) {
        return ordemServicoRepository.findAllByTipoUsuarioModel_Id(usuarioId);
    }


}
