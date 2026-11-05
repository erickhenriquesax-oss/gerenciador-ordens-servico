package com.os.gerenciador.controller;

import com.os.gerenciador.config.TokenConfig;
import com.os.gerenciador.config.UsuarioPrincipal;
import com.os.gerenciador.dto.request.LoginRequestDto;
import com.os.gerenciador.dto.response.LoginResponseDto;
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
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenConfig  tokenConfig;


    @PostMapping
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto){
        UsernamePasswordAuthenticationToken emailAndSenha = new UsernamePasswordAuthenticationToken(loginRequestDto.email(), loginRequestDto.senha());
        Authentication autenticacao = authenticationManager.authenticate(emailAndSenha);

        UsuarioPrincipal usuario = (UsuarioPrincipal) autenticacao.getPrincipal();
        String token = tokenConfig.generateToken(usuario);
        return ResponseEntity.ok(new LoginResponseDto(token));
    }
}
