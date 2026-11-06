package com.os.gerenciador.integration;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FluxoOrdemServicoIntegrationTest extends IntegrationTestSupport {

    @Test
    void ordemPercorreTodoOCicloAteSerEncerrada() throws Exception {
        Usuario solicitante = solicitante();
        Usuario executor = executor();
        Long ordem = abrirOrdem(solicitante);

        executar(get("/gerenciador/os/" + ordem), solicitante)
                .andExpect(jsonPath("$.status").value("ABERTA"));

        executar(put("/gerenciador/os/" + ordem), executor, executorJson(executor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_PROCESSO"))
                .andExpect(jsonPath("$.idExecutor").value(executor.id()));

        executar(post("/gerenciador/historico"), solicitante, historicoJson(solicitante, ordem, "Parou ontem à tarde"))
                .andExpect(status().isCreated());
        executar(post("/gerenciador/historico"), executor, historicoJson(executor, ordem, "Filtro trocado"))
                .andExpect(status().isCreated());

        executar(put("/gerenciador/os/finalizar/" + ordem), executor)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENCERRADA"))
                .andExpect(jsonPath("$.dataFinalizacao", notNullValue()));

        executar(get("/gerenciador/historico/ordem/" + ordem), solicitante)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void ordemEncerradaNaoVoltaParaAtendimento() throws Exception {
        Usuario solicitante = solicitante();
        Usuario executor = executor();
        Long ordem = abrirOrdem(solicitante);
        executar(put("/gerenciador/os/" + ordem), executor, executorJson(executor)).andExpect(status().isOk());
        executar(put("/gerenciador/os/finalizar/" + ordem), executor).andExpect(status().isOk());

        executar(put("/gerenciador/os/" + ordem), executor, executorJson(executor))
                .andExpect(status().isConflict());
        executar(put("/gerenciador/os/finalizar/" + ordem), executor)
                .andExpect(status().isConflict());
    }

    @Test
    void ordemEmProcessoNaoPodeSerAssumidaPorOutroExecutor() throws Exception {
        Usuario executor = executor();
        Usuario outroExecutor = executor();
        Long ordem = abrirOrdem(solicitante());
        executar(put("/gerenciador/os/" + ordem), executor, executorJson(executor)).andExpect(status().isOk());

        executar(put("/gerenciador/os/" + ordem), outroExecutor, executorJson(outroExecutor))
                .andExpect(status().isConflict());
        executar(put("/gerenciador/os/finalizar/" + ordem), outroExecutor)
                .andExpect(status().isForbidden());
    }

    @Test
    void executorVeOrdensAbertasMasNaoAsDeOutroExecutor() throws Exception {
        Usuario solicitante = solicitante();
        Usuario executor = executor();
        Usuario outroExecutor = executor();
        Long aberta = abrirOrdem(solicitante);
        Long assumida = abrirOrdem(solicitante);
        executar(put("/gerenciador/os/" + assumida), executor, executorJson(executor)).andExpect(status().isOk());

        executar(get("/gerenciador/os"), outroExecutor)
                .andExpect(jsonPath("$[*].id", hasItem(aberta.intValue())))
                .andExpect(jsonPath("$[*].id", not(hasItem(assumida.intValue()))));
        executar(get("/gerenciador/os/" + assumida), outroExecutor)
                .andExpect(status().isNotFound());
    }

    @Test
    void adminAtribuiApenasParaExecutores() throws Exception {
        Usuario admin = admin();
        Usuario solicitante = solicitante();
        Usuario executor = executor();
        Long ordem = abrirOrdem(solicitante);

        executar(put("/gerenciador/os/" + ordem), admin, executorJson(solicitante))
                .andExpect(status().isBadRequest());
        executar(put("/gerenciador/os/" + ordem), admin, executorJson(executor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idExecutor").value(executor.id()));
    }

    @Test
    void executorPassaAVerHistoricoAnteriorAoAssumirAOrdem() throws Exception {
        Usuario solicitante = solicitante();
        Usuario executor = executor();
        Long ordem = abrirOrdem(solicitante);
        Long historico = idDe(executar(post("/gerenciador/historico"), solicitante,
                historicoJson(solicitante, ordem, "Registrado antes da atribuição")).andExpect(status().isCreated()));

        executar(get("/gerenciador/historico"), executor)
                .andExpect(jsonPath("$[*].id", not(hasItem(historico.intValue()))));

        executar(put("/gerenciador/os/" + ordem), executor, executorJson(executor)).andExpect(status().isOk());

        executar(get("/gerenciador/historico"), executor)
                .andExpect(jsonPath("$[*].id", hasItem(historico.intValue())));
    }

    @Test
    void listagemRefleteAlteracaoLogoAposAEscrita() throws Exception {
        Usuario solicitante = solicitante();
        Usuario executor = executor();
        Long ordem = abrirOrdem(solicitante);
        executar(get("/gerenciador/os"), solicitante)
                .andExpect(jsonPath("$[?(@.id == %d)].status".formatted(ordem)).value("ABERTA"));

        executar(put("/gerenciador/os/" + ordem), executor, executorJson(executor)).andExpect(status().isOk());

        executar(get("/gerenciador/os"), solicitante)
                .andExpect(jsonPath("$[?(@.id == %d)].status".formatted(ordem)).value("EM_PROCESSO"));
    }
}
