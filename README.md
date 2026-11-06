# Gerenciador de Ordens de Serviço

[![Java 25](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot 4.1.2](https://img.shields.io/badge/Spring%20Boot-4.1.2-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)

**Stack:** Java 25 · Spring Boot 4.1.2 · Spring Security e JWT · Spring Data JPA · PostgreSQL · Redis

API REST para organizar o atendimento de solicitações por meio de ordens de serviço (O.S.). Um solicitante registra um problema ou serviço necessário; um executor assume a O.S., registra o que está sendo feito e, ao concluir o atendimento, encerra a ordem. O histórico mantém os acontecimentos e as atividades registrados durante esse processo, dando visibilidade ao solicitante, ao executor responsável e ao administrador.

A API também gerencia usuários e seus tipos, controla quem pode consultar ou alterar cada recurso e oferece autenticação por token.

## Sumário

- [Fluxo principal da O.S.](#fluxo-principal-de-uma-os)
  - [Estados e transições](#estados-e-transições-da-os)
- [Requisitos](#requisitos)
- [Configuração](#configuração)
- [Executar](#executar)
  - [Integração do Spring com o Docker Compose](#integração-do-spring-com-o-docker-compose)
- [Diagnóstico rápido](#diagnóstico-rápido)
- [Papéis de usuário](#papéis-de-usuário)
  - [Primeiro administrador](#primeiro-administrador)
- [Autenticação](#autenticação)
- [Endpoints principais](#endpoints-principais)
  - [Usuários](#usuários)
  - [Ordens de serviço](#ordens-de-serviço)
  - [Histórico](#histórico)
  - [Papéis disponíveis](#papéis-disponíveis)
  - [Exemplos de requisição e resposta](#exemplos-de-requisição-e-resposta)
- [Campos obrigatórios dos requests](#campos-obrigatórios-dos-requests)
- [Documentação interativa (Swagger)](#documentação-interativa-swagger)
- [Erros HTTP](#erros-http)
- [Cache](#cache)
- [Testes](#testes)
- [Estrutura do código](#estrutura-do-código)
- [Limitações e documentação](#limitações-e-documentação)

## Fluxo principal de uma O.S.

1. **Abertura:** o solicitante cria uma O.S. para si e descreve o serviço ou problema. Um administrador também pode abrir uma O.S. em nome de um solicitante. A O.S. fica aberta aguardando atendimento.
2. **Atribuição:** um executor assume uma O.S. aberta. O administrador também pode atribuir a ordem a um executor.
3. **Acompanhamento:** o solicitante e o executor responsável podem registrar entradas no histórico, descrevendo o que aconteceu e o que foi ou está sendo executado. O histórico fica associado à O.S.
4. **Encerramento:** o executor responsável encerra a O.S. O administrador também pode realizar essa ação.

O administrador tem acesso de gestão. Solicitantes acompanham suas próprias ordens, e executores podem consultar as ordens abertas e as que estão atribuídas a eles.

### Estados e transições da O.S.

- `ABERTA`: criada e aguardando um executor.
- `EM_PROCESSO`: executor assumiu a ordem.
- `ENCERRADA`: ordem encerrada pelo executor atribuído ou pelo administrador.

As transições permitidas são `ABERTA → EM_PROCESSO → ENCERRADA`. A aplicação não oferece reabertura de O.S. encerrada. A ação da API chama-se “finalizar” (`PUT /gerenciador/os/finalizar/{id}`), e registra a data em `dataFinalizacao`.

## Requisitos

- Java 25
- Docker e Docker Compose, para iniciar PostgreSQL e Redis
- Maven Wrapper incluído no projeto

## Configuração

Todos os comandos deste README devem ser executados dentro da pasta `gerenciador/`, onde ficam `pom.xml`, `mvnw`, `compose.yaml` e `.env.example`.

Copie `.env.example` para `.env` nessa pasta e ajuste os valores. No PowerShell, use `Copy-Item .env.example .env`; em macOS/Linux, use `cp .env.example .env`. O `.env` está no `.gitignore`; mantenha nele apenas as credenciais locais e não o versione.

Gere um segredo aleatório para o JWT com OpenSSL:

```shell
openssl rand -base64 32
```

Coloque o resultado em `API_SECURITY_TOKEN_SECRET` no `.env`. O projeto assina tokens com HS256, que exige uma chave de pelo menos 256 bits (32 bytes). O Spring carrega `.env` quando a aplicação é iniciada a partir da pasta `gerenciador/`. O Docker Compose também usa esse arquivo para configurar os containers.

Os containers leem o `.env` apenas quando são criados. Se você alterar `POSTGRES_*` ou `REDIS_PASSWORD` depois disso, o `.env` deixa de corresponder aos containers em execução:

- Redis: recrie o container com `docker compose up -d --force-recreate redis`. O cache é descartável.
- PostgreSQL: o volume `postgres_data` mantém o banco, o usuário e a senha da primeira criação. Volte o `.env` para os valores originais ou recrie o volume com `docker compose down -v`. Esse comando **apaga os dados**.

## Executar

Inicie os serviços de infraestrutura:

```shell
docker compose up -d
```

Inicie a API diretamente pelo Maven Wrapper:

```shell
./mvnw spring-boot:run
```

No Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

A API fica disponível em `http://localhost:8080`. O PostgreSQL é publicado na porta `5433`, e o Redis na `6379`.

O Hibernate está configurado com `ddl-auto=update`, que atualiza o schema durante o desenvolvimento. Esse modo cria tabelas e colunas novas, mas não altera colunas que já existem (por exemplo, aumentar o `length` de um `@Column` não muda o tamanho no banco) e não remove tabelas de entidades que deixaram de existir. Nesses casos, ajuste o banco manualmente com `ALTER TABLE` ou recrie o schema. Não use essa estratégia em produção; para ambientes controlados, adote migrações versionadas com Flyway ou Liquibase.

### Integração do Spring com o Docker Compose

O `pom.xml` inclui `spring-boot-docker-compose`. Com isso, `./mvnw spring-boot:run` lê o `compose.yaml` e sobe os serviços sozinho quando eles não estão rodando. O `docker compose up -d` acima continua válido e é útil para subir a infraestrutura antes da API.

Essa integração também muda a origem das credenciais:

- **PostgreSQL:** a conexão é montada a partir do container, com host, porta, banco, usuário e senha tirados das variáveis do serviço `postgres`. Os valores de `spring.datasource.*` no `application.properties` são ignorados.
- **Redis:** o serviço `redis` do `compose.yaml` tem a label `org.springframework.boot.ignore: "true"`. No Spring Boot 4.1.2, a conexão Redis vinda do Docker Compose traz apenas host e porta, sem senha. Sem essa label, a aplicação conectava sem autenticar e toda consulta com cache falhava com `NOAUTH Authentication required`. Com a label, o Redis usa `spring.data.redis.*`, inclusive `REDIS_PASSWORD` do `.env`.

Nos testes, a integração fica desligada (`spring.docker.compose.enabled=false` em `application-test.properties`).

## Diagnóstico rápido

- Se a inicialização falhar ao conectar ao PostgreSQL ou Redis, confirme que os serviços estão ativos com `docker compose ps` e que as portas e credenciais do `.env` correspondem às dos containers (veja a seção [Configuração](#configuração)).
- Se as listagens retornarem erro `RedisConnectionFailureException` / `NOAUTH Authentication required`, confira que a label `org.springframework.boot.ignore` continua no serviço `redis` do `compose.yaml` e que o container foi recriado depois de alterar `REDIS_PASSWORD`. Ver [Integração do Spring com o Docker Compose](#integração-do-spring-com-o-docker-compose).
- Se o Swagger retornar `401`, reinicie a aplicação para carregar as regras de segurança atualizadas e confira que está acessando `/swagger-ui.html`, `/v3/api-docs` ou `/v3/api-docs.yaml` na porta da aplicação (por padrão, `8080`).
- Se o banco for recriado (por exemplo, com `ddl-auto=create` ou `docker compose down -v`), limpe também o cache do Redis. Caso contrário, as listagens podem devolver dados antigos por até 10 minutos.
- Limpe o cache também depois de alterar os campos de um DTO de resposta. O Redis guarda os objetos serializados no formato antigo, e eles deixam de corresponder à classe nova. Para limpar, use `docker compose exec redis redis-cli -a <REDIS_PASSWORD> FLUSHALL`.
- O `pom.xml` usa Spring Boot 4.1.2 e springdoc 3.2.0; o projeto springdoc documenta a linha 3.x para Spring Boot 4 ([matriz oficial de compatibilidade](https://github.com/springdoc/springdoc-openapi)).

## Papéis de usuário

Os papéis são associados aos registros de `tb_tipo_usuario` por ID:

| ID | Papel | Autoridade |
|---:|---|---|
| 1 | Executor | `ROLE_EXECUTOR` |
| 2 | Solicitante | `ROLE_SOLICITANTE` |
| 3 | Administrador | `ROLE_ADMIN` |

Na primeira inicialização, `TipoUsuarioDataInitializer` insere esses três tipos se a tabela estiver vazia. O código relaciona os papéis às autoridades e aos IDs numéricos. Ao adicionar ou alterar papéis, atualize também `UserRoles` e o processo de inicialização para manter esse mapeamento consistente.

O cadastro público cria somente usuários solicitantes. O ID enviado no campo `tipoUsuario` deve corresponder ao papel solicitante (ID `2`).

### Primeiro administrador

O projeto cria os papéis de usuário, mas não cria uma conta administradora. Cadastre primeiro um usuário pela rota pública `POST /gerenciador/usuario`, usando o papel solicitante (`tipoUsuario: 2`). Exemplo de corpo (use seus próprios dados):

```json
{
  "nome": "Administrador inicial",
  "email": "admin@exemplo.com",
  "senha": "troque-por-uma-senha-forte",
  "cpf": "52998224725",
  "telefone": "11999999999",
  "tipoUsuario": 2,
  "dataNascimento": "1990-01-01"
}
```

Depois, promova essa conta diretamente no banco, como etapa de bootstrap, e confirme que o `UPDATE` afetou exatamente um registro:

```shell
docker compose exec postgres psql -U gerenciador -d gerenciador_os
```

No prompt do `psql`, substitua o e-mail pelo usado no cadastro, em letras minúsculas, porque a API grava o e-mail dessa forma:

```sql
UPDATE tb_usuario
SET id_tipo_usuario = 3
WHERE email = 'admin@exemplo.com';
```

Os valores `gerenciador` e `gerenciador_os` acima são os do `.env.example`; use os valores configurados no seu `.env` (`POSTGRES_USER` e `POSTGRES_DB`). A senha cadastrada pela API já fica armazenada com BCrypt. O tipo `3` corresponde a `ROLE_ADMIN`.

Depois da promoção, faça login de novo: o papel fica gravado no token, e um token emitido antes do `UPDATE` continua com o papel antigo.

## Autenticação

Faça login com e-mail e senha:

```http
POST /gerenciador/login
Content-Type: application/json

{
  "email": "usuario@exemplo.com",
  "senha": "sua-senha"
}
```

O login retorna o token, o ID e o papel do usuário autenticado. O token expira em uma hora:

```json
{
  "token": "<jwt>",
  "id": 2,
  "role": "ROLE_SOLICITANTE"
}
```

Use o `id` retornado nos campos que pedem o próprio ID: `usuarioSolicitante` ao abrir uma O.S., `idExecutor` ao assumir uma O.S. e `idUsuario` ao registrar histórico.

Copie o valor de `token` da resposta e envie-o no cabeçalho das próximas requisições protegidas:

```http
Authorization: Bearer <jwt>
```

O e-mail é comparado sem diferenciar maiúsculas e minúsculas: `Ana@Exemplo.com` e `ana@exemplo.com` entram na mesma conta.

O token é um JWT assinado com HS256 e contém estas informações:

| Claim | Conteúdo |
|---|---|
| `sub` | e-mail do usuário |
| `id` | ID do usuário |
| `role` | autoridade (`ROLE_SOLICITANTE`, `ROLE_EXECUTOR` ou `ROLE_ADMIN`) |
| `exp` | expiração, uma hora após o login |

O `id` e o `role` da resposta de login são os mesmos valores gravados nas claims do token.

O papel é lido do token a cada requisição, sem consultar o banco. Por isso, depois de alterar o papel (`PATCH /usuario/{id}`), a senha ou excluir um usuário, os tokens já emitidos continuam válidos até expirar. Faça login de novo para obter um token com os dados atualizados.

## Endpoints principais

Todas as rotas usam o prefixo `/gerenciador`.

### Usuários

| Método e rota | Acesso | Resposta de sucesso |
|---|---|---|
| `POST /usuario` | Público; cria somente solicitantes | `201 Created` |
| `GET /usuario` | Administrador | `200 OK` |
| `GET /usuario/{id}` | Administrador ou executor; qualquer usuário pode consultar o próprio cadastro | `200 OK` |
| `PUT /usuario/{id}` | Administrador ou o próprio usuário | `200 OK` |
| `PATCH /usuario/{id}` | Administrador; altera o papel do usuário | `200 OK` |
| `DELETE /usuario/{id}` | Administrador | `204 No Content` |
| `GET /usuario/tipo_usuario/{idTipoUsuario}` | Administrador | `200 OK` |

Observações sobre usuários:

- O e-mail é gravado sem espaços nas pontas e em letras minúsculas, no cadastro e no `PUT`. Por isso, `Ana@Exemplo.com` e `ana@exemplo.com` contam como duplicados (`409`).
- O `PUT /usuario/{id}` substitui o cadastro inteiro: envie todos os campos, incluindo `senha`, que é sempre codificada de novo. O campo `tipoUsuario` é obrigatório no corpo, mas o `PUT` o ignora. O papel só muda pelo `PATCH`.
- As respostas trazem `id`, `nome`, `email`, `telefone`, `cpf` e `dataNascimento`. O cadastro (`POST /usuario`) já devolve o `id` do novo usuário.
- O CPF é validado, mas não precisa ser único.
- Um usuário que tem ordens ou históricos não pode ser excluído (`409`).

### Ordens de serviço

| Método e rota | Acesso e escopo | Resposta de sucesso |
|---|---|---|
| `GET /os` | Os três papéis; solicitante vê as próprias, executor vê todas abertas e as atribuídas a si, administrador vê todas | `200 OK` |
| `GET /os/{id}` | Mesmas regras de escopo da listagem | `200 OK` |
| `GET /os/user/{id}` | Administrador ou o próprio usuário | `200 OK` |
| `POST /os` | Solicitante cria para si; administrador também pode criar | `201 Created` |
| `PUT /os/{idOrdem}` | Executor assume informando o próprio `idExecutor` no corpo; administrador pode atribuir a ordem a um executor | `200 OK` |
| `PUT /os/finalizar/{id}` | Executor atribuído ou administrador | `200 OK` |
| `DELETE /os/{id}` | Administrador | `204 No Content` |

Para um solicitante ou executor, uma O.S. fora do seu escopo responde `404` em `GET /os/{id}`, como se não existisse. Só uma O.S. `ABERTA` pode receber executor. Tentar assumir ou atribuir uma O.S. em processo ou encerrada retorna `409`. Finalizar uma O.S. que não está `EM_PROCESSO` também retorna `409`, e um executor que não é o atribuído recebe `403`. A exclusão de uma O.S. apaga em cascata as entradas de histórico ligadas a ela.

### Histórico

| Método e rota | Acesso e escopo | Resposta de sucesso |
|---|---|---|
| `GET /historico` | Os três papéis; retorna histórico próprio ou de ordens das quais participa; administrador vê tudo | `200 OK` |
| `GET /historico/{id}` | Autor do histórico, participante da ordem relacionada ou administrador | `200 OK` |
| `GET /historico/user/{id}` | Administrador ou o próprio usuário; consulta entradas registradas por esse usuário | `200 OK` |
| `GET /historico/ordem/{id}` | Solicitante da ordem, executor atribuído ou administrador | `200 OK` |
| `POST /historico` | Usuário autenticado que participa da ordem; administrador pode registrar para outro usuário | `201 Created` |
| `PUT /historico/{id}` | Administrador | `200 OK` |
| `DELETE /historico/{id}` | Administrador | `204 No Content` |

Alterar ou excluir entradas de histórico é permitido somente ao administrador para preservar a rastreabilidade dos registros. O solicitante e o executor participante podem adicionar novas entradas para documentar o andamento da O.S., inclusive depois que ela é encerrada. Para quem não participa da ordem, `GET /historico/ordem/{id}` responde `403`.

### Papéis disponíveis

| Método e rota | Acesso | Resposta de sucesso |
|---|---|---|
| `GET /tipousuario` | Administrador | `200 OK` |
| `POST /tipousuario` | Administrador | `201 Created` |
| `GET /tipousuario/{id}` | Administrador | `200 OK` |
| `DELETE /tipousuario/{id}` | Administrador | `200 OK` |

Como a autorização depende dos IDs fixos descritos acima, novos tipos precisam ser mapeados no código antes de serem usados como papéis. A API não bloqueia isso: o `PATCH /usuario/{id}` aceita um tipo sem papel mapeado, e depois disso o login desse usuário retorna `400`. As respostas de `/tipousuario` trazem `id` e `descricao`.

### Campos obrigatórios dos requests

Os campos abaixo são obrigatórios conforme os DTOs de entrada; `@Valid` também aplica as restrições indicadas:

| Operação | Campos obrigatórios e validações principais |
|---|---|
| `POST /login` | `email` (até 100 caracteres) e `senha` não podem ser vazios |
| `POST /usuario`, `PUT /usuario/{id}` | `nome` (até 50), `email` válido (até 100), `senha` (até 100), `cpf` válido, `telefone` (até 13), `tipoUsuario` (ID do papel) e `dataNascimento` |
| `PATCH /usuario/{id}` | `id` do papel a associar |
| `POST /os` | `descricao` (até 255 caracteres) e `usuarioSolicitante` |
| `PUT /os/{idOrdem}` | `idExecutor` |
| `POST /historico`, `PUT /historico/{id}` | `idUsuario`, `idOrdemServico` e `descricao` não vazia (até 255 caracteres) |
| `POST /tipousuario` | `descricao` não vazia (até 255 caracteres) |

Os limites são definidos com `@Size` nos DTOs de entrada; um valor acima do limite retorna `400`. Eles são iguais ou menores que o tamanho das colunas no banco (255 caracteres), então o texto é recusado na validação, antes de chegar ao banco. Não há validação de data de nascimento no futuro, de formato de telefone nem de tamanho mínimo de senha.

No cadastro público (`POST /gerenciador/usuario`), além da validação do corpo: um papel diferente de SOLICITANTE retorna `403`; um ID de tipo inexistente retorna `404`; um ID sem papel mapeado no código retorna `400`; e e-mail duplicado retorna `409`. O cadastro retorna `201 Created` quando concluído.

### Exemplos de requisição e resposta

Os exemplos abaixo assumem que `TOKEN` foi obtido pelo login e que os IDs correspondem a registros existentes. Substitua os valores pelos dados do seu banco. Para solicitantes e executores, o servidor confere se os IDs informados no corpo correspondem ao usuário autenticado e à O.S.; o administrador pode operar em nome de outros usuários.

Criar uma O.S. como solicitante autenticado (o `usuarioSolicitante` deve ser o ID do próprio solicitante):

```http
POST /gerenciador/os
Authorization: Bearer TOKEN
Content-Type: application/json

{
  "descricao": "Manutenção do ar-condicionado da sala 12",
  "usuarioSolicitante": 2
}
```

Na requisição, `usuarioSolicitante` informa o ID do solicitante. Na resposta, o DTO expõe esse mesmo valor como `idSolicitante`; os nomes diferem porque representam os formatos de entrada e saída da API.

Exemplo de resposta `201 Created`:

```json
{
  "id": 15,
  "descricao": "Manutenção do ar-condicionado da sala 12",
  "status": "ABERTA",
  "idSolicitante": 2,
  "idExecutor": null,
  "dataCriacao": "2026-11-05T10:30:00",
  "dataFinalizacao": null
}
```

Assumir a O.S. como executor: envie o ID do próprio executor no corpo. O administrador pode usar o mesmo endpoint para atribuir a O.S. a outro executor.

```http
PUT /gerenciador/os/15
Authorization: Bearer TOKEN
Content-Type: application/json

{
  "idExecutor": 7
}
```

Adicionar uma anotação ao histórico como solicitante ou executor que participa da O.S. `idUsuario` deve ser o ID do usuário autenticado; o administrador pode registrar em nome de outro usuário.

```http
POST /gerenciador/historico
Authorization: Bearer TOKEN
Content-Type: application/json

{
  "idUsuario": 2,
  "idOrdemServico": 15,
  "descricao": "Atendimento iniciado; equipamento em avaliação."
}
```

Exemplo de resposta `201 Created`:

```json
{
  "id": 31,
  "dataHistorico": "2026-11-05T11:00:00",
  "idUsuario": 2,
  "idOrdemServico": 15,
  "descricao": "Atendimento iniciado; equipamento em avaliação."
}
```

Encerrar a ordem assumida (a ação do endpoint é chamada “finalizar”):

```http
PUT /gerenciador/os/finalizar/15
Authorization: Bearer TOKEN
```

## Documentação interativa (Swagger)

Com a aplicação em execução, acesse:

- Interface Swagger UI: <http://localhost:8080/swagger-ui.html>
- Especificação OpenAPI em JSON: <http://localhost:8080/v3/api-docs>
- Especificação OpenAPI em YAML: <http://localhost:8080/v3/api-docs.yaml>

As três rotas são públicas. No `SecurityConfig`, `/v3/api-docs.yaml` é liberado à parte, porque o padrão `/v3/api-docs/**` cobre apenas os caminhos abaixo de `/v3/api-docs/`.

Para autenticar e executar operações protegidas:

1. Faça login em `POST /gerenciador/login` usando e-mail e senha. Essa rota é pública e não exige token.
2. Copie o valor de `token` retornado no JSON.
3. Na interface Swagger UI, clique em **Authorize**.
4. Cole o token JWT no campo exibido e confirme em **Authorize**. Como o esquema é HTTP Bearer, informe somente o token; o Swagger adiciona o prefixo `Bearer` nas requisições.
5. Feche a janela de autorização e execute as operações. As permissões ainda dependem do papel e do escopo do usuário autenticado.

## Erros HTTP

| Status | Quando ocorre |
|---|---|
| `400 Bad Request` | Corpo inválido, falha de validação ou argumento incorreto |
| `401 Unauthorized` | Token ausente, inválido ou expirado; credenciais de login inválidas |
| `403 Forbidden` | Usuário autenticado sem permissão para a operação ou recurso |
| `404 Not Found` | Registro inexistente em consultas, alterações e exclusões, ou O.S. fora do escopo de consulta do usuário |
| `409 Conflict` | Transição inválida da O.S. ou conflito com restrição de dados, como e-mail duplicado ou exclusão de usuário ou papel com vínculos |

As exceções tratadas pelo `ApiExceptionHandler` retornam JSON no formato `{"erro":"mensagem"}`. Algumas respostas fogem desse formato:

- A autenticação ausente ou inválida é respondida pelo Spring Security com `401` sem corpo.
- `GET /os/{id}` e `GET /usuario/{id}` respondem `404` sem corpo.
- JSON malformado, corpo ausente, método não suportado (`405`) e `Content-Type` incorreto (`415`) usam o corpo padrão do Spring (`timestamp`, `status`, `error`, `path`).

## Cache

As consultas gerais anotadas com `@Cacheable` para usuários, papéis, ordens e históricos usam Redis com TTL de 10 minutos (`spring.cache.redis.time-to-live`). A chave da listagem de ordens inclui o ID do usuário e os indicadores de administrador e executor; a chave do histórico inclui o ID do usuário e o indicador de administrador. As operações de escrita invalidam os caches associados conforme as anotações `@CacheEvict` nos serviços.

| Cache | Chave | Invalidado por |
|---|---|---|
| `usuarios` | `todos` | cadastro, `PUT`, `PATCH` e `DELETE` de usuário |
| `tiposUsuario` | `todos` | criação e exclusão de tipo |
| `ordens` | `usuarioId:admin:executor` | criação, atribuição, finalização e exclusão de O.S. |
| `historicos` | `usuarioId:admin` | criação, alteração e exclusão de histórico, e atribuição e exclusão de O.S. |

A atribuição de uma O.S. também invalida `historicos`, porque `GET /historico` inclui o histórico das ordens em que o usuário é o executor. Assim que o executor assume uma ordem, ele passa a ver as entradas registradas antes da atribuição.

## Testes

Execute a suíte com:

```shell
./mvnw test
```

No Windows PowerShell:

```powershell
.\mvnw.cmd test
```

Os testes não precisam do PostgreSQL nem do Redis locais. A suíte tem 36 testes:

| Classe | Tipo | O que cobre |
|---|---|---|
| `GerenciadorApplicationTests` | `@SpringBootTest` com perfil `test` (H2, cache em memória, Docker Compose desligado) | carregamento do contexto |
| `FluxoOrdemServicoIntegrationTest` | integração (`@SpringBootTest` + `MockMvc`) | ciclo completo da O.S. até o encerramento, transições bloqueadas depois do encerramento, disputa entre executores, visibilidade do executor, atribuição pelo admin, histórico visível ao executor logo depois de assumir e listagem atualizada depois de uma escrita |
| `PermissoesIntegrationTest` | integração (`@SpringBootTest` + `MockMvc`) | acesso sem token, documentação OpenAPI pública, isolamento entre solicitantes, histórico restrito a participantes, rotas de administração, cadastro público como admin, e-mail duplicado com outra caixa, `id` e papel no login, `404` nos `DELETE` e limite de tamanho |
| `LoginSecurityTest` | `@WebMvcTest` | login com senha incorreta (`401`) e com campos vazios (`400`), rota protegida sem token (`401`) e `GET` na rota de login |
| `UsuarioServiceTest` | unitário (Mockito) | cadastro com senha em BCrypt, e-mail gravado em minúsculas, normalização independente do locale, bloqueio de cadastro como ADMIN, tipo inexistente, `id` na resposta do cadastro e `DELETE` de usuário inexistente (`404`) |
| `OrdemServicoServiceTest` | unitário (Mockito) | criação de ordem aberta, executor assumindo ordem, consulta usada para o executor ver as abertas, recusa de atribuição a quem não é executor e `DELETE` de ordem inexistente (`404`) |

Os testes de service usam repositórios simulados e verificam regras isoladas. Os de integração sobem a aplicação inteira e fazem as requisições HTTP com tokens reais. Eles passam pela segurança (`@PreAuthorize`), pelas consultas JPQL de visibilidade e pelo cache em memória do perfil `test`. A classe `IntegrationTestSupport` concentra o cadastro, a definição do papel e o login usados nesses testes.

O perfil `test` não define `api.security.token.secret`. Por isso, o `GerenciadorApplicationTests` precisa do `API_SECURITY_TOKEN_SECRET` do `.env` (ou de uma variável de ambiente com esse nome) para iniciar.

## Estrutura do código

- `controller`: endpoints HTTP e autorização por papel
- `service`: regras de negócio e transações
- `repository`: persistência com Spring Data JPA
- `model`: entidades JPA
- `dto`: formatos de entrada e saída da API
- `mapper`: conversões entre DTOs e entidades
- `config`: segurança, JWT, tratamento global de erros e inicialização dos tipos

## Limitações e documentação

- O projeto ainda não inclui uma coleção Postman/Insomnia.
- A criação do primeiro administrador exige a etapa manual de promoção no banco descrita acima.
- Ainda não há migrações versionadas; para produção, adote Flyway ou Liquibase em vez de `ddl-auto=update`.
- A aplicação não oferece reabertura de ordens encerradas, nem cancelamento pelo solicitante ou devolução da ordem pelo executor.
- As listagens não têm paginação nem filtros (por status, por exemplo).
- Tokens emitidos continuam válidos até expirar, mesmo depois de mudança de papel, troca de senha ou exclusão do usuário.
- Executores e administradores conseguem consultar CPF, telefone e data de nascimento de qualquer usuário por `GET /usuario/{id}`.

## Sobre mim

Sou Erick Henrique Pereira e estou em busca da minha primeira oportunidade como desenvolvedor backend Java júnior. Neste projeto, venho trabalhando com Java, Spring Boot, criação de APIs REST, Spring Security e JWT, Spring Data JPA, PostgreSQL, Redis, Maven, Docker Compose e testes com JUnit.

- LinkedIn: [linkedin.com/in/erickhenriquepereira](https://www.linkedin.com/in/erickhenriquepereira/)
- E-mail: [erickhenriquesax@gmail.com](mailto:erickhenriquesax@gmail.com)
