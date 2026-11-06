package com.os.gerenciador.mapper;

import com.os.gerenciador.dto.request.UsuarioRequestDto;
import com.os.gerenciador.dto.response.UsuarioResponseDto;
import com.os.gerenciador.model.TipoUsuarioModel;
import com.os.gerenciador.model.UsuarioModel;

public final class UsuarioMapper {
    private UsuarioMapper() {}

    public static UsuarioModel toModel(UsuarioRequestDto request, TipoUsuarioModel tipo) {
        return UsuarioModel.builder().nome(request.nome()).cpf(request.cpf()).telefone(request.telefone())
                .email(request.email()).senha(request.senha()).dataNascimento(request.dataNascimento())
                .tipoUsuarioModel(tipo).build();
    }

    public static UsuarioResponseDto toResponse(UsuarioModel model) {
        return new UsuarioResponseDto(model.getId(), model.getNome(), model.getEmail(), model.getTelefone(), model.getCpf(), model.getDataNascimento());
    }
}
