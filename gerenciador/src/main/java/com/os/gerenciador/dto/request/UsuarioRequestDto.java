package com.os.gerenciador.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.br.CPF;

public record UsuarioRequestDto(
        @NotNull @NotBlank String nome,
        @NotBlank @NotNull @Email String email,
        @NotNull @NotBlank String senha,
        @NotNull @NotBlank String cpf,
        String telefone,
        @NotNull @NotBlank Long tipoUsuario

) {
}
