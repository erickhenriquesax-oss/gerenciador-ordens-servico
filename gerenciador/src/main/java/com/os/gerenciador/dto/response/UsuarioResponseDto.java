package com.os.gerenciador.dto.response;


import java.time.LocalDate;
import java.time.LocalDateTime;

public record UsuarioResponseDto (String nome, String email, String telefone, String cpf, LocalDate dataNascimento) {}
