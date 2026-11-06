package com.os.gerenciador.integration;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PermissoesIntegrationTest extends IntegrationTestSupport {

    @Test
    void rotaProtegidaExigeToken() throws Exception {
        executar(get("/gerenciador/os"), null).andExpect(status().isUnauthorized());
    }

    @Test
    void documentacaoOpenApiEPublica() throws Exception {
        executar(get("/v3/api-docs"), null).andExpect(status().isOk());
        executar(get("/v3/api-docs.yaml"), null).andExpect(status().isOk());
    }

    @Test
    void solicitanteSoEnxergaAsPropriasOrdens() throws Exception {
        Usuario dono = solicitante();
        Usuario outro = solicitante();
        Long ordem = abrirOrdem(dono);

        executar(get("/gerenciador/os/" + ordem), outro).andExpect(status().isNotFound());
        executar(get("/gerenciador/os"), outro)
                .andExpect(jsonPath("$[*].id", not(hasItem(ordem.intValue()))));
        executar(get("/gerenciador/historico/ordem/" + ordem), outro).andExpect(status().isForbidden());
    }

    @Test
    void solicitanteNaoAbreOrdemEmNomeDeOutro() throws Exception {
        Usuario solicitante = solicitante();
        Usuario outro = solicitante();
        String corpo = "{\"descricao\":\"Teste\",\"usuarioSolicitante\":%d}".formatted(outro.id());

        executar(post("/gerenciador/os"), solicitante, corpo).andExpect(status().isForbidden());
    }

    @Test
    void quemNaoParticipaDaOrdemNaoRegistraHistorico() throws Exception {
        Long ordem = abrirOrdem(solicitante());
        Usuario executor = executor();
        Usuario estranho = solicitante();

        executar(post("/gerenciador/historico"), executor, historicoJson(executor, ordem, "Antes de assumir"))
                .andExpect(status().isForbidden());
        executar(post("/gerenciador/historico"), estranho, historicoJson(estranho, ordem, "Intruso"))
                .andExpect(status().isForbidden());
    }

    @Test
    void solicitanteNaoUsaRotasDeAdministracao() throws Exception {
        Usuario solicitante = solicitante();
        Long ordem = abrirOrdem(solicitante);

        executar(get("/gerenciador/usuario"), solicitante).andExpect(status().isForbidden());
        executar(patch("/gerenciador/usuario/" + solicitante.id()), solicitante, "{\"id\":3}")
                .andExpect(status().isForbidden());
        executar(delete("/gerenciador/os/" + ordem), solicitante).andExpect(status().isForbidden());
    }

    @Test
    void cadastroPublicoNaoCriaAdministrador() throws Exception {
        String corpo = cadastroJson(novoEmail()).replace("\"tipoUsuario\":2", "\"tipoUsuario\":3");

        executar(post("/gerenciador/usuario"), null, corpo).andExpect(status().isForbidden());
    }

    @Test
    void emailComOutraCaixaContaComoDuplicado() throws Exception {
        Usuario existente = solicitante();

        executar(post("/gerenciador/usuario"), null, cadastroJson(existente.email().toUpperCase()))
                .andExpect(status().isConflict());
    }

    @Test
    void loginInformaIdEPapel() throws Exception {
        Usuario executor = executor();
        String login = "{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(executor.email().toUpperCase(), SENHA);

        executar(post("/gerenciador/login"), null, login)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(executor.id()))
                .andExpect(jsonPath("$.role").value("ROLE_EXECUTOR"));
    }

    @Test
    void exclusaoDeRegistroInexistenteRetornaNaoEncontrado() throws Exception {
        Usuario admin = admin();

        executar(delete("/gerenciador/os/999999"), admin).andExpect(status().isNotFound());
        executar(delete("/gerenciador/usuario/999999"), admin).andExpect(status().isNotFound());
        executar(delete("/gerenciador/tipousuario/999999"), admin).andExpect(status().isNotFound());
    }

    @Test
    void descricaoAcimaDoLimiteERecusadaNaValidacao() throws Exception {
        Usuario solicitante = solicitante();
        String corpo = "{\"descricao\":\"%s\",\"usuarioSolicitante\":%d}".formatted("a".repeat(256), solicitante.id());

        executar(post("/gerenciador/os"), solicitante, corpo).andExpect(status().isBadRequest());
    }
}
