package com.os.gerenciador.integration;

import com.jayway.jsonpath.JsonPath;
import com.os.gerenciador.Enums.UserRoles;
import com.os.gerenciador.repository.TipoUsuarioRepository;
import com.os.gerenciador.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class IntegrationTestSupport {

    protected static final String SENHA = "senha-de-teste";
    private static final AtomicInteger SEQUENCIA = new AtomicInteger();

    @Autowired protected MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TipoUsuarioRepository tipoUsuarioRepository;

    protected record Usuario(Long id, String email, String token) {}

    protected Usuario solicitante() throws Exception {
        return criar(null);
    }

    protected Usuario executor() throws Exception {
        return criar(UserRoles.EXECUTOR);
    }

    protected Usuario admin() throws Exception {
        return criar(UserRoles.ADMIN);
    }

    protected String novoEmail() {
        return "usuario" + SEQUENCIA.incrementAndGet() + "@teste.local";
    }

    protected String cadastroJson(String email) {
        return """
                {"nome":"Usuário Teste","email":"%s","senha":"%s","cpf":"52998224725",
                 "telefone":"11999999999","tipoUsuario":2,"dataNascimento":"1990-01-01"}
                """.formatted(email, SENHA);
    }

    protected ResultActions executar(MockHttpServletRequestBuilder request, Usuario usuario, String corpo) throws Exception {
        if (usuario != null) {
            request.header("Authorization", "Bearer " + usuario.token());
        }
        if (corpo != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(corpo);
        }
        return mockMvc.perform(request);
    }

    protected ResultActions executar(MockHttpServletRequestBuilder request, Usuario usuario) throws Exception {
        return executar(request, usuario, null);
    }

    protected Long abrirOrdem(Usuario solicitante) throws Exception {
        String corpo = """
                {"descricao":"Ar-condicionado sem refrigerar","usuarioSolicitante":%d}
                """.formatted(solicitante.id());
        return idDe(executar(post("/gerenciador/os"), solicitante, corpo).andExpect(status().isCreated()));
    }

    protected String historicoJson(Usuario autor, Long ordemId, String descricao) {
        return """
                {"idUsuario":%d,"idOrdemServico":%d,"descricao":"%s"}
                """.formatted(autor.id(), ordemId, descricao);
    }

    protected String executorJson(Usuario executor) {
        return "{\"idExecutor\":%d}".formatted(executor.id());
    }

    protected Long idDe(ResultActions resultado) throws Exception {
        Number id = JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private Usuario criar(UserRoles papel) throws Exception {
        String email = novoEmail();
        Long id = idDe(executar(post("/gerenciador/usuario"), null, cadastroJson(email))
                .andExpect(status().isCreated()));

        if (papel != null) {
            var usuario = usuarioRepository.findById(id).orElseThrow();
            usuario.setTipoUsuarioModel(tipoUsuarioRepository.findById(papel.getId()).orElseThrow());
            usuarioRepository.save(usuario);
        }

        String login = """
                {"email":"%s","senha":"%s"}
                """.formatted(email, SENHA);
        String resposta = executar(post("/gerenciador/login"), null, login)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new Usuario(id, email, JsonPath.read(resposta, "$.token"));
    }
}
