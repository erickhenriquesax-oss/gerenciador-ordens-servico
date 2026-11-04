package com.os.gerenciador.service;

import com.os.gerenciador.dto.request.AlternaTipoUsuarioRequest;
import com.os.gerenciador.dto.request.TipoUsuarioRequestDto;
import com.os.gerenciador.dto.request.UsuarioRequestDto;
import com.os.gerenciador.model.TipoUsuarioModel;
import com.os.gerenciador.model.UsuarioModel;
import com.os.gerenciador.repository.TipoUsuarioRepository;
import com.os.gerenciador.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Transactional
    public List<UsuarioModel> findAll(){
        return usuarioRepository.findAll();
    }

    @Transactional
    public Optional<UsuarioModel> findById(Long id){
        return usuarioRepository.findById(id);
    }

    @Transactional
    public UsuarioModel save(UsuarioRequestDto usuarioRequestDto){
        try {
            UsuarioModel usuarioModel = new UsuarioModel();
            usuarioModel.setNome(usuarioRequestDto.nome());
            usuarioModel.setCpf(usuarioRequestDto.cpf());
            usuarioModel.setTelefone(usuarioRequestDto.telefone());
            usuarioModel.setEmail(usuarioRequestDto.email());
            usuarioModel.setSenha(usuarioRequestDto.senha());

            TipoUsuarioModel tipoUsuarioModel = tipoUsuarioRepository.findById(usuarioRequestDto.tipoUsuario()).get();
            usuarioModel.setTipoUsuarioModel(tipoUsuarioModel);

            return usuarioRepository.save(usuarioModel);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    public UsuarioModel update(Long id, UsuarioRequestDto usuarioRequestDto){
        UsuarioModel usuarioUpdate = usuarioRepository.findById(id).get();
        usuarioUpdate.setNome(usuarioRequestDto.nome());
        usuarioUpdate.setCpf(usuarioRequestDto.cpf());
        usuarioUpdate.setTelefone(usuarioRequestDto.telefone());
        usuarioUpdate.setEmail(usuarioRequestDto.email());
        usuarioUpdate.setSenha(usuarioRequestDto.senha());
        return usuarioRepository.save(usuarioUpdate);
    }

    @Transactional
    public void delete(Long id){
        usuarioRepository.deleteById(id);
    }

    @Transactional
    public UsuarioModel updateTipoUsuario(Long id, AlternaTipoUsuarioRequest alternaTipoUsuarioRequest){
        UsuarioModel usuarioUpdate = usuarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        TipoUsuarioModel tipo = tipoUsuarioRepository.findById(alternaTipoUsuarioRequest.id()).orElseThrow(() -> new EntityNotFoundException("Tipo de usuário não encontrado"));
        usuarioUpdate.setTipoUsuarioModel(tipo);
        return usuarioRepository.save(usuarioUpdate);
    }

    @Transactional
    public List<UsuarioModel> findByTipoUsuario(Long tipoUsuarioId) {
        return usuarioRepository.findAllByTipoUsuarioModel_Id(tipoUsuarioId);
    }

}
