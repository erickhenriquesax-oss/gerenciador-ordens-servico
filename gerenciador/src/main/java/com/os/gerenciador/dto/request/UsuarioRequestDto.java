package com.os.gerenciador.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record UsuarioRequestDto(
        @NotNull @NotBlank @Size(max = 50) String nome,
        @NotBlank @NotNull @Email @Size(max = 100)String email,
        @NotNull @NotBlank @Size(max = 100) String senha,
        @NotNull @NotBlank @CPF String cpf,
        @NotNull @NotBlank @Size(max = 13)String telefone,
        @NotNull Long tipoUsuario,
        @NotNull LocalDate dataNascimento

        ) {
}
