package com.os.gerenciador.service;

import com.os.gerenciador.Enums.StatusOS;
import com.os.gerenciador.Enums.UserRoles;
import com.os.gerenciador.dto.request.OrdemServicoExecutorRequest;
import com.os.gerenciador.dto.request.OrdemServicoRequestDto;
import com.os.gerenciador.model.OrdemServicoModel;
import com.os.gerenciador.model.UsuarioModel;
import com.os.gerenciador.mapper.OrdemServicoMapper;
import com.os.gerenciador.dto.response.OrdemServicoResponseDto;
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
    private OrdemServicoModel saveEntity(OrdemServicoRequestDto ordemServicoDto) {

        UsuarioModel solicitante = usuarioRepository.findById(ordemServicoDto.usuarioSolicitante())
                .orElseThrow(() -> new EntityNotFoundException("Solicitante não encontrado"));
        if (solicitante.getTipoUsuarioModel().toUserRole() != UserRoles.SOLICITANTE) {
            throw new IllegalArgumentException(
                    "Somente usuários com papel SOLICITANTE podem abrir ordens."
            );
        }
        OrdemServicoModel ordem = OrdemServicoMapper.toModel(ordemServicoDto, solicitante);
        return ordemServicoRepository.save(ordem);
    }

    @Transactional
    private OrdemServicoModel atribuirExecutorEntity(Long ordemId, OrdemServicoExecutorRequest ordemServicoExecutorRequest) {
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
    private OrdemServicoModel finalizarEntity(Long ordemId) {
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
    private Optional<OrdemServicoModel> findEntityById(Long ordemId) {
        return ordemServicoRepository.findById(ordemId);
    }

    @Transactional
    private List<OrdemServicoModel> findAllEntities() {
        return ordemServicoRepository.findAll();
    }

    @Transactional
    private List<OrdemServicoModel> findEntitiesByUsuario(Long usuarioId) {
        return ordemServicoRepository.findAllByUsuarioSolicitante_Id(usuarioId);
    }

    @Transactional public List<OrdemServicoResponseDto> getAllOrdemServicos() { return findAllEntities().stream().map(OrdemServicoMapper::toResponse).toList(); }
    @Transactional public Optional<OrdemServicoResponseDto> getOrdemServicoById(Long id) { return findEntityById(id).map(OrdemServicoMapper::toResponse); }
    @Transactional public List<OrdemServicoResponseDto> getOrdemServicosByUsuario(Long id) { return findEntitiesByUsuario(id).stream().map(OrdemServicoMapper::toResponse).toList(); }
    @Transactional public OrdemServicoResponseDto save(OrdemServicoRequestDto request) { return OrdemServicoMapper.toResponse(saveEntity(request)); }
    @Transactional public OrdemServicoResponseDto atribuirExecutor(Long id, OrdemServicoExecutorRequest request) { return OrdemServicoMapper.toResponse(atribuirExecutorEntity(id, request)); }
    @Transactional public OrdemServicoResponseDto finalizarOrdemServico(Long id) { return OrdemServicoMapper.toResponse(finalizarEntity(id)); }


}
