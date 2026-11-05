package com.os.gerenciador.service;

import com.os.gerenciador.dto.request.HistoricoOrdemServicoRequest;
import com.os.gerenciador.model.HistoricoOrdemServicoModel;
import com.os.gerenciador.model.OrdemServicoModel;
import com.os.gerenciador.model.UsuarioModel;
import com.os.gerenciador.repository.HistoricoOrdemServicoRepository;
import com.os.gerenciador.repository.OrdemServicoRepository;
import com.os.gerenciador.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    public HistoricoOrdemServicoModel save(HistoricoOrdemServicoRequest historicoOrdemServicoRequest) {
        UsuarioModel usuario = usuarioRepository.findById(historicoOrdemServicoRequest.idUsuario())
                .orElseThrow(() -> new EntityNotFoundException("Usuário para histórico não encontrado"));

        OrdemServicoModel ordem = ordemServicoRepository.findById(historicoOrdemServicoRequest.idOrdemServico())
                .orElseThrow(() -> new EntityNotFoundException("Ordem de Serviço não encontrada"));

        HistoricoOrdemServicoModel historico = new HistoricoOrdemServicoModel();
        historico.setDataHistorico(LocalDateTime.now());
        historico.setUsuarioHistorico(usuario);
        historico.setOrdemServico(ordem);
        historico.setDescricao(historicoOrdemServicoRequest.descricao());
        return historicoOrdemServicoRepository.save(historico);
    }

    @Transactional
    public HistoricoOrdemServicoModel findById(Long id) {
        return historicoOrdemServicoRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Histórico de Ordem de Serviço não encontrada"));
    }

    @Transactional
    public List<HistoricoOrdemServicoModel> findAll() {
        return historicoOrdemServicoRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        HistoricoOrdemServicoModel historicoOrdemServicoModel = historicoOrdemServicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Histórico não encontrado"));
        historicoOrdemServicoRepository.delete(historicoOrdemServicoModel);
    }

    @Transactional
    public List<HistoricoOrdemServicoModel> findAllByUsuarioHistorico(Long id) {
        UsuarioModel usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        return historicoOrdemServicoRepository.findAllByUsuarioHistorico(usuario);
    }

    @Transactional
    public List<HistoricoOrdemServicoModel> findAllByOrdemServico(Long id){
        OrdemServicoModel ordemServico = ordemServicoRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Ordem de Serviço não encontrada"));
        return historicoOrdemServicoRepository.findAllByOrdemServico(ordemServico);
    }

    @Transactional
    public HistoricoOrdemServicoModel update(Long id,HistoricoOrdemServicoRequest historicoOrdemServicoRequest) {
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
}
