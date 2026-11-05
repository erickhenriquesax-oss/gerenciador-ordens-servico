package com.os.gerenciador.controller;

import com.os.gerenciador.dto.request.OrdemServicoExecutorRequest;
import com.os.gerenciador.dto.request.OrdemServicoRequestDto;
import com.os.gerenciador.model.OrdemServicoModel;
import com.os.gerenciador.service.OrdemServicoService;
import jakarta.validation.Valid;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping("/{id}")
    public ResponseEntity<OrdemServicoModel> deleteOrdemServicoById(@PathVariable Long id){
        ordemServicoService.deletarOrdemServico(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping
    public ResponseEntity<OrdemServicoModel> saveOrdemServico(@Valid @RequestBody OrdemServicoRequestDto ordemServicoRequestDto){
        OrdemServicoModel ordemServicoModel = ordemServicoService.save(ordemServicoRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ordemServicoModel);
    }

    @PutMapping("/{idOrdem}")
    public ResponseEntity<OrdemServicoModel> atribuirExecutorOrdemServico(@Valid @PathVariable Long idOrdem, @Valid @RequestBody OrdemServicoExecutorRequest  ordemServicoExecutorRequest){
        OrdemServicoModel ordem = ordemServicoService.atribuirExecutor(idOrdem, ordemServicoExecutorRequest);
        return ResponseEntity.status(HttpStatus.OK).body(ordem);
    }

    @PutMapping("/finalizar/{id}")
    public ResponseEntity<OrdemServicoModel> finalizarOrdemServico(@PathVariable Long id){
        OrdemServicoModel ordem = ordemServicoService.finalizarOrdemServico(id);
        return ResponseEntity.status(HttpStatus.OK).body(ordem);
    }


}
