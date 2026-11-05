package com.os.gerenciador.service;

import com.os.gerenciador.dto.request.AlternaTipoUsuarioRequest;
import com.os.gerenciador.dto.request.UsuarioRequestDto;
import com.os.gerenciador.model.TipoUsuarioModel;
import com.os.gerenciador.model.UsuarioModel;
import com.os.gerenciador.repository.TipoUsuarioRepository;
import com.os.gerenciador.repository.UsuarioRepository;
import com.os.gerenciador.mapper.UsuarioMapper;
import com.os.gerenciador.dto.response.UsuarioResponseDto;
import com.os.gerenciador.Enums.UserRoles;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        TipoUsuarioModel tipoUsuarioModel = tipoUsuarioRepository.findById(usuarioRequestDto.tipoUsuario())
                .orElseThrow(() -> new EntityNotFoundException("Tipo de usuário não encontrado"));
        if (tipoUsuarioModel.toUserRole() != UserRoles.SOLICITANTE) {
            throw new AccessDeniedException("O cadastro público permite criar apenas usuários SOLICITANTE");
        }
        UsuarioModel usuarioModel = UsuarioMapper.toModel(usuarioRequestDto, tipoUsuarioModel);
        usuarioModel.setSenha(passwordEncoder.encode(usuarioRequestDto.senha()));
        return usuarioRepository.save(usuarioModel);
    }

    @Transactional
    private UsuarioModel updateEntity(Long id, UsuarioRequestDto usuarioRequestDto){
        UsuarioModel usuarioUpdate;
        try {
            usuarioUpdate = usuarioRepository.findById(id).orElseThrow();
        } catch (NoSuchElementException exception) {
            throw new EntityNotFoundException("Usuário não encontrado: " + id, exception);
        }
        usuarioUpdate.setNome(usuarioRequestDto.nome());
        usuarioUpdate.setCpf(usuarioRequestDto.cpf());
        usuarioUpdate.setTelefone(usuarioRequestDto.telefone());
        usuarioUpdate.setEmail(usuarioRequestDto.email());
        usuarioUpdate.setSenha(passwordEncoder.encode(usuarioRequestDto.senha()));
        return usuarioRepository.save(usuarioUpdate);
    }

    @Transactional
    @CacheEvict(cacheNames = "usuarios", allEntries = true)
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

    @Transactional
    @Cacheable(cacheNames = "usuarios", key = "'todos'")
    public List<UsuarioResponseDto> findAll() { return findAllEntities().stream().map(UsuarioMapper::toResponse).toList(); }
    @Transactional public Optional<UsuarioResponseDto> findById(Long id) { return findEntityById(id).map(UsuarioMapper::toResponse); }
    @Transactional @CacheEvict(cacheNames = "usuarios", allEntries = true)
    public UsuarioResponseDto save(UsuarioRequestDto request) { return UsuarioMapper.toResponse(saveEntity(request)); }
    @Transactional @CacheEvict(cacheNames = "usuarios", allEntries = true)
    public UsuarioResponseDto update(Long id, UsuarioRequestDto request) { return UsuarioMapper.toResponse(updateEntity(id, request)); }
    @Transactional @CacheEvict(cacheNames = "usuarios", allEntries = true)
    public UsuarioResponseDto updateTipoUsuario(Long id, AlternaTipoUsuarioRequest request) { return UsuarioMapper.toResponse(updateTipoUsuarioEntity(id, request)); }
    @Transactional public List<UsuarioResponseDto> findByTipoUsuario(Long id) { return findEntitiesByTipoUsuario(id).stream().map(UsuarioMapper::toResponse).toList(); }

}
