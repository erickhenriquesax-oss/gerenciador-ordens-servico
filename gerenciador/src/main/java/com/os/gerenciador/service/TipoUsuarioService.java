package com.os.gerenciador.service;


import com.os.gerenciador.dto.request.TipoUsuarioRequestDto;
import com.os.gerenciador.model.TipoUsuarioModel;
import com.os.gerenciador.repository.TipoUsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TipoUsuarioService {

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Transactional
    public TipoUsuarioModel save(TipoUsuarioRequestDto tipoUsuarioRequestDto) {
        TipoUsuarioModel tipoUsuarioModel = new TipoUsuarioModel();
        tipoUsuarioModel.setDescricao(tipoUsuarioRequestDto.descricao());

        return tipoUsuarioRepository.save(tipoUsuarioModel);
    }

    @Transactional
    public List<TipoUsuarioModel> findAll() {
        return tipoUsuarioRepository.findAll();
    }

    @Transactional
    public TipoUsuarioModel findById(Long id) {
        return tipoUsuarioRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteById(Long id) {
        tipoUsuarioRepository.deleteById(id);
    }

}


