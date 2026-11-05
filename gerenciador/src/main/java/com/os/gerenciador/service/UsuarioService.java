package com.os.gerenciador.service;

import com.os.gerenciador.dto.request.AlternaTipoUsuarioRequest;
import com.os.gerenciador.dto.request.UsuarioRequestDto;
import com.os.gerenciador.model.TipoUsuarioModel;
import com.os.gerenciador.model.UsuarioModel;
import com.os.gerenciador.repository.TipoUsuarioRepository;
import com.os.gerenciador.repository.UsuarioRepository;
import com.os.gerenciador.mapper.UsuarioMapper;
import com.os.gerenciador.dto.response.UsuarioResponseDto;
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
    private List<UsuarioModel> findAllEntities(){
        return usuarioRepository.findAll();
    }

    @Transactional
    private Optional<UsuarioModel> findEntityById(Long id){
        return usuarioRepository.findById(id);
    }

    @Transactional
    private UsuarioModel saveEntity(UsuarioRequestDto usuarioRequestDto){
        try {
            TipoUsuarioModel tipoUsuarioModel = tipoUsuarioRepository.findById(usuarioRequestDto.tipoUsuario()).get();
            UsuarioModel usuarioModel = UsuarioMapper.toModel(usuarioRequestDto, tipoUsuarioModel);

            return usuarioRepository.save(usuarioModel);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    private UsuarioModel updateEntity(Long id, UsuarioRequestDto usuarioRequestDto){
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
    private UsuarioModel updateTipoUsuarioEntity(Long id, AlternaTipoUsuarioRequest alternaTipoUsuarioRequest){
        UsuarioModel usuarioUpdate = usuarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        TipoUsuarioModel tipo = tipoUsuarioRepository.findById(alternaTipoUsuarioRequest.id()).orElseThrow(() -> new EntityNotFoundException("Tipo de usuário não encontrado"));
        usuarioUpdate.setTipoUsuarioModel(tipo);
        return usuarioRepository.save(usuarioUpdate);
    }

    @Transactional
    private List<UsuarioModel> findEntitiesByTipoUsuario(Long tipoUsuarioId) {
        return usuarioRepository.findAllByTipoUsuarioModel_Id(tipoUsuarioId);
    }

    @Transactional public List<UsuarioResponseDto> findAll() { return findAllEntities().stream().map(UsuarioMapper::toResponse).toList(); }
    @Transactional public Optional<UsuarioResponseDto> findById(Long id) { return findEntityById(id).map(UsuarioMapper::toResponse); }
    @Transactional public UsuarioResponseDto save(UsuarioRequestDto request) { return UsuarioMapper.toResponse(saveEntity(request)); }
    @Transactional public UsuarioResponseDto update(Long id, UsuarioRequestDto request) { return UsuarioMapper.toResponse(updateEntity(id, request)); }
    @Transactional public UsuarioResponseDto updateTipoUsuario(Long id, AlternaTipoUsuarioRequest request) { return UsuarioMapper.toResponse(updateTipoUsuarioEntity(id, request)); }
    @Transactional public List<UsuarioResponseDto> findByTipoUsuario(Long id) { return findEntitiesByTipoUsuario(id).stream().map(UsuarioMapper::toResponse).toList(); }

}
