package com.os.gerenciador.controller;

import com.os.gerenciador.dto.request.HistoricoOrdemServicoRequest;
import com.os.gerenciador.dto.response.HistoricoOrdemServicoResponseDto;
import com.os.gerenciador.config.JWTUserData;
import com.os.gerenciador.service.HistoricoOrdemServicoService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("gerenciador/historico")
@Tag(name = "Histórico", description = "Consulta e manutenção do histórico das ordens de serviço")
public class HistoricoOrdemServicoController {

    @Autowired
    private HistoricoOrdemServicoService historicoOrdemServicoService;

    @PreAuthorize("hasAnyRole('SOLICITANTE', 'EXECUTOR', 'ADMIN')")
    @GetMapping
    @Operation(summary = "Busca históricos visíveis ao usuário", description = "ADMIN consulta todos os históricos. SOLICITANTE e EXECUTOR consultam históricos que registraram ou pertencem a ordens em que são solicitantes ou executores.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de históricos visíveis", content = @Content(array = @ArraySchema(schema = @Schema(implementation = HistoricoOrdemServicoResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Papel sem permissão para consultar históricos")
    })
    public ResponseEntity<List<HistoricoOrdemServicoResponseDto>> findAll(@AuthenticationPrincipal JWTUserData principal) {
        return new ResponseEntity<>(historicoOrdemServicoService.findAll(principal.userId(), principal.isAdmin()), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SOLICITANTE', 'EXECUTOR', 'ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Busca histórico por ID", description = "ADMIN pode consultar qualquer histórico. Os demais perfis podem consultar históricos que registraram ou de ordens em que participam como solicitante ou executor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico encontrado", content = @Content(schema = @Schema(implementation = HistoricoOrdemServicoResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Usuário sem acesso a este histórico"),
            @ApiResponse(responseCode = "404", description = "Histórico inexistente")
    })
    public HistoricoOrdemServicoResponseDto findById(@PathVariable Long id, @AuthenticationPrincipal JWTUserData principal) {
        return historicoOrdemServicoService.findById(id, principal.userId(), principal.isAdmin());
    }

    @PreAuthorize("hasRole('ADMIN') or #p0 == authentication.principal.userId")
    @GetMapping("/user/{id}")
    @Operation(summary = "Busca históricos pelo usuário", description = "Lista os históricos registrados pelo usuário informado. ADMIN pode consultar qualquer usuário; os demais podem informar apenas o próprio ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de históricos registrados pelo usuário", content = @Content(array = @ArraySchema(schema = @Schema(implementation = HistoricoOrdemServicoResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para consultar esse ID"),
            @ApiResponse(responseCode = "404", description = "Usuário inexistente")
    })
    public ResponseEntity<List<HistoricoOrdemServicoResponseDto>> findByUser(@PathVariable Long id, @AuthenticationPrincipal JWTUserData principal) {
        return new ResponseEntity<>(historicoOrdemServicoService.findAllByUsuario(id, principal.userId(), principal.isAdmin()), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SOLICITANTE', 'EXECUTOR', 'ADMIN')")
    @GetMapping("/ordem/{id}")
    @Operation(summary = "Busca históricos de uma Ordem de Serviço", description = "Lista os históricos da ordem. ADMIN tem acesso a qualquer ordem; SOLICITANTE e EXECUTOR precisam participar da ordem.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de históricos da ordem", content = @Content(array = @ArraySchema(schema = @Schema(implementation = HistoricoOrdemServicoResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Usuário não participa da ordem"),
            @ApiResponse(responseCode = "404", description = "Ordem inexistente")
    })
    public ResponseEntity<List<HistoricoOrdemServicoResponseDto>> findByOrdem(@PathVariable Long id, @AuthenticationPrincipal JWTUserData principal) {
        return new ResponseEntity<>(historicoOrdemServicoService.findAllByOrdem(id, principal.userId(), principal.isAdmin()), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SOLICITANTE', 'EXECUTOR', 'ADMIN')")
    @PostMapping
    @Operation(summary = "Salva um novo histórico", description = "Registra uma descrição na ordem informada. ADMIN pode registrar por qualquer usuário; nos demais perfis, o ID do usuário deve ser o autenticado e ele deve participar da ordem.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Histórico registrado", content = @Content(schema = @Schema(implementation = HistoricoOrdemServicoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Corpo inválido ou campos obrigatórios ausentes"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para registrar este histórico"),
            @ApiResponse(responseCode = "404", description = "Usuário ou ordem inexistente")
    })
    public ResponseEntity<HistoricoOrdemServicoResponseDto> save(@Valid @RequestBody HistoricoOrdemServicoRequest historicoOrdemServicoRequest,
                                                                  @AuthenticationPrincipal JWTUserData principal) {
        HistoricoOrdemServicoResponseDto historicoOrdemServicoModel = historicoOrdemServicoService.save(
                historicoOrdemServicoRequest, principal.userId(), principal.isAdmin());
        return new ResponseEntity<>(historicoOrdemServicoModel, HttpStatus.CREATED);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Altera um histórico", description = "Altera o usuário, a ordem ou a descrição de um registro histórico. Permitido somente para ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico atualizado", content = @Content(schema = @Schema(implementation = HistoricoOrdemServicoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Corpo inválido ou campos obrigatórios ausentes"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Operação permitida somente para ADMIN"),
            @ApiResponse(responseCode = "404", description = "Histórico, usuário ou ordem inexistente")
    })
    public ResponseEntity<HistoricoOrdemServicoResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody HistoricoOrdemServicoRequest request) {
        return ResponseEntity.ok(historicoOrdemServicoService.update(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um histórico", description = "Exclui um registro do histórico pelo ID. Permitido somente para ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Histórico excluído"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Operação permitida somente para ADMIN"),
            @ApiResponse(responseCode = "404", description = "Histórico inexistente")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        historicoOrdemServicoService.delete(id);
        return ResponseEntity.noContent().build();
    }



}
