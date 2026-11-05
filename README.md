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
- [Papéis de usuário](#papéis-de-usuário)
  - [Primeiro administrador](#primeiro-administrador)
- [Autenticação](#autenticação)
- [Endpoints principais](#endpoints-principais)
  - [Usuários](#usuários)
  - [Ordens de serviço](#ordens-de-serviço)
  - [Histórico](#histórico)
  - [Tipos de usuário](#tipos-de-usuário)
  - [Exemplos de requisição e resposta](#exemplos-de-requisição-e-resposta)
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

Copie `.env.example` para `.env` na raiz do projeto e ajuste os valores. No PowerShell, use `Copy-Item .env.example .env`; em macOS/Linux, use `cp .env.example .env`. O `.env` está no `.gitignore`; mantenha nele apenas as credenciais locais e não o versione.

Gere um segredo aleatório para o JWT com OpenSSL:

```shell
openssl rand -base64 32
```

Coloque o resultado em `API_SECURITY_TOKEN_SECRET` no `.env`. O projeto assina tokens com HS256, que exige uma chave de pelo menos 256 bits (32 bytes). O Spring carrega `.env` quando a aplicação é iniciada a partir da raiz do projeto. O Docker Compose também usa esse arquivo para configurar os containers.

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

O Hibernate está configurado com `ddl-auto=update`, que atualiza o schema durante o desenvolvimento. Não use essa estratégia em produção; para ambientes controlados, adote migrações versionadas com Flyway ou Liquibase.

## Papéis de usuário

Os papéis são associados aos registros de `tb_tipo_usuario` por ID:

| ID | Papel | Autoridade |
|---:|---|---|
| 1 | Executor | `ROLE_EXECUTOR` |
| 2 | Solicitante | `ROLE_SOLICITANTE` |
| 3 | Administrador | `ROLE_ADMIN` |

Na primeira inicialização, `TipoUsuarioDataInitializer` insere esses três tipos se a tabela estiver vazia. O código relaciona as roles aos IDs numéricos. Ao adicionar ou alterar papéis, atualize também `UserRoles` e o processo de inicialização para manter esse mapeamento consistente.

O cadastro público cria somente usuários solicitantes. A role enviada no cadastro deve corresponder ao tipo de solicitante (ID `2`).

### Primeiro administrador

O projeto cria os tipos de usuário, mas não cria uma conta administradora. Cadastre primeiro um usuário pela rota pública `POST /gerenciador/usuario`, usando o tipo solicitante (`tipoUsuario: 2`). Exemplo de corpo (use seus próprios dados):

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

No prompt do `psql`, substitua o e-mail pelo usado no cadastro:

```sql
UPDATE tb_usuario
SET id_tipo_usuario = 3
WHERE email = 'admin@exemplo.com';
```

Os valores `gerenciador` e `gerenciador_os` acima são os do `.env.example`; use os valores configurados no seu `.env`. A senha cadastrada pela API já fica armazenada com BCrypt. O tipo `3` corresponde a `ROLE_ADMIN`.

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

O login retorna JSON no formato abaixo. O token expira em uma hora:

```json
{
  "token": "<jwt>"
}
```

Copie o valor de `token` da resposta e envie-o no cabeçalho das próximas requisições protegidas:

```http
Authorization: Bearer <jwt>
```

## Endpoints principais

Todas as rotas usam o prefixo `/gerenciador`.

### Usuários

| Método e rota | Acesso |
|---|---|
| `POST /usuario` | Público; cria somente solicitantes |
| `GET /usuario` | Administrador |
| `GET /usuario/{id}` | Administrador ou executor; qualquer usuário pode consultar o próprio cadastro |
| `PUT /usuario/{id}` | Administrador ou o próprio usuário |
| `PATCH /usuario/{id}` | Administrador; altera o tipo do usuário |
| `DELETE /usuario/{id}` | Administrador |
| `GET /usuario/tipo_usuario/{idTipoUsuario}` | Administrador |

### Ordens de serviço

| Método e rota | Acesso e escopo |
|---|---|
| `GET /os` | Os três papéis; solicitante vê as próprias, executor vê todas abertas e as atribuídas a si, administrador vê todas |
| `GET /os/{id}` | Mesmas regras de escopo da listagem |
| `GET /os/user/{id}` | Administrador ou o próprio usuário |
| `POST /os` | Solicitante cria para si; administrador também pode criar |
| `PUT /os/{idOrdem}` | Executor assume informando o próprio `idExecutor` no corpo; administrador pode atribuir a ordem a um executor |
| `PUT /os/finalizar/{id}` | Executor atribuído ou administrador |
| `DELETE /os/{id}` | Administrador |

### Histórico

| Método e rota | Acesso e escopo |
|---|---|
| `GET /historico` | Os três papéis; retorna histórico próprio ou de ordens das quais participa; administrador vê tudo |
| `GET /historico/{id}` | Autor do histórico, participante da ordem relacionada ou administrador |
| `GET /historico/user/{id}` | Administrador ou o próprio usuário; consulta entradas registradas por esse usuário |
| `GET /historico/ordem/{id}` | Solicitante da ordem, executor atribuído ou administrador |
| `POST /historico` | Usuário autenticado que participa da ordem; administrador pode registrar para outro usuário |
| `PUT /historico/{id}` | Administrador |
| `DELETE /historico/{id}` | Administrador |

Alterar ou excluir entradas de histórico é permitido somente ao administrador para preservar a rastreabilidade dos registros. O solicitante e o executor participante podem adicionar novas entradas para documentar o andamento da O.S.

### Tipos de usuário

`/tipousuario` oferece listagem, criação, consulta por ID e exclusão para administradores. Como a autorização depende dos IDs fixos descritos acima, novos tipos precisam ser mapeados no código antes de serem usados como roles.

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

## Erros HTTP

| Status | Quando ocorre |
|---|---|
| `400 Bad Request` | Corpo inválido, falha de validação ou argumento incorreto |
| `401 Unauthorized` | Token ausente, inválido ou expirado; credenciais de login inválidas |
| `403 Forbidden` | Usuário autenticado sem permissão para a operação ou recurso |
| `404 Not Found` | Registro inexistente ou O.S. fora do escopo de consulta do usuário |
| `409 Conflict` | Transição inválida da O.S. ou conflito com restrição de dados, como e-mail duplicado |

Os erros tratados pelo `ApiExceptionHandler` retornam JSON no formato `{"erro":"mensagem"}`. A autenticação sem token retorna `401`.

## Cache

As listagens de usuários, tipos, ordens e históricos usam Redis com TTL de 10 minutos. As chaves das ordens e do histórico incluem o usuário e o contexto de acesso. Operações de criação, alteração e exclusão invalidam os caches relacionados.

## Testes

Execute a suíte com:

```shell
./mvnw test
```

No Windows PowerShell:

```powershell
.\mvnw.cmd test
```

Os testes usam H2 e um cache em memória no perfil `test`; não precisam do PostgreSQL nem do Redis locais. Há testes para autenticação, acesso sem token, cadastro de usuários, criação e atribuição de ordens e visibilidade de ordens abertas para executores.

## Estrutura do código

- `controller`: endpoints HTTP e autorização por role
- `service`: regras de negócio e transações
- `repository`: persistência com Spring Data JPA
- `model`: entidades JPA
- `dto`: formatos de entrada e saída da API
- `mapper`: conversões entre DTOs e entidades
- `config`: segurança, JWT, tratamento global de erros e inicialização dos tipos

## Limitações e documentação

- O projeto ainda não inclui Swagger/OpenAPI nem uma coleção Postman/Insomnia; este README documenta as rotas e os exemplos disponíveis.
- A criação do primeiro administrador exige a etapa manual de promoção no banco descrita acima.
- Ainda não há migrações versionadas; para produção, adote Flyway ou Liquibase em vez de `ddl-auto=update`.
- A aplicação não oferece reabertura de ordens encerradas.
- O repositório não declara uma licença nem um canal de contato. Defina essas informações antes de distribuir o projeto.

## Sobre mim

Sou Erick Henrique Pereira e estou em busca da minha primeira oportunidade como desenvolvedor backend Java júnior. Neste projeto, venho trabalhando com Java, Spring Boot, criação de APIs REST, Spring Security e JWT, Spring Data JPA, PostgreSQL, Redis, Maven, Docker Compose e testes com JUnit.
