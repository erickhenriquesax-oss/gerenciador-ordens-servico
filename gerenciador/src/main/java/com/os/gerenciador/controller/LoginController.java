package com.os.gerenciador.controller;

import com.os.gerenciador.config.TokenConfig;
import com.os.gerenciador.config.UsuarioPrincipal;
import com.os.gerenciador.dto.request.LoginRequestDto;
import com.os.gerenciador.dto.response.LoginResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gerenciador/login")
@Tag(name = "Login", description = "Autenticação e emissão de token JWT")
@SecurityRequirements
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenConfig  tokenConfig;


    @PostMapping
    @Operation(summary = "Realiza login", description = "Autentica com e-mail e senha e retorna um token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticação bem-sucedida; retorna o token, o ID e o papel do usuário", content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Corpo inválido ou campos obrigatórios ausentes"),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos")
    })
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto){
        UsernamePasswordAuthenticationToken emailAndSenha = new UsernamePasswordAuthenticationToken(loginRequestDto.email(), loginRequestDto.senha());
        Authentication autenticacao = authenticationManager.authenticate(emailAndSenha);

        UsuarioPrincipal usuario = (UsuarioPrincipal) autenticacao.getPrincipal();
        String token = tokenConfig.generateToken(usuario);
        return ResponseEntity.ok(new LoginResponseDto(token, usuario.id(), usuario.role()));
    }
}
