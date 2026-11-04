package com.os.gerenciador.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.os.gerenciador.Enums.StatusOS;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_ordem")
@Getter
@Setter
public class OrdemServicoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOS status;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_solicitante", nullable = false)
    private UsuarioModel usuarioSolicitante;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_executor")
    private UsuarioModel usuarioExecutor;

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL)
    private List<HistoricoOrdemServicoModel> historicos = new ArrayList<>();




}
