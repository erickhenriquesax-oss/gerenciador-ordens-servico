
package com.os.gerenciador.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.os.gerenciador.Enums.UserRoles;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "tb_tipo_usuario")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoUsuarioModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String descricao;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToMany(mappedBy = "tipoUsuarioModel", fetch = FetchType.LAZY)
    @Default
    private Set<UsuarioModel> usuarios = new HashSet<>();

    public UserRoles toUserRole() {
        return UserRoles.fromCodigo(this.id);
    }
}
