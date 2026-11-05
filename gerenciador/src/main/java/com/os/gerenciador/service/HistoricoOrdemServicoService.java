package com.os.gerenciador.service;

import com.os.gerenciador.dto.request.HistoricoOrdemServicoRequest;
import com.os.gerenciador.model.HistoricoOrdemServicoModel;
import com.os.gerenciador.model.OrdemServicoModel;
import com.os.gerenciador.model.UsuarioModel;
import com.os.gerenciador.mapper.HistoricoOrdemServicoMapper;
import com.os.gerenciador.dto.response.HistoricoOrdemServicoResponseDto;
import com.os.gerenciador.repository.HistoricoOrdemServicoRepository;
import com.os.gerenciador.repository.OrdemServicoRepository;
import com.os.gerenciador.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoricoOrdemServicoService {

    @Autowired
    private HistoricoOrdemServicoRepository historicoOrdemServicoRepository;

    @Autowired
    private OrdemServicoRepository ordemServicoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    private HistoricoOrdemServicoModel saveEntity(HistoricoOrdemServicoRequest historicoOrdemServicoRequest) {
        UsuarioModel usuario = usuarioRepository.findById(historicoOrdemServicoRequest.idUsuario())
                .orElseThrow(() -> new EntityNotFoundException("Usuário para histórico não encontrado"));

        OrdemServicoModel ordem = ordemServicoRepository.findById(historicoOrdemServicoRequest.idOrdemServico())
                .orElseThrow(() -> new EntityNotFoundException("Ordem de Serviço não encontrada"));

        HistoricoOrdemServicoModel historico = HistoricoOrdemServicoMapper.toModel(historicoOrdemServicoRequest, usuario, ordem);
        return historicoOrdemServicoRepository.save(historico);
    }

    @Transactional
    private HistoricoOrdemServicoModel findEntityById(Long id) {
        return historicoOrdemServicoRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Histórico de Ordem de Serviço não encontrada"));
    }

    @Transactional
    private List<HistoricoOrdemServicoModel> findAllEntities() {
        return historicoOrdemServicoRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        HistoricoOrdemServicoModel historicoOrdemServicoModel = historicoOrdemServicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Histórico não encontrado"));
        historicoOrdemServicoRepository.delete(historicoOrdemServicoModel);
    }

    @Transactional
    private List<HistoricoOrdemServicoModel> findEntitiesByUsuario(Long id) {
        UsuarioModel usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        return historicoOrdemServicoRepository.findAllByUsuarioHistorico(usuario);
    }

    @Transactional
    private List<HistoricoOrdemServicoModel> findEntitiesByOrdem(Long id){
        OrdemServicoModel ordemServico = ordemServicoRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Ordem de Serviço não encontrada"));
        return historicoOrdemServicoRepository.findAllByOrdemServico(ordemServico);
    }

    @Transactional
    private HistoricoOrdemServicoModel updateEntity(Long id,HistoricoOrdemServicoRequest historicoOrdemServicoRequest) {
        HistoricoOrdemServicoModel historico=historicoOrdemServicoRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Histórico não encontrado")
        );
        UsuarioModel usuario = usuarioRepository.findById(historicoOrdemServicoRequest.idUsuario())
                .orElseThrow(() -> new EntityNotFoundException("Usuário para histórico não encontrado"));

        OrdemServicoModel ordem = ordemServicoRepository.findById(historicoOrdemServicoRequest.idOrdemServico())
                .orElseThrow(() -> new EntityNotFoundException("Ordem de Serviço não encontrada"));
        historico.setOrdemServico(ordem);
        historico.setUsuarioHistorico(usuario);
        historico.setDescricao(historicoOrdemServicoRequest.descricao());
        return historicoOrdemServicoRepository.save(historico);
    }

    @Transactional public HistoricoOrdemServicoResponseDto save(HistoricoOrdemServicoRequest request) { return HistoricoOrdemServicoMapper.toResponse(saveEntity(request)); }
    @Transactional public HistoricoOrdemServicoResponseDto findById(Long id) { return HistoricoOrdemServicoMapper.toResponse(findEntityById(id)); }
    @Transactional public List<HistoricoOrdemServicoResponseDto> findAll() { return findAllEntities().stream().map(HistoricoOrdemServicoMapper::toResponse).toList(); }
    @Transactional public List<HistoricoOrdemServicoResponseDto> findAllByUsuarioHistorico(Long id) { return findEntitiesByUsuario(id).stream().map(HistoricoOrdemServicoMapper::toResponse).toList(); }
    @Transactional public List<HistoricoOrdemServicoResponseDto> findAllByOrdemServico(Long id) { return findEntitiesByOrdem(id).stream().map(HistoricoOrdemServicoMapper::toResponse).toList(); }
    @Transactional public HistoricoOrdemServicoResponseDto update(Long id, HistoricoOrdemServicoRequest request) { return HistoricoOrdemServicoMapper.toResponse(updateEntity(id, request)); }
}
