package com.os.gerenciador.controller;

import com.os.gerenciador.dto.request.AlternaTipoUsuarioRequest;
import com.os.gerenciador.dto.request.UsuarioRequestDto;
import com.os.gerenciador.dto.response.UsuarioResponseDto;
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
        List<UsuarioResponseDto> list = usuarioService.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> findById(@Valid @PathVariable Long id) {
        return usuarioService.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDto> create(@Valid @RequestBody UsuarioRequestDto usuarioRequestDto) {
        return ResponseEntity.ok(usuarioService.save(usuarioRequestDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> update(@Valid @PathVariable Long id, @Valid @RequestBody UsuarioRequestDto usuarioRequestDto) {
        return ResponseEntity.ok(usuarioService.update(id, usuarioRequestDto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> updatetipousuario(@PathVariable Long id, @Valid @RequestBody AlternaTipoUsuarioRequest alternaTipoUsuarioRequest) {
        return ResponseEntity.ok(usuarioService.updateTipoUsuario(id, alternaTipoUsuarioRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> deleteById(@Valid @PathVariable Long id){
        usuarioService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/tipo_usuario/{idTipoUsuario}")
    public ResponseEntity<List<UsuarioResponseDto>> findAllByTipoUsuario(@Valid @PathVariable Long idTipoUsuario) {
        List<UsuarioResponseDto> list = usuarioService.findByTipoUsuario(idTipoUsuario);
        return ResponseEntity.ok(list);
    }


}
