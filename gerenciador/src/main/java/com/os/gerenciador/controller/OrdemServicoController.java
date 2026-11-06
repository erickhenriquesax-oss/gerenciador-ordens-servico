package com.os.gerenciador.controller;

import com.os.gerenciador.dto.request.OrdemServicoExecutorRequest;
import com.os.gerenciador.dto.request.OrdemServicoRequestDto;
import com.os.gerenciador.dto.response.OrdemServicoResponseDto;
import com.os.gerenciador.config.JWTUserData;
import com.os.gerenciador.service.OrdemServicoService;
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
@RequestMapping("/gerenciador/os")
@Tag(name = "Ordem de Serviço", description = "Gerenciador de Ordens de Serviço")
public class OrdemServicoController {

    @Autowired
    private OrdemServicoService ordemServicoService;

    @PreAuthorize("hasAnyRole('SOLICITANTE', 'EXECUTOR', 'ADMIN')")
    @GetMapping
    @Operation(summary = "Busca ordens de serviço visíveis ao usuário", description = "ADMIN consulta todas as ordens. EXECUTOR consulta ordens abertas e ordens atribuídas a ele. SOLICITANTE consulta somente as ordens em que é o solicitante.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de ordens visíveis ao usuário", content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrdemServicoResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Papel sem permissão para consultar ordens")
    })
    public ResponseEntity<List<OrdemServicoResponseDto>> getAllOrdemServicos(@AuthenticationPrincipal JWTUserData principal){
        return new ResponseEntity<>(ordemServicoService.getAllOrdemServicos(principal.userId(), principal.isAdmin(), principal.isExecutor()), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SOLICITANTE', 'EXECUTOR', 'ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Busca Ordem de Serviço por ID", description = "ADMIN pode consultar qualquer ordem. EXECUTOR pode consultar ordens abertas e as atribuídas a ele. SOLICITANTE pode consultar apenas as próprias ordens.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ordem encontrada e visível ao usuário", content = @Content(schema = @Schema(implementation = OrdemServicoResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Papel sem permissão para consultar ordens"),
            @ApiResponse(responseCode = "404", description = "Ordem inexistente ou fora do escopo do usuário")
    })
    public ResponseEntity<OrdemServicoResponseDto> getOrdemServicoById(@PathVariable Long id, @AuthenticationPrincipal JWTUserData principal){
        return ordemServicoService.getOrdemServicoById(id, principal.userId(), principal.isAdmin(), principal.isExecutor()).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN') or #p0 == authentication.principal.userId")
    @GetMapping("/user/{id}")
    @Operation(summary = "Busca ordens de serviço visíveis ao usuário", description = "Lista as ordens solicitadas ou executadas pelo usuário informado. ADMIN pode consultar qualquer usuário; os demais podem informar apenas o próprio ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de ordens solicitadas ou executadas pelo usuário", content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrdemServicoResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para consultar esse ID")
    })
    public ResponseEntity<List<OrdemServicoResponseDto>> getOrdemServicoByUser(@PathVariable Long id){
        return ResponseEntity.ok(ordemServicoService.getOrdemServicoByUsuario(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Deleta ordem de serviço", description = "Deleta uma ordem de serviço através do ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ordem excluída"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Operação permitida somente para ADMIN"),
            @ApiResponse(responseCode = "404", description = "Ordem não encontrada")
    })
    public ResponseEntity<Void> deleteOrdemServicoById(@PathVariable Long id){
        ordemServicoService.deletarOrdemServico(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('SOLICITANTE') and authentication.principal.userId == #p0.usuarioSolicitante)")
    @PostMapping
    @Operation(summary = "Cria ordem de serviço", description = "Cria uma nova ordem de serviço. Disponível para ADMIN e SOLICITANTE; solicitantes só podem criar ordens para si.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ordem criada", content = @Content(schema = @Schema(implementation = OrdemServicoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Corpo inválido ou dados rejeitados"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Usuário não pode criar ordem para o solicitante informado"),
            @ApiResponse(responseCode = "404", description = "Solicitante inexistente"),
            @ApiResponse(responseCode = "409", description = "Conflito com restrição de dados")
    })
    public ResponseEntity<OrdemServicoResponseDto> saveOrdemServico(@Valid @RequestBody OrdemServicoRequestDto ordemServicoRequestDto){
        OrdemServicoResponseDto ordemServicoModel = ordemServicoService.save(ordemServicoRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ordemServicoModel);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXECUTOR') and authentication.principal.userId == #p1.idExecutor)")
    @PutMapping("/{idOrdem}")
    @Operation(summary = "Atribuir executor para Ordem de Serviço", description = "Atribui um executor a uma ordem ainda aberta e altera o status para EM_PROCESSO. ADMIN pode indicar qualquer executor; EXECUTOR só pode indicar a si mesmo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Executor atribuído e ordem alterada para EM_PROCESSO", content = @Content(schema = @Schema(implementation = OrdemServicoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Corpo inválido ou usuário não tem papel EXECUTOR"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Executor só pode atribuir a ordem a si mesmo"),
            @ApiResponse(responseCode = "404", description = "Ordem ou executor inexistente"),
            @ApiResponse(responseCode = "409", description = "A ordem não está ABERTA")
    })
    public ResponseEntity<OrdemServicoResponseDto> atribuirExecutorOrdemServico(@Valid @PathVariable Long idOrdem, @Valid @RequestBody OrdemServicoExecutorRequest  ordemServicoExecutorRequest){
        OrdemServicoResponseDto ordem = ordemServicoService.atribuirExecutor(idOrdem, ordemServicoExecutorRequest);
        return ResponseEntity.status(HttpStatus.OK).body(ordem);
    }

    @PreAuthorize("hasAnyRole('EXECUTOR', 'ADMIN')")
    @PutMapping("/finalizar/{id}")
    @Operation(summary = "Finalizar Ordem de Serviço", description = "Encerra uma ordem em processo. ADMIN ou o executor atribuído podem finalizar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ordem encerrada", content = @Content(schema = @Schema(implementation = OrdemServicoResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Somente ADMIN ou o executor atribuído pode finalizar"),
            @ApiResponse(responseCode = "404", description = "Ordem inexistente"),
            @ApiResponse(responseCode = "409", description = "A ordem não está EM_PROCESSO")
    })
    public ResponseEntity<OrdemServicoResponseDto> finalizarOrdemServico(@PathVariable Long id, @AuthenticationPrincipal JWTUserData principal){
        OrdemServicoResponseDto ordem = ordemServicoService.finalizarOrdemServico(id, principal.userId(), principal.isAdmin());
        return ResponseEntity.status(HttpStatus.OK).body(ordem);
    }


}
