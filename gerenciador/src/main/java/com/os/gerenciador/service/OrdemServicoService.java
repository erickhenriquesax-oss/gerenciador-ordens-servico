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
import org.springframework.security.access.AccessDeniedException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

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
    @CacheEvict(cacheNames = {"ordens", "historicos"}, allEntries = true)
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
        return ordemServicoRepository.findAllVisibleToUsuario(usuarioId);
    }

    @Transactional
    @Cacheable(cacheNames = "ordens", key = "#p0 + ':' + #p1 + ':' + #p2")
    public List<OrdemServicoResponseDto> getAllOrdemServicos(Long usuarioId, boolean admin, boolean executor) {
        List<OrdemServicoModel> ordens = admin ? findAllEntities()
                : executor ? ordemServicoRepository.findAllVisibleToExecutor(usuarioId)
                : findEntitiesByUsuario(usuarioId);
        return ordens.stream().map(OrdemServicoMapper::toResponse).toList();
    }
    @Transactional public Optional<OrdemServicoResponseDto> getOrdemServicoById(Long id, Long usuarioId, boolean admin, boolean executor) {
        return findEntityById(id).filter(ordem -> admin || (executor
                ? ordem.getStatus() == StatusOS.ABERTA || pertenceAoExecutor(ordem, usuarioId)
                : ordem.getUsuarioSolicitante().getId() == usuarioId)).map(OrdemServicoMapper::toResponse);
    }
    @Transactional public List<OrdemServicoResponseDto> getOrdemServicoByUsuario(Long id) {
        return findEntitiesByUsuario(id).stream().map(OrdemServicoMapper::toResponse).toList();
    }
    @Transactional @CacheEvict(cacheNames = "ordens", allEntries = true)
    public OrdemServicoResponseDto save(OrdemServicoRequestDto request) { return OrdemServicoMapper.toResponse(saveEntity(request)); }
    @Transactional @CacheEvict(cacheNames = "ordens", allEntries = true)
    public OrdemServicoResponseDto atribuirExecutor(Long id, OrdemServicoExecutorRequest request) { return OrdemServicoMapper.toResponse(atribuirExecutorEntity(id, request)); }
    @Transactional @CacheEvict(cacheNames = "ordens", allEntries = true)
    public OrdemServicoResponseDto finalizarOrdemServico(Long id, Long usuarioId, boolean admin) {
        OrdemServicoModel ordem = findEntityById(id).orElseThrow(() -> new EntityNotFoundException("Ordem não encontrada"));
        if (!admin && (ordem.getUsuarioExecutor() == null || ordem.getUsuarioExecutor().getId() != usuarioId)) {
            throw new AccessDeniedException("Apenas o executor atribuído pode finalizar esta ordem");
        }
        return OrdemServicoMapper.toResponse(finalizarEntity(id));
    }

    private boolean pertenceAoUsuario(OrdemServicoModel ordem, Long usuarioId) {
        return ordem.getUsuarioSolicitante().getId() == usuarioId
                || (ordem.getUsuarioExecutor() != null && ordem.getUsuarioExecutor().getId() == usuarioId);
    }

    private boolean pertenceAoExecutor(OrdemServicoModel ordem, Long usuarioId) {
        return ordem.getUsuarioExecutor() != null && ordem.getUsuarioExecutor().getId() == usuarioId;
    }


}
