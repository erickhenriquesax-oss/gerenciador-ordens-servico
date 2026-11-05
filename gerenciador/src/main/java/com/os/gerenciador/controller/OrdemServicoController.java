package com.os.gerenciador.controller;

import com.os.gerenciador.dto.request.OrdemServicoExecutorRequest;
import com.os.gerenciador.dto.request.OrdemServicoRequestDto;
import com.os.gerenciador.dto.response.OrdemServicoResponseDto;
import com.os.gerenciador.config.JWTUserData;
import com.os.gerenciador.service.OrdemServicoService;
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
public class OrdemServicoController {

    @Autowired
    private OrdemServicoService ordemServicoService;

    @PreAuthorize("hasAnyRole('SOLICITANTE', 'EXECUTOR', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<OrdemServicoResponseDto>> getAllOrdemServicos(@AuthenticationPrincipal JWTUserData principal){
        return new ResponseEntity<>(ordemServicoService.getAllOrdemServicos(principal.userId(), principal.isAdmin(), principal.isExecutor()), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SOLICITANTE', 'EXECUTOR', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<OrdemServicoResponseDto> getOrdemServicoById(@PathVariable Long id, @AuthenticationPrincipal JWTUserData principal){
        return ordemServicoService.getOrdemServicoById(id, principal.userId(), principal.isAdmin(), principal.isExecutor()).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN') or #p0 == authentication.principal.userId")
    @GetMapping("/user/{id}")
    public ResponseEntity<List<OrdemServicoResponseDto>> getOrdemServicoByUser(@PathVariable Long id){
        return ResponseEntity.ok(ordemServicoService.getOrdemServicoByUsuario(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrdemServicoById(@PathVariable Long id){
        ordemServicoService.deletarOrdemServico(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('SOLICITANTE') and authentication.principal.userId == #p0.usuarioSolicitante)")
    @PostMapping
    public ResponseEntity<OrdemServicoResponseDto> saveOrdemServico(@Valid @RequestBody OrdemServicoRequestDto ordemServicoRequestDto){
        OrdemServicoResponseDto ordemServicoModel = ordemServicoService.save(ordemServicoRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ordemServicoModel);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXECUTOR') and authentication.principal.userId == #p1.idExecutor)")
    @PutMapping("/{idOrdem}")
    public ResponseEntity<OrdemServicoResponseDto> atribuirExecutorOrdemServico(@Valid @PathVariable Long idOrdem, @Valid @RequestBody OrdemServicoExecutorRequest  ordemServicoExecutorRequest){
        OrdemServicoResponseDto ordem = ordemServicoService.atribuirExecutor(idOrdem, ordemServicoExecutorRequest);
        return ResponseEntity.status(HttpStatus.OK).body(ordem);
    }

    @PreAuthorize("hasAnyRole('EXECUTOR', 'ADMIN')")
    @PutMapping("/finalizar/{id}")
    public ResponseEntity<OrdemServicoResponseDto> finalizarOrdemServico(@PathVariable Long id, @AuthenticationPrincipal JWTUserData principal){
        OrdemServicoResponseDto ordem = ordemServicoService.finalizarOrdemServico(id, principal.userId(), principal.isAdmin());
        return ResponseEntity.status(HttpStatus.OK).body(ordem);
    }


}
