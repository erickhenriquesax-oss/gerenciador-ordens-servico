package com.os.gerenciador.controller;

import com.os.gerenciador.model.OrdemServicoModel;
import com.os.gerenciador.service.OrdemServicoService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/gerenciador/os")
public class OrdemServicoController {

    @Autowired
    private OrdemServicoService ordemServicoService;

    @GetMapping
    public ResponseEntity<List<OrdemServicoModel>> getAllOrdemServicos(){
        return new ResponseEntity<>(ordemServicoService.getAllOrdemServicos(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdemServicoModel> getOrdemServicoById(@PathVariable Long id){
        return ordemServicoService.getOrdemServicoById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<OrdemServicoModel>> getOrdemServicoByUser(@PathVariable Long id){
        return ResponseEntity.ok(ordemServicoService.getOrdemServicosByUsuario(id));
    }
}
