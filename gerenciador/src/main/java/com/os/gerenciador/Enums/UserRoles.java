
package com.os.gerenciador.Enums;

public enum UserRoles {

    EXECUTOR(1L, "ROLE_EXECUTOR"),
    SOLICITANTE(2L, "ROLE_SOLICITANTE");

    private final Long id;
    private final String authority;

    UserRoles(Long id, String authority) {
        this.id = id;
        this.authority = authority;
    }


    public static UserRoles fromCodigo(Long id) {
        for (UserRoles role : UserRoles.values()) {
            if (role.id.equals(id)) {
                return role;
            }
        }

        throw new IllegalArgumentException(
                "Código de permissão desconhecido: " + id
        );
    }

    public static UserRoles fromDescricao(String descricao) {
        if (descricao == null || descricao.isEmpty()) {
            return null;
        }

        switch (descricao.trim().toUpperCase()) {
            case "EXECUTOR": return UserRoles.EXECUTOR;

            case "SOLICITANTE": return UserRoles.SOLICITANTE;

            default: return null;
        }
    }

    public String getAuthority() {
        return authority;
    }

    public Long getId() {
        return id;
    }
}