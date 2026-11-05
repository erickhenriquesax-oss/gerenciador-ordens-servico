package com.os.gerenciador.controller;

import com.os.gerenciador.dto.request.OrdemServicoExecutorRequest;
import com.os.gerenciador.dto.request.OrdemServicoRequestDto;
import com.os.gerenciador.dto.response.OrdemServicoResponseDto;
import com.os.gerenciador.service.OrdemServicoService;
import jakarta.validation.Valid;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gerenciador/os")
public class OrdemServicoController {

    @Autowired
    private OrdemServicoService ordemServicoService;

    @GetMapping
    public ResponseEntity<List<OrdemServicoResponseDto>> getAllOrdemServicos(){
        return new ResponseEntity<>(ordemServicoService.getAllOrdemServicos(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdemServicoResponseDto> getOrdemServicoById(@PathVariable Long id){
        return ordemServicoService.getOrdemServicoById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<OrdemServicoResponseDto>> getOrdemServicoByUser(@PathVariable Long id){
        return ResponseEntity.ok(ordemServicoService.getOrdemServicosByUsuario(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrdemServicoById(@PathVariable Long id){
        ordemServicoService.deletarOrdemServico(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping
    public ResponseEntity<OrdemServicoResponseDto> saveOrdemServico(@Valid @RequestBody OrdemServicoRequestDto ordemServicoRequestDto){
        OrdemServicoResponseDto ordemServicoModel = ordemServicoService.save(ordemServicoRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ordemServicoModel);
    }

    @PutMapping("/{idOrdem}")
    public ResponseEntity<OrdemServicoResponseDto> atribuirExecutorOrdemServico(@Valid @PathVariable Long idOrdem, @Valid @RequestBody OrdemServicoExecutorRequest  ordemServicoExecutorRequest){
        OrdemServicoResponseDto ordem = ordemServicoService.atribuirExecutor(idOrdem, ordemServicoExecutorRequest);
        return ResponseEntity.status(HttpStatus.OK).body(ordem);
    }

    @PutMapping("/finalizar/{id}")
    public ResponseEntity<OrdemServicoResponseDto> finalizarOrdemServico(@PathVariable Long id){
        OrdemServicoResponseDto ordem = ordemServicoService.finalizarOrdemServico(id);
        return ResponseEntity.status(HttpStatus.OK).body(ordem);
    }


}
