# 🛠️ Gerenciador de Ordens de Serviço

API REST para abertura, atribuição, acompanhamento e encerramento de ordens de serviço (O.S.). O projeto organiza as responsabilidades entre solicitantes e executores e mantém um histórico de atualizações associado a cada ordem.

> Projeto em desenvolvimento: as operações principais de O.S., usuários e histórico estão estruturadas. A autenticação com Spring Security e JWT está planejada e ainda não está implementada.

##  Objetivo

Permitir que um usuário com papel **SOLICITANTE** registre uma O.S. descrevendo o serviço necessário. A ordem começa como `ABERTA`. Um usuário **EXECUTOR** pode assumi-la, alterando o status para `EM_PROCESSO`, registrar atualizações no histórico e, ao concluir o trabalho, encerrar a ordem com status `ENCERRADA`.

O fluxo de status é:

```text
ABERTA → EM_PROCESSO → ENCERRADA
```

As regras de papel e transição de status do fluxo de O.S. são verificadas na camada de serviço. O controle de acesso por autenticação e autorização HTTP será adicionado com Spring Security e JWT.

##  Funcionalidades

- Cadastro, consulta, atualização e remoção de usuários.
- Cadastro e consulta de tipos de usuário (`SOLICITANTE` e `EXECUTOR`).
- Abertura de O.S. por solicitante.
- Atribuição de executor a uma O.S. aberta.
- Finalização de O.S. em andamento.
- Registro e consulta de históricos por usuário ou por ordem.
- DTOs para entrada e saída, com conversão centralizada em mappers.
- Validação de dados com Jakarta Bean Validation.

##  Tecnologias

- **Java 25**
- **Spring Boot 4.1.2**
- **Spring Web MVC** — API REST
- **Spring Data JPA / Hibernate** — persistência e mapeamento objeto-relacional
- **PostgreSQL** — banco de dados relacional
- **Redis e Spring Cache** — dependências para cache
- **Docker Compose** — serviços locais de PostgreSQL e Redis
- **Jakarta Validation** — validação dos dados recebidos
- **Lombok** — redução de código repetitivo
- **Maven** — gerenciamento e build do projeto

## Organização do projeto

O código está organizado por responsabilidade:

| Pacote | Responsabilidade |
| --- | --- |
| `controller` | Expõe os endpoints REST e recebe/devolve DTOs. |
| `service` | Implementa regras de negócio e coordena as operações. |
| `repository` | Acessa o banco por meio do Spring Data JPA. |
| `model` | Entidades JPA e relacionamentos persistidos. |
| `dto.request` / `dto.response` | Contratos de entrada e saída da API. |
| `mapper` | Converte entre DTOs e entidades. |
| `Enums` | Define os papéis e os estados possíveis da O.S. |

### Modelos principais

- **`UsuarioModel`**: dados do usuário e relacionamento com seu tipo/papel.
- **`TipoUsuarioModel`**: tipo associado ao usuário, usado para representar solicitante ou executor.
- **`OrdemServicoModel`**: descrição, status, datas, solicitante, executor e históricos.
- **`HistoricoOrdemServicoModel`**: descrição e data de uma atualização, vinculada a uma ordem e ao usuário que a registrou.

##  Como executar

### Pré-requisitos

- JDK 25
- Docker e Docker Compose
- Maven (opcional, o projeto inclui Maven Wrapper)

Configure as variáveis exigidas pelo `compose.yaml` em um arquivo `.env` na raiz:

```dotenv
POSTGRES_DB=gerenciador_os
POSTGRES_USER=postgres
POSTGRES_PASSWORD=uma_senha_local
REDIS_PASSWORD=uma_senha_redis
```

Suba os serviços:

```bash
docker compose up -d
```

O PostgreSQL fica disponível na porta `5433` do host e o Redis na `6379`. Configure a conexão da aplicação com esses serviços nas propriedades da aplicação (por exemplo, `src/main/resources/application.properties` ou `application.yml`).

Execute a API pelo Maven Wrapper:

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

> A aplicação precisa das configurações de conexão do banco. O Compose fornece os serviços, mas as credenciais e a URL JDBC da aplicação devem estar configuradas para coincidir com o seu ambiente.

## Endpoints

Prefixo base: `http://localhost:8080`

### Ordens de serviço — `/gerenciador/os`

