package com.os.gerenciador.service;

import com.os.gerenciador.dto.request.HistoricoOrdemServicoRequest;
import com.os.gerenciador.dto.response.HistoricoOrdemServicoResponseDto;
import com.os.gerenciador.mapper.HistoricoOrdemServicoMapper;
import com.os.gerenciador.model.HistoricoOrdemServicoModel;
import com.os.gerenciador.model.OrdemServicoModel;
import com.os.gerenciador.model.UsuarioModel;
import com.os.gerenciador.repository.HistoricoOrdemServicoRepository;
import com.os.gerenciador.repository.OrdemServicoRepository;
import com.os.gerenciador.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoricoOrdemServicoService {
    private final HistoricoOrdemServicoRepository historicoRepository;
    private final OrdemServicoRepository ordemRepository;
    private final UsuarioRepository usuarioRepository;

    public HistoricoOrdemServicoService(HistoricoOrdemServicoRepository historicoRepository,
                                        OrdemServicoRepository ordemRepository,
                                        UsuarioRepository usuarioRepository) {
        this.historicoRepository = historicoRepository;
        this.ordemRepository = ordemRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    @CacheEvict(cacheNames = "historicos", allEntries = true)
    public HistoricoOrdemServicoResponseDto save(HistoricoOrdemServicoRequest request, Long usuarioAutenticadoId, boolean admin) {
        UsuarioModel usuario = usuarioRepository.findById(request.idUsuario())
                .orElseThrow(() -> new EntityNotFoundException("Usuário para histórico não encontrado"));
        OrdemServicoModel ordem = findOrdem(request.idOrdemServico());
        if (!admin && (usuario.getId() != usuarioAutenticadoId || !pertenceAoUsuario(ordem, usuarioAutenticadoId))) {
            throw new AccessDeniedException("Você só pode registrar histórico em uma ordem própria");
        }

        HistoricoOrdemServicoModel historico = HistoricoOrdemServicoMapper.toModel(request, usuario, ordem);
        return HistoricoOrdemServicoMapper.toResponse(historicoRepository.save(historico));
    }

    @Transactional
    public HistoricoOrdemServicoResponseDto findById(Long id, Long usuarioId, boolean admin) {
        HistoricoOrdemServicoModel historico = findHistorico(id);
        if (!admin && historico.getUsuarioHistorico().getId() != usuarioId
                && !pertenceAoUsuario(historico.getOrdemServico(), usuarioId)) {
            throw new AccessDeniedException("Você não tem acesso a este histórico");
        }
        return HistoricoOrdemServicoMapper.toResponse(historico);
    }

    @Transactional
    @Cacheable(cacheNames = "historicos", key = "#p0 + ':' + #p1")
    public List<HistoricoOrdemServicoResponseDto> findAll(Long usuarioId, boolean admin) {
        List<HistoricoOrdemServicoModel> historicos = admin
                ? historicoRepository.findAll()
                : historicoRepository.findAllVisibleToUsuario(usuarioId);
        return historicos.stream().map(HistoricoOrdemServicoMapper::toResponse).toList();
    }

    @Transactional
    public List<HistoricoOrdemServicoResponseDto> findAllByUsuario(Long id, Long usuarioAutenticadoId, boolean admin) {
        if (!admin && !id.equals(usuarioAutenticadoId)) {
            throw new AccessDeniedException("Você só pode consultar seu próprio histórico");
        }
        UsuarioModel usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        return historicoRepository.findAllByUsuarioHistorico(usuario).stream()
                .map(HistoricoOrdemServicoMapper::toResponse).toList();
    }

    @Transactional
    public List<HistoricoOrdemServicoResponseDto> findAllByOrdem(Long ordemId, Long usuarioId, boolean admin) {
        OrdemServicoModel ordem = findOrdem(ordemId);
        if (!admin && !pertenceAoUsuario(ordem, usuarioId)) {
            throw new AccessDeniedException("Você não tem acesso ao histórico desta ordem");
        }
        return historicoRepository.findAllByOrdemServico(ordem).stream()
                .map(HistoricoOrdemServicoMapper::toResponse).toList();
    }

    @Transactional
    @CacheEvict(cacheNames = "historicos", allEntries = true)
    public HistoricoOrdemServicoResponseDto update(Long id, HistoricoOrdemServicoRequest request) {
        HistoricoOrdemServicoModel historico = findHistorico(id);
        UsuarioModel usuario = usuarioRepository.findById(request.idUsuario())
                .orElseThrow(() -> new EntityNotFoundException("Usuário para histórico não encontrado"));
        OrdemServicoModel ordem = findOrdem(request.idOrdemServico());
        historico.setOrdemServico(ordem);
        historico.setUsuarioHistorico(usuario);
        historico.setDescricao(request.descricao());
        return HistoricoOrdemServicoMapper.toResponse(historicoRepository.save(historico));
    }

    @Transactional
    @CacheEvict(cacheNames = "historicos", allEntries = true)
    public void delete(Long id) {
        historicoRepository.delete(findHistorico(id));
    }

    private HistoricoOrdemServicoModel findHistorico(Long id) {
        return historicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Histórico não encontrado"));
    }

    private OrdemServicoModel findOrdem(Long id) {
        return ordemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ordem de Serviço não encontrada"));
    }

    private boolean pertenceAoUsuario(OrdemServicoModel ordem, Long usuarioId) {
        return ordem.getUsuarioSolicitante().getId() == usuarioId
                || (ordem.getUsuarioExecutor() != null && ordem.getUsuarioExecutor().getId() == usuarioId);
    }
}
