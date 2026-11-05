package com.os.gerenciador.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_historico_ordem_servico")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoOrdemServicoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataHistorico;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_historico", nullable = false)
    private UsuarioModel usuarioHistorico;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ordem_servico", nullable = false)
    private OrdemServicoModel ordemServico;

    @Column(nullable = false)
    private String descricao;
}
