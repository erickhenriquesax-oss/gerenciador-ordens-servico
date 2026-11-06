package com.os.gerenciador.controller;

import com.os.gerenciador.dto.request.AlternaTipoUsuarioRequest;
import com.os.gerenciador.dto.request.UsuarioRequestDto;
import com.os.gerenciador.dto.response.UsuarioResponseDto;
import com.os.gerenciador.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gerenciador/usuario")
@Tag(name = "Usuários", description = "Gerenciador de usuários")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "Busca todos os usuários", description = "Lista todos os usuários cadastrados. Permitido somente para ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuários", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UsuarioResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Operação permitida somente para ADMIN")
    })
    public ResponseEntity<List<UsuarioResponseDto>> findAll() {
        List<UsuarioResponseDto> list = usuarioService.findAll();
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EXECUTOR') or authentication.principal.userId == #p0")
    @GetMapping("/{id}")
    @Operation(summary = "Busca usuário por ID", description = "Busca os dados do usuário pelo ID. Permitido para ADMIN e EXECUTOR, ou para o próprio usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado", content = @Content(schema = @Schema(implementation = UsuarioResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para consultar esse ID"),
            @ApiResponse(responseCode = "404", description = "Usuário inexistente")
    })
    public ResponseEntity<UsuarioResponseDto> findById(@Valid @PathVariable Long id) {
        return usuarioService.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @SecurityRequirements
    @Operation(summary = "Cadastrar usuário", description = "Cadastra um novo usuário solicitante. Não exige autenticação; o e-mail deve ser único e o ID de papel informado em tipoUsuario deve corresponder a SOLICITANTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado", content = @Content(schema = @Schema(implementation = UsuarioResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Corpo inválido, falha de validação ou ID de papel sem mapeamento"),
            @ApiResponse(responseCode = "403", description = "Cadastro público aceita somente usuários SOLICITANTE"),
            @ApiResponse(responseCode = "404", description = "Tipo de usuário inexistente"),
            @ApiResponse(responseCode = "409", description = "E-mail ou outro dado já cadastrado")
    })
    public ResponseEntity<UsuarioResponseDto> create(@Valid @RequestBody UsuarioRequestDto usuarioRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.save(usuarioRequestDto));
    }

    @PreAuthorize("hasRole('ADMIN') or authentication.principal.userId == #p0")
    @PutMapping("/{id}")
    @Operation(summary = "Atualiza dados do usuário", description = "Atualiza nome, e-mail, senha, CPF, telefone e data de nascimento. Permitido para o próprio usuário ou para ADMIN. A alteração do papel é feita separadamente em PATCH /gerenciador/usuario/{id}.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado", content = @Content(schema = @Schema(implementation = UsuarioResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Corpo inválido ou falha de validação"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Permitido somente para o próprio usuário ou ADMIN"),
            @ApiResponse(responseCode = "404", description = "Usuário inexistente"),
            @ApiResponse(responseCode = "409", description = "Conflito com registro existente, como e-mail duplicado")
    })
    public ResponseEntity<UsuarioResponseDto> update(@Valid @PathVariable Long id, @Valid @RequestBody UsuarioRequestDto usuarioRequestDto) {
        return ResponseEntity.ok(usuarioService.update(id, usuarioRequestDto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    @Operation(summary = "Atualiza o papel do usuário", description = "Associa ao usuário o papel identificado pelo campo id. Permitido somente para ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Papel atualizado", content = @Content(schema = @Schema(implementation = UsuarioResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Corpo inválido ou ID do papel ausente"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Operação permitida somente para ADMIN"),
            @ApiResponse(responseCode = "404", description = "Usuário ou papel inexistente")
    })
    public ResponseEntity<UsuarioResponseDto> updatetipousuario(@PathVariable Long id, @Valid @RequestBody AlternaTipoUsuarioRequest alternaTipoUsuarioRequest) {
        return ResponseEntity.ok(usuarioService.updateTipoUsuario(id, alternaTipoUsuarioRequest));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um usuário", description = "Exclui o usuário pelo ID. Permitido somente para ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário excluído"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Operação permitida somente para ADMIN"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "409", description = "Usuário ainda está associado a ordens ou históricos")
    })
    public ResponseEntity<Void> deleteById(@Valid @PathVariable Long id){
        usuarioService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/tipo_usuario/{idTipoUsuario}")
    @Operation(summary = "Busca usuários pelo papel", description = "Lista os usuários associados ao papel indicado por idTipoUsuario. Permitido somente para ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuários associados ao papel", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UsuarioResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Operação permitida somente para ADMIN")
    })
    public ResponseEntity<List<UsuarioResponseDto>> findAllByTipoUsuario(@Valid @PathVariable Long idTipoUsuario) {
        List<UsuarioResponseDto> list = usuarioService.findByTipoUsuario(idTipoUsuario);
        return ResponseEntity.ok(list);
    }


}
