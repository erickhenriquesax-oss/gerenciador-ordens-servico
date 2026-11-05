package com.os.gerenciador.controller;

import com.os.gerenciador.dto.request.TipoUsuarioRequestDto;
import com.os.gerenciador.dto.response.TipoUsuarioResponseDto;
import com.os.gerenciador.service.TipoUsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gerenciador/tipousuario")
public class TipoUsuarioController {

    @Autowired
    private TipoUsuarioService tipoUsuarioService;

    @GetMapping
    public ResponseEntity<List<TipoUsuarioResponseDto>> findAllTipoUsuario(){
        List<TipoUsuarioResponseDto> list = tipoUsuarioService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @PostMapping
    public ResponseEntity<TipoUsuarioResponseDto> createTipoUsuario(@Valid @RequestBody TipoUsuarioRequestDto tipoUsuarioRequestDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoUsuarioService.save(tipoUsuarioRequestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoUsuarioResponseDto> findById(@Valid @PathVariable Long id){
        return  ResponseEntity.status(HttpStatus.OK).body(tipoUsuarioService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@Valid @PathVariable Long id){
        tipoUsuarioService.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Tipo de usuário deletado");
    }
}
