package com.os.gerenciador.dto.response;

import com.os.gerenciador.model.UsuarioModel;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UsuarioResponseDto (String nome, String email, String telefone, String cpf, LocalDate dataNascimento){
    public UsuarioResponseDto(UsuarioModel usuarioModel){
        this(
                usuarioModel.getNome(),
                usuarioModel.getEmail(),
                usuarioModel.getTelefone(),
                usuarioModel.getCpf(),
                usuarioModel.getDataNascimento()
        );
    }
}
