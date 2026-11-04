package com.os.gerenciador.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_usuario")
@Getter
@Setter
public class UsuarioModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String nome;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false)
    private String cpf;

    @Column(nullable = false)
    private String telefone;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JoinColumn(name = "id_tipo_usuario")
    @ManyToOne(fetch = FetchType.LAZY)
    private TipoUsuarioModel tipoUsuarioModel;

    @OneToMany(mappedBy = "usuarioSolicitante")
    private List<OrdemServicoModel> ordensComoSolicitante = new ArrayList<>();

    @OneToMany(mappedBy = "usuarioExecutor")
    private List<OrdemServicoModel> ordensComoExecutor = new ArrayList<>();

    @OneToMany(mappedBy = "usuarioHistorico")
    private List<HistoricoOrdemServicoModel> usuarioHistorico = new ArrayList<>();

}
