package com.os.gerenciador.controller;

import com.os.gerenciador.config.SecurityConfig;
import com.os.gerenciador.config.TokenConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoginController.class)
@Import({SecurityConfig.class, LoginSecurityTest.CacheConfiguration.class})
class LoginSecurityTest {
    @Autowired private MockMvc mockMvc;

    @MockitoBean private AuthenticationManager authenticationManager;
    @MockitoBean private TokenConfig tokenConfig;

    @TestConfiguration(proxyBeanMethods = false)
    static class CacheConfiguration {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("usuarios", "tiposUsuario", "ordens", "historicos");
        }
    }

    @Test
    void loginComSenhaIncorretaRetornaNaoAutorizado() throws Exception {
        given(authenticationManager.authenticate(any()))
                .willThrow(new BadCredentialsException("Credenciais inválidas"));

        mockMvc.perform(post("/gerenciador/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@example.com","senha":"incorreta"}
                                """))
                .andExpect(status().isUnauthorized());

        verify(authenticationManager).authenticate(any());
    }

    @Test
    void loginComCamposVaziosRetornaRequisicaoInvalida() throws Exception {
        mockMvc.perform(post("/gerenciador/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"","senha":""}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationManager, tokenConfig);
    }

    @Test
    void rotaProtegidaSemTokenRetornaNaoAutorizado() throws Exception {
        mockMvc.perform(get("/gerenciador/os"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void metodoGetNaoPodeUsarRotaDeLogin() throws Exception {
        mockMvc.perform(get("/gerenciador/login"))
                .andExpect(status().isUnauthorized());
    }
}
