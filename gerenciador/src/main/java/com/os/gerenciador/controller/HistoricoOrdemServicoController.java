package com.os.gerenciador.controller;

import com.os.gerenciador.dto.request.HistoricoOrdemServicoRequest;
import com.os.gerenciador.dto.response.HistoricoOrdemServicoResponseDto;
import com.os.gerenciador.config.JWTUserData;
import com.os.gerenciador.service.HistoricoOrdemServicoService;
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
public class HistoricoOrdemServicoController {

    @Autowired
    private HistoricoOrdemServicoService historicoOrdemServicoService;

    @PreAuthorize("hasAnyRole('SOLICITANTE', 'EXECUTOR', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<HistoricoOrdemServicoResponseDto>> findAll(@AuthenticationPrincipal JWTUserData principal) {
        return new ResponseEntity<>(historicoOrdemServicoService.findAll(principal.userId(), principal.isAdmin()), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SOLICITANTE', 'EXECUTOR', 'ADMIN')")
    @GetMapping("/{id}")
    public HistoricoOrdemServicoResponseDto findById(@PathVariable Long id, @AuthenticationPrincipal JWTUserData principal) {
        return historicoOrdemServicoService.findById(id, principal.userId(), principal.isAdmin());
    }

    @PreAuthorize("hasRole('ADMIN') or #p0 == authentication.principal.userId")
    @GetMapping("/user/{id}")
    public ResponseEntity<List<HistoricoOrdemServicoResponseDto>> findByUser(@PathVariable Long id, @AuthenticationPrincipal JWTUserData principal) {
        return new ResponseEntity<>(historicoOrdemServicoService.findAllByUsuario(id, principal.userId(), principal.isAdmin()), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SOLICITANTE', 'EXECUTOR', 'ADMIN')")
    @GetMapping("/ordem/{id}")
    public ResponseEntity<List<HistoricoOrdemServicoResponseDto>> findByOrdem(@PathVariable Long id, @AuthenticationPrincipal JWTUserData principal) {
        return new ResponseEntity<>(historicoOrdemServicoService.findAllByOrdem(id, principal.userId(), principal.isAdmin()), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SOLICITANTE', 'EXECUTOR', 'ADMIN')")
    @PostMapping
    public ResponseEntity<HistoricoOrdemServicoResponseDto> save(@Valid @RequestBody HistoricoOrdemServicoRequest historicoOrdemServicoRequest,
                                                                  @AuthenticationPrincipal JWTUserData principal) {
        HistoricoOrdemServicoResponseDto historicoOrdemServicoModel = historicoOrdemServicoService.save(
                historicoOrdemServicoRequest, principal.userId(), principal.isAdmin());
        return new ResponseEntity<>(historicoOrdemServicoModel, HttpStatus.CREATED);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<HistoricoOrdemServicoResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody HistoricoOrdemServicoRequest request) {
        return ResponseEntity.ok(historicoOrdemServicoService.update(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        historicoOrdemServicoService.delete(id);
        return ResponseEntity.noContent().build();
    }



}
