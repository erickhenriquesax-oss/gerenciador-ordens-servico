package com.os.gerenciador.controller;

import com.os.gerenciador.dto.request.HistoricoOrdemServicoRequest;
import com.os.gerenciador.dto.response.HistoricoOrdemServicoResponseDto;
import com.os.gerenciador.service.HistoricoOrdemServicoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("gerenciador/historico")
public class HistoricoOrdemServicoController {

    @Autowired
    private HistoricoOrdemServicoService historicoOrdemServicoService;

    @GetMapping
    public ResponseEntity<List<HistoricoOrdemServicoResponseDto>> findAll() {
        return new ResponseEntity<>(historicoOrdemServicoService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public HistoricoOrdemServicoResponseDto findById(@PathVariable Long id) {
        return historicoOrdemServicoService.findById(id);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<HistoricoOrdemServicoResponseDto>> findByUser(@PathVariable Long id) {
        return new ResponseEntity<>(historicoOrdemServicoService.findAllByUsuarioHistorico(id), HttpStatus.OK);
    }

    @GetMapping("/ordem/{id}")
    public ResponseEntity<List<HistoricoOrdemServicoResponseDto>> findByOrdem(@PathVariable Long id) {
        return new ResponseEntity<>(historicoOrdemServicoService.findAllByOrdemServico(id), HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<HistoricoOrdemServicoResponseDto> save(@RequestBody HistoricoOrdemServicoRequest historicoOrdemServicoRequest) {
        HistoricoOrdemServicoResponseDto  historicoOrdemServicoModel = historicoOrdemServicoService.save(historicoOrdemServicoRequest);
        return new ResponseEntity<>(historicoOrdemServicoModel, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HistoricoOrdemServicoResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody HistoricoOrdemServicoRequest request) {
        return ResponseEntity.ok(historicoOrdemServicoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        historicoOrdemServicoService.delete(id);
        return ResponseEntity.noContent().build();
    }



}
