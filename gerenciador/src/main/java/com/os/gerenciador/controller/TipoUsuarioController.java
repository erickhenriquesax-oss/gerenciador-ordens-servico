package com.os.gerenciador.controller;

import com.os.gerenciador.dto.request.TipoUsuarioRequestDto;
import com.os.gerenciador.dto.response.TipoUsuarioResponseDto;
import com.os.gerenciador.service.TipoUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gerenciador/tipousuario")
@Tag(name = "Papéis de usuário", description = "Gerenciamento dos papéis disponíveis")
public class TipoUsuarioController {

    @Autowired
    private TipoUsuarioService tipoUsuarioService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "Lista os papéis de usuário", description = "Lista todos os papéis cadastrados. Permitido somente para ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de papéis de usuário", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TipoUsuarioResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Operação permitida somente para ADMIN")
    })
    public ResponseEntity<List<TipoUsuarioResponseDto>> findAllTipoUsuario(){
        List<TipoUsuarioResponseDto> list = tipoUsuarioService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Cria papel de usuário", description = "Cadastra um novo papel de usuário. Permitido somente para ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Papel de usuário criado", content = @Content(schema = @Schema(implementation = TipoUsuarioResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Corpo inválido ou descrição vazia"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Operação permitida somente para ADMIN")
    })
    public ResponseEntity<TipoUsuarioResponseDto> createTipoUsuario(@Valid @RequestBody TipoUsuarioRequestDto tipoUsuarioRequestDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoUsuarioService.save(tipoUsuarioRequestDto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Busca papel de usuário por ID", description = "Busca um papel de usuário pelo ID. Permitido somente para ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Papel de usuário encontrado", content = @Content(schema = @Schema(implementation = TipoUsuarioResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Operação permitida somente para ADMIN"),
            @ApiResponse(responseCode = "404", description = "Papel de usuário inexistente")
    })
    public ResponseEntity<TipoUsuarioResponseDto> findById(@Valid @PathVariable Long id){
        return  ResponseEntity.status(HttpStatus.OK).body(tipoUsuarioService.findById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui papel de usuário", description = "Exclui o papel de usuário pelo ID. Permitido somente para ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mensagem de confirmação de exclusão", content = @Content(schema = @Schema(type = "string", example = "Tipo de usuário deletado"))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Operação permitida somente para ADMIN"),
            @ApiResponse(responseCode = "404", description = "Papel de usuário não encontrado"),
            @ApiResponse(responseCode = "409", description = "Papel ainda está associado a usuários")
    })
    public ResponseEntity<String> deleteById(@Valid @PathVariable Long id){
        tipoUsuarioService.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Tipo de usuário deletado");
    }
}
