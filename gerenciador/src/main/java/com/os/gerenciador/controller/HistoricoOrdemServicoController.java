package com.os.gerenciador.controller;

import com.os.gerenciador.dto.request.HistoricoOrdemServicoRequest;
import com.os.gerenciador.model.HistoricoOrdemServicoModel;
import com.os.gerenciador.service.HistoricoOrdemServicoService;
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
    public ResponseEntity<List<HistoricoOrdemServicoModel>> findAll() {
        return new ResponseEntity<>(historicoOrdemServicoService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public HistoricoOrdemServicoModel findById(@PathVariable Long id) {
        return historicoOrdemServicoService.findById(id);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<HistoricoOrdemServicoModel>> findByUser(@PathVariable Long id) {
        return new ResponseEntity<>(historicoOrdemServicoService.findAllByUsuarioHistorico(id), HttpStatus.OK);
    }

    @GetMapping("/ordem/{id}")
    public ResponseEntity<List<HistoricoOrdemServicoModel>> findByOrdem(@PathVariable Long id) {
        return new ResponseEntity<>(historicoOrdemServicoService.findAllByOrdemServico(id), HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<HistoricoOrdemServicoModel> save(@RequestBody HistoricoOrdemServicoRequest historicoOrdemServicoRequest) {
        HistoricoOrdemServicoModel  historicoOrdemServicoModel = historicoOrdemServicoService.save(historicoOrdemServicoRequest);
        return new ResponseEntity<>(historicoOrdemServicoModel, HttpStatus.CREATED);
    }



}