| Método | Rota | Função |
| --- | --- | --- |
| `GET` | `/gerenciador/os` | Lista todas as O.S. |
| `GET` | `/gerenciador/os/{id}` | Consulta uma O.S. pelo ID. |
| `GET` | `/gerenciador/os/user/{id}` | Lista as O.S. de um usuário solicitante. |
| `POST` | `/gerenciador/os` | Abre uma O.S. com status inicial `ABERTA`. |
| `PUT` | `/gerenciador/os/{idOrdem}` | Atribui um executor e muda a O.S. aberta para `EM_PROCESSO`. |
| `PUT` | `/gerenciador/os/finalizar/{id}` | Encerra uma O.S. em andamento. |
| `DELETE` | `/gerenciador/os/{id}` | Remove uma O.S. |

Exemplo de abertura:

```http
POST /gerenciador/os
Content-Type: application/json

{
  "descricao": "Verificar o equipamento da sala 2",
  "usuarioSolicitante": 1
}
```

Exemplo de atribuição:

```http
PUT /gerenciador/os/1
Content-Type: application/json

{
  "idExecutor": 2
}
```

### Histórico — `/gerenciador/historico`

| Método | Rota | Função |
| --- | --- | --- |
| `GET` | `/gerenciador/historico` | Lista todos os registros de histórico. |
| `GET` | `/gerenciador/historico/{id}` | Consulta um registro pelo ID. |
| `GET` | `/gerenciador/historico/user/{id}` | Lista históricos registrados por um usuário. |
| `GET` | `/gerenciador/historico/ordem/{id}` | Lista o histórico de uma O.S. específica. |
| `POST` | `/gerenciador/historico` | Registra uma atualização na O.S. |
| `PUT` | `/gerenciador/historico/{id}` | Atualiza um registro de histórico. |
| `DELETE` | `/gerenciador/historico/{id}` | Remove um registro de histórico. |

Exemplo de registro:

```http
POST /gerenciador/historico
Content-Type: application/json

{
  "idUsuario": 2,
  "idOrdemServico": 1,
  "descricao": "Equipamento em análise; iniciada a substituição da peça."
}
```

### Usuários — `/gerenciador/usuario`

| Método | Rota | Função |
| --- | --- | --- |
| `GET` | `/gerenciador/usuario` | Lista usuários. |
| `GET` | `/gerenciador/usuario/{id}` | Consulta usuário pelo ID. |
| `GET` | `/gerenciador/usuario/tipo_usuario/{idTipoUsuario}` | Lista usuários pelo tipo. |
| `POST` | `/gerenciador/usuario` | Cadastra usuário. |
| `PUT` | `/gerenciador/usuario/{id}` | Atualiza usuário. |
| `PATCH` | `/gerenciador/usuario/{id}` | Altera o tipo/papel do usuário. |
| `DELETE` | `/gerenciador/usuario/{id}` | Remove usuário. |

### Tipos de usuário — `/gerenciador/tipousuario`

| Método | Rota | Função |
| --- | --- | --- |
| `GET` | `/gerenciador/tipousuario` | Lista tipos de usuário. |
| `GET` | `/gerenciador/tipousuario/{id}` | Consulta tipo pelo ID. |
| `POST` | `/gerenciador/tipousuario` | Cadastra um tipo de usuário. |
| `DELETE` | `/gerenciador/tipousuario/{id}` | Remove um tipo de usuário. |

##  Papéis e regras de negócio

- `SOLICITANTE`: pode abrir O.S.; o serviço valida o papel do usuário informado.
- `EXECUTOR`: pode assumir uma O.S. aberta; o serviço valida o papel do executor informado.
- Uma O.S. só pode ser assumida quando está `ABERTA`.
- Uma O.S. só pode ser finalizada quando está `EM_PROCESSO`.
- Ao finalizar, a O.S. passa para `ENCERRADA` e recebe data de finalização.
- Os papéis do domínio são representados por `UserRoles`; os estados são `ABERTA`, `EM_PROCESSO` e `ENCERRADA` em `StatusOS`.

## ️ Próximos passos

- Implementar autenticação e autorização com Spring Security e JWT.
- Restringir endpoints por papel autenticado, vinculando as ações ao usuário do token.

## 📌 Sobre

Erick Henrique Pereira, Desenvolvedor Back-End Java em busca de oportinidades como Desenvolvedor Júnior, buscando aprimorar conhecimentos, exercer os conhecimentos obtidos e aprofundar mais nesse caminho de desenvolvimento, buscando soluções e melhorias de problemas e processos sempre com vontade, determinação e adaptação ao cenário
