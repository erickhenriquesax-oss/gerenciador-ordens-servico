package com.os.gerenciador.service;


import com.os.gerenciador.dto.request.TipoUsuarioRequestDto;
import com.os.gerenciador.model.TipoUsuarioModel;
import com.os.gerenciador.mapper.TipoUsuarioMapper;
import com.os.gerenciador.dto.response.TipoUsuarioResponseDto;
import com.os.gerenciador.repository.TipoUsuarioRepository;
import jakarta.transaction.Transactional;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TipoUsuarioService {

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Transactional
    private TipoUsuarioModel saveEntity(TipoUsuarioRequestDto tipoUsuarioRequestDto) {
        return tipoUsuarioRepository.save(TipoUsuarioMapper.toModel(tipoUsuarioRequestDto));
    }

    @Transactional
    private List<TipoUsuarioModel> findAllEntities() {
        return tipoUsuarioRepository.findAll();
    }

    @Transactional
    private TipoUsuarioModel findEntityById(Long id) {
        return tipoUsuarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Tipo de usuário não encontrado"));
    }

    @Transactional
    public void deleteById(Long id) {
        tipoUsuarioRepository.deleteById(id);
    }

    @Transactional public List<TipoUsuarioResponseDto> findAll() { return findAllEntities().stream().map(TipoUsuarioMapper::toResponse).toList(); }
    @Transactional public TipoUsuarioResponseDto save(TipoUsuarioRequestDto request) { return TipoUsuarioMapper.toResponse(saveEntity(request)); }
    @Transactional public TipoUsuarioResponseDto findById(Long id) { return TipoUsuarioMapper.toResponse(findEntityById(id)); }

}


