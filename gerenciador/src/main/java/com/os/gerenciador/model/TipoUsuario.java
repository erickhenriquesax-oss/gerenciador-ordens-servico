package com.os.gerenciador.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tb_tipo_usuario")
public class TipoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String descricao;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToMany(mappedBy = "tipo_usuario", fetch = FetchType.LAZY)
    private Set<UsuarioModel> usuarios = new HashSet<UsuarioModel>();

    public UserRoles toUserRole() {
        return UserRoles.fromCodigo(this.idTipoUsuario);
    }
}
