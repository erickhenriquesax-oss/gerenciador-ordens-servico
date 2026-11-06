package com.os.gerenciador.dto.response;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.Serializable;

public record UsuarioResponseDto (Long id, String nome, String email, String telefone, String cpf, LocalDate dataNascimento) implements Serializable {}
