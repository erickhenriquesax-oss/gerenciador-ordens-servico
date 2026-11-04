package com.os.gerenciador.controller;

import com.os.gerenciador.dto.request.AlternaTipoUsuarioRequest;
import com.os.gerenciador.dto.request.UsuarioRequestDto;
import com.os.gerenciador.dto.response.UsuarioResponseDto;
import com.os.gerenciador.model.UsuarioModel;
import com.os.gerenciador.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/gerenciador/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDto>> findAll() {
        List<UsuarioResponseDto> list = usuarioService.findAll().stream().map(UsuarioResponseDto::new).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> findById(@Valid @PathVariable Long id) {
        Optional<UsuarioModel> usuario = usuarioService.findById(id);
        if (usuario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new UsuarioResponseDto(usuario.get()));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDto> create(@Valid @RequestBody UsuarioRequestDto usuarioRequestDto) {
        UsuarioModel usuario = usuarioService.save(usuarioRequestDto);
        return ResponseEntity.ok(new UsuarioResponseDto(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> update(@Valid @PathVariable Long id, @Valid @RequestBody UsuarioRequestDto usuarioRequestDto) {
        UsuarioModel usuario = usuarioService.update(id, usuarioRequestDto);
        return ResponseEntity.ok(new UsuarioResponseDto(usuario));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> updatetipousuario(@PathVariable Long id, @Valid @RequestBody AlternaTipoUsuarioRequest alternaTipoUsuarioRequest) {
        UsuarioModel usuario = usuarioService.updateTipoUsuario(id, alternaTipoUsuarioRequest);
        return ResponseEntity.ok(new UsuarioResponseDto(usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> deleteById(@Valid @PathVariable Long id){
        usuarioService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/tipo_usuario/{idTipoUsuario}")
    public ResponseEntity<List<UsuarioResponseDto>> findAllByTipoUsuario(@Valid @PathVariable Long idTipoUsuario) {
        List<UsuarioResponseDto> list = usuarioService.findByTipoUsuario(idTipoUsuario).stream().map(UsuarioResponseDto::new).toList();
        return ResponseEntity.ok(list);
    }


}
