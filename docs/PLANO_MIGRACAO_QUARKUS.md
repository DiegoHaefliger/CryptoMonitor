# Plano de migração — CryptoMonitor: Spring Boot → Quarkus, Java 17 → 21, MySQL → Postgres

## 1. Estado atual (levantado do código)

| Item | Hoje |
|---|---|
| Framework | Spring Boot 3.5.0 (web MVC, Data JPA, Security, Kafka, Data Redis, devtools) |
| Java | 17 (`maven.compiler.source/target`) |
| Banco | MySQL 8.0 (`compose.yaml`, base `CryptoPools`, porta 3307), driver `com.mysql.cj.jdbc.Driver` |
| Schema | Liquibase, `db/changelog/changelog-master.xml`, 6 changeSets (2 tabelas: `estrategias`, `condicoes_estrategia`) |
| Código | ~2.567 linhas, 60 arquivos Java, pacote `com.haefliger.cryptomonitor` |
| Config | `./config/application.properties` (fora do classpath, valores em branco no repo) |
| Extras | Lombok, MapStruct 1.5.5, springdoc-openapi 2.7.0, Gson, Java-WebSocket 1.5.6, logback.xml + janino |
| Testes | 1 teste (`contextLoads`) — cobertura efetiva zero |

Alvo de referência já existente no monorepo: `trade/backend` — Quarkus **3.33.3**, Java **21**, Postgres,
MapStruct 1.6.3, Spotless + Checkstyle + ArchUnit. A migração deve **convergir para esse mesmo stack**,
não inventar um segundo.

## 2. Decisões (fechadas em 21/08/2026)

| # | Decisão | Escolha |
|---|---|---|
| D1 | Isolamento no Postgres | Base própria `crypto_monitor` na instância que já roda `crypto_alerts`, **reusando o usuário `admin`** |
| D2 | Dados atuais do MySQL | **Não migrar.** Schema vazio pelo Liquibase; dump do MySQL dispensado pelo usuário em 21/08/2026 |
| D3 | Persistência | `EntityManager` + JPQL tipado, nome de método derivado por fora (igual `trade/backend`) |
| D4 | Lombok | **Removido junto na F4**, arquivo a arquivo, no mesmo passo do port |
| D5 | Repositório | Segue repo git próprio, copiando os gates de build do `trade/backend` |
| D6 | Porta / caminho | `${CRYPTO_MONITOR_HTTP_PORT:8086}` + `quarkus.http.root-path=/crypto-monitor` |
| D7 | Kafka | `quarkus-messaging-kafka` com `@Channel("estrategia") Emitter<String>` |
| D8 | Testes de caracterização | Suíte completa antes da F1 — bloqueante |

Consequências que atravessam o plano:

- **D2 encolhe a F2.** Some a carga de dados, o `setval` das sequences e a conversão
  `TINYINT(1)`→`boolean`. Sobra criar a base, trocar o driver e rodar o Liquibase.
- **D4 aumenta a F4 e torna a D8 obrigatória.** Cada classe é tocada duas vezes no mesmo
  commit (framework + Lombok). Com ~60 arquivos, revisão no olho não é critério aceitável;
  a suíte da F0 é.
- **D1 exige atenção de permissão.** O `admin` já é dono de `crypto_alerts`. Nada impede o
  CryptoMonitor de escrever lá por engano — só a URL de conexão separa. Errar a
  `DATABASE_JDBC_URL` aponta o Liquibase do CryptoMonitor para a base do backend.
  Conferir a URL antes do primeiro `migrate` é o único freio que existe nesse arranjo.

### Ambiente confirmado (verificado em 21/08/2026)

```
container  crypto_alerts_db   postgres:15   127.0.0.1:5432
usuário    admin
bases      crypto_alerts, postgres, template0, template1
```

`crypto_monitor` ainda não existe. Kafka e Redis não estão de pé na máquina.

## 3. Tabela de equivalência Spring → Quarkus

| Spring | Quarkus |
|---|---|
| `@SpringBootApplication` + `SpringApplication.run` | nada — `io.quarkus.runtime.Quarkus` cuida do ciclo; classe `main` some |
| `@Service`, `@Component` | `@ApplicationScoped` |
| `@Repository extends JpaRepository<T,ID>` | `@ApplicationScoped` + `EntityManager` injetado, JPQL tipado por dentro, nome de método derivado por fora |
| `@Configuration` + `@Bean` | classe produtora: `@Produces @ApplicationScoped` |
| `@Value("${x}")` | `@ConfigProperty(name="x")` ou, melhor, uma interface `@ConfigMapping` |
| `@RestController` + `@RequestMapping("/estrategia")` | `@Path("/estrategia")` |
| `@PostMapping` / `@GetMapping` / `@PutMapping` / `@DeleteMapping` | `@POST` / `@GET` / `@PUT` / `@DELETE` + `@Produces(APPLICATION_JSON)` |
| `@RequestParam` | `@QueryParam` |
| `@RequestBody` | parâmetro sem anotação |
| `@ResponseStatus(HttpStatus.CREATED)` | `org.jboss.resteasy.reactive.ResponseStatus(201)` |
| `@ControllerAdvice` + `@ExceptionHandler` | `@Provider implements ExceptionMapper<T>` |
| `MethodArgumentNotValidException` | `ResteasyReactiveViolationException` / `ConstraintViolationException` |
| `HttpServletRequest.getRequestURI()` | `@Context UriInfo uriInfo` |
| `org.springframework.transaction.annotation.@Transactional` | `jakarta.transaction.Transactional` |
| `@EnableScheduling` + `@Scheduled` | `quarkus-scheduler` + `io.quarkus.scheduler.Scheduled` |
| `@Scheduled(initialDelay=10s, fixedDelay=MAX_VALUE)` (gambiarra de startup) | `void onStart(@Observes StartupEvent ev)` — some a gambiarra |
| `@EnableAsync` + `@Async` | `ManagedExecutor` (`quarkus-smallrye-context-propagation`) |
| `RedisTemplate<String,Object>` + `RedisConfig` | `RedisDataSource` → `ValueCommands<String, EstrategiaCacheDTO>` (serialização Jackson nativa; a classe `RedisConfig` inteira some) |
| `KafkaTemplate<String,String>` + `KafkaConfig` | `@Channel("estrategia") Emitter<String>` (a classe `KafkaConfig` inteira some) |
| `spring-boot-starter-security` (tudo `permitAll`) | remover — nenhuma extensão de segurança |
| `springdoc-openapi-starter-webmvc-ui` | `quarkus-smallrye-openapi` — as anotações `io.swagger.v3.oas.annotations.*` do controller ficam **iguais** |
| `logback.xml` + `janino` | `quarkus.log.*` no `application.properties` (Quarkus usa JBoss Log Manager; logback.xml é ignorado) |
| `spring-boot-devtools` | `./mvnw quarkus:dev` |
| MapStruct `componentModel = "spring"` | `componentModel = MappingConstants.ComponentModel.JAKARTA_CDI` |
| `spring-boot-starter-test` | `quarkus-junit5` + `rest-assured` + Testcontainers/Dev Services |

## 4. Fases

### F0 — Preparo — **CONCLUÍDA (21/08/2026)**, exceto o item 4

1. **Encoding — feito, e muito menor que o estimado.** Varredura nos 80 arquivos
   versionados achou **um** arquivo fora de UTF-8: `config/application.properties`,
   com **2 bytes** (`e7 e3`, o "çã" de `# Configuração Redis`). Todos os `.java` já
   estavam em UTF-8, e não há texto duplo-codificado em lugar nenhum.
   O mojibake relatado na versão anterior deste plano era erro do `iconv` usado na
   leitura, não do repositório. Arquivo convertido, conteúdo conferido byte a byte.

2. **Suíte de caracterização — feita. 70 testes, verdes.**

   | Arquivo | Testes | O que trava |
   |---|---|---|
   | `EstrategiaControllerTest` | 6 | contrato HTTP das 4 rotas, embrulho `{"dados": …}`, corpo do 400 |
   | `EstrategiaRequestValidacaoTest` | 13 | mensagem exata de cada validador |
   | `EstrategiaServiceImplTest` | 9 | fluxo, `ServiceException`, efeito só pós-commit |
   | `EstrategiaRSITest` | 9 | RSI de Wilder com valores-ouro, disparo por transição |
   | `EstrategiaPrecoTest` | 8 | cruzamento de alvo para cima/baixo |
   | `FormatterTest` | 11 | sufixo de intervalo |
   | `OperadorComparacaoEnumTest` | 6 | ordem dos argumentos de `comparar` |
   | `KafkaServiceImplTest` | 5 | tópico, chave e texto publicado |
   | `RedisCacheFormatoTest` | 3 | JSON gravado no cache |

   `CryptoMonitorApplicationTests.contextLoads` foi **removido**: subia o contexto
   inteiro e falhava com `spring.datasource.url` vazia — ou seja, **`mvn test` já estava
   vermelho no repositório antes desta fase**. No lugar entraram slices (`@WebMvcTest`)
   e testes unitários, que não dependem de banco, Redis ou Kafka.

3. **Formato do cache Redis — congelado em teste, não em documento.**
   `RedisCacheFormatoTest` grava o JSON exato produzido pelo serializador de produção:

   ```json
   [{"id":1,"nome":"Bitcoin RSI","simbolo":"BTCUSDT","intervalo":"60","operadorLogico":"AND",
     "dateCreated":"2026-01-01T10:00:00","dateLastUpdate":null,"ativo":true,"permanente":false,
     "condicoes":[{"id":10,"tipoIndicador":"RSI","operador":"MENOR","valor":"30",
                   "dateCreated":"2026-01-01T10:00:00"}]}]
   ```

   Propriedades que a versão Quarkus tem que reproduzir: **sem `@class`**, data em
   ISO-8601 (não epoch), nulo explícito no JSON, enum pelo `name()` (`MENOR`, não `<`).
   A ausência de `@class` é o que faz a leitura voltar como `ArrayList<LinkedHashMap>` —
   e é a explicação do `instanceof LinkedHashMap` + `convertValue` que existe hoje no
   `RedisServiceImpl`, que sem isso parece código sem motivo.

4. **`ParametersProperties` — PENDENTE, depende do valor real.**
   Continua lendo `spring.redis.keys.estrategias.ativas`, que não existe em nenhum
   arquivo do repositório. Ver §7.

**Saída:** repo em UTF-8, `mvn test` verde pela primeira vez (70 testes), formato do
cache travado por teste.

#### Achados da F0 que mudam a migração

| # | Achado | Consequência |
|---|---|---|
| 1 | MapStruct já emite 3 `Unmapped target properties` (`EstrategiaMapper` ×2, `EstrategiaCacheMapper` ×1) | Com `unmappedTargetPolicy=ERROR` na F4, **quebram o build**. É o comportamento desejado, mas tem que ser resolvido explicitamente, não descoberto no meio do port |
| 2 | `@NotEmptyWithFieldMessage` na lista de condições **não dispara para lista vazia** — o validador só trata `null` e `String` vazia | Lista vazia produz 1 violação (`"não deve estar vazio"`, do `@NotEmpty`); lista nula produz 2. O contrato de erro depende disso e está travado em teste |
| 3 | `EstrategiaPreco` acessa `getCondicoes().get(0)` sem guarda; `EstrategiaRSI` tem guarda | Estratégia de preço sem condição estoura `IndexOutOfBoundsException`. Comportamento travado como está — **não corrigir durante a migração** |
| 4 | `EstrategiaMediaMovel` é um esqueleto: só loga, com `// TODO` | Nada a caracterizar e nada a preservar. O `TipoIndicadorEnum.MEDIA_MOVEL` é aceito pela validação e não faz nada |
| 5 | `statusEstrategia` de id inexistente vira `ServiceException` (500), não 404 | Preservado como está na migração; virar 404 é decisão de produto, não de port |
| 6 | Mensagem de RSI publica o **valor-alvo**, não o RSI calculado (`sendMessage(…, rsiAlvo, …)` cai no parâmetro chamado `rsi`) | Texto publicado é `"valor RSI < 80"` com 80 = alvo. Travado em teste como está |
| 7 | JDK padrão da máquina é **25**; o projeto compila com `release 17` | Build precisa de `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`. Na F1 isso deixa de ser contorno e vira o alvo |

### F1 — Java 21 ainda no Spring — **CONCLUÍDA (21/08/2026)**

Duas linhas no `pom.xml`:

- `<java.version>` de `17` para `21`;
- o Lombok em `annotationProcessorPaths`, que estava pinado em `1.18.32`
  (não compila em 21), passa a usar `${lombok.version}` do
  `spring-boot-starter-parent` — **1.18.38**, exatamente a versão que a
  dependência já resolvia. O pin mantinha processador e biblioteca em versões
  diferentes sem nenhum motivo.

Resultado: 70 testes verdes, bytecode `major version 65`, jar empacotando normal.

**Achado:** a suíte passa igual no JDK 21 e no JDK 25 (o padrão desta máquina),
os dois compilando com `release 21`. O Lombok 1.18.38 aguenta o 25 — ou seja, o
pin antigo era o único bloqueio real, não a versão do JDK instalado. Ainda assim,
o build de referência é o 21: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`.

**Saída:** aplicação idêntica, rodando em 21.

### F2 — MySQL → Postgres ainda no Spring — **CONCLUÍDA (21/08/2026)**

1. Base criada na instância que já roda `crypto_alerts`:
   `CREATE DATABASE crypto_monitor OWNER admin`. `DATABASECHANGELOG` e
   `DATABASECHANGELOGLOCK` nasceram dentro dela — nada compartilhado com `crypto_alerts`.
2. Dump do MySQL **dispensado** pelo usuário: os dados não interessam. O serviço
   `mysql` e o volume `mysql_data` saíram do `compose.yaml`; o `redis` ficou, e o
   Postgres é externo (container `crypto_alerts_db`).
3. `mysql-connector-j` → `org.postgresql:postgresql`; `driver-class-name` removido
   (o Spring deduz). URL, usuário e senha passam a vir por env var com default local.
4. Liquibase rodado contra a base vazia: 8 changeSets aplicados, schema conferido no `psql`.

**Achado principal da fase — `valor` era `TEXT`.** Com `ddl-auto=validate` o contexto
não sobe:

```
Schema-validation: wrong column type encountered in column [valor] in table
[condicoes_estrategia]; found [text (Types#VARCHAR)], but expecting [numeric(20,8)]
```

`CondicaoEstrategia.valor` sempre foi `BigDecimal` com `precision=20, scale=8`, e o
código faz `getValor().doubleValue()`. O `TEXT` do changelog nunca incomodou porque no
MySQL o Hibernate rodava **sem schema-validation** — ninguém conferia. O changeSet 8
corrige para `NUMERIC(20,8)`, com as duas grafias (`USING valor::numeric` no Postgres,
`MODIFY COLUMN` no MySQL, já que `text`→`numeric` não tem cast de atribuição) e rollback.

Vale registrar o que isso significa: **o banco de produção do MySQL guarda números numa
coluna de texto**. Com a D2 (começar vazio) o problema morre junto com os dados; se a
decisão fosse migrar, essa conversão seria o passo mais delicado da carga.

**Verificação — `SchemaPostgresIT`, 9 testes contra o Postgres real:** criação do schema
pelo Liquibase, validação do mapeamento pelo Hibernate, identity gerando id, enum
gravado como texto (`AND`, `RSI`, `MENOR_IGUAL`, não ordinal), 8 casas decimais
preservadas, CHECK de `operador_logico` recusando valor fora da lista, cascata da FK
apagando as condições, e o `join fetch` funcionando.

O teste **pula sozinho** quando o Postgres não responde (`@EnabledIf`), então `mvn verify`
continua verde em máquina sem banco. Entrou o `maven-failsafe-plugin` para separar as
duas suítes: `mvn test` = 70 testes rápidos, `mvn verify` = +9 de integração. Sem ele o
sufixo `IT` não é coletado por ninguém e o teste nunca rodaria.

```bash
DB_PASSWORD=... ./mvnw -B verify     # 70 + 9, verde
./mvnw -B verify                     # 70 + 9 pulados, verde
```

**Saída:** app Spring apontando para Postgres, schema criado do zero e validado.
**Ponto de rollback seguro.**

#### Testcontainers: era versão de API, não contexto do Docker

A primeira leitura desta sessão culpou o contexto `desktop-linux` e a
`DockerDesktopClientProviderStrategy`. **Diagnóstico errado.** Com o log completo, a
primeira estratégia tentada mostra a causa real:

```
UnixSocketClientProviderStrategy: failed with exception BadRequestException (Status 400:
{"message":"client version 1.32 is too old. Minimum supported API version is 1.40,
 please upgrade your client to a newer version"})
```

O daemon local é Docker **29.6.1**, com `ApiVersion 1.55` e **`MinAPIVersion 1.40`**. O
docker-java embutido no Testcontainers 1.21.0 cai no default antigo **1.32** quando não
negocia versão, e o daemon recusa. O socket sempre esteve acessível — por isso o
`/_ping` respondia 200 e o `docker ps` funcionava.

Correção: `api.version=1.44` como system property do failsafe, no `pom.xml`. Nada de
`docker context use default`, que não teria resolvido.

Com isso o `SchemaPostgresIT` voltou a usar Testcontainers (`postgres:15` + `@ServiceConnection`),
guardado por `DockerClientFactory.instance().isDockerAvailable()` para pular onde não há
Docker. Vantagem sobre apontar para o banco local: roda contra uma base virgem toda vez,
não precisa de senha, e prova o changelog do zero a cada execução.

**Consequência para a F3:** o Quarkus Dev Services usa o mesmo Testcontainers, então vai
precisar da mesma `api.version` enquanto o Docker local for 29.x. Fora isso, Dev Services
funciona nesta máquina — o bloqueio que este plano registrava não existe.

### F3 — Esqueleto Quarkus

Novo `pom.xml` com `quarkus-bom 3.33.3` (mesma LTS do `trade/backend`) e extensões:

```
quarkus-rest, quarkus-rest-jackson, quarkus-hibernate-orm, quarkus-jdbc-postgresql,
quarkus-hibernate-validator, quarkus-liquibase, quarkus-redis-client,
quarkus-messaging-kafka, quarkus-scheduler, quarkus-smallrye-openapi,
quarkus-smallrye-context-propagation, quarkus-arc
```

Fora: `spring-boot-*`, `spring-security-*`, `springdoc`, `janino`, `logback`,
`mysql-connector-j`, `gson` (Jackson já cobre). `quarkus-maven-plugin` no lugar do
`spring-boot-maven-plugin`; MapStruct sobe para 1.6.3 com
`-Amapstruct.unmappedTargetPolicy=ERROR`, igual `trade/backend`.

`config/application.properties` (hoje fora do classpath, carregado pela convenção do
Spring) vira `src/main/resources/application.properties`, com indireção por env var e
sem nenhum segredo versionado:

```properties
quarkus.http.port=${CRYPTO_MONITOR_HTTP_PORT:8086}
quarkus.http.root-path=/crypto-monitor

quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=${DB_USER:admin}
quarkus.datasource.password=${DB_PASSWORD:}
quarkus.datasource.jdbc.url=${DATABASE_JDBC_URL:jdbc:postgresql://localhost:5432/crypto_monitor}

quarkus.hibernate-orm.database.generation=none
quarkus.hibernate-orm.schema-management.strategy=validate
quarkus.liquibase.change-log=db/changelog/changelog-master.xml
quarkus.liquibase.migrate-at-start=true

mp.messaging.outgoing.estrategia.connector=smallrye-kafka
mp.messaging.outgoing.estrategia.topic=strategy
mp.messaging.outgoing.estrategia.value.serializer=org.apache.kafka.common.serialization.StringSerializer
kafka.bootstrap.servers=${KAFKA_BOOTSTRAP_SERVERS:}

quarkus.redis.hosts=${REDIS_URL:redis://localhost:6395}
```

`schema-management.strategy=validate` com `migrate-at-start=true`: o Liquibase é o dono
do schema, o Hibernate só confere. Difere do `trade/backend`, onde o Liquibase está
passivo porque o Alembic (Python) ainda manda — aqui não há segundo dono.

A porta `8086` fecha a D6: `8080` é o dashboard do Traefik na VPS e `8085` é o kernel.
O `root-path` preserva o `/crypto-monitor` que o `TradeFront` já chama.

**Saída:** `./mvnw quarkus:dev` sobe, `/q/health` e `/q/openapi` respondem, Liquibase
cria o schema em `crypto_monitor`. Sem regra de negócio ainda.

### F4 — Portar por fatia vertical (framework + Lombok no mesmo passo)

Cada classe é tocada uma vez só, resolvendo as duas coisas: anotação de framework e
saída do Lombok. Ordem de dentro para fora, cada item compilando e com a suíte da F0
verde antes do próximo.

Padrão de substituição do Lombok:

| Lombok | Vira |
|---|---|
| `@Data` / `@Getter` / `@Setter` em DTO | `record` com componentes finais |
| `@Builder` em DTO | construtor canônico do `record` (ou `Builder` à mão onde há muitos campos opcionais) |
| `@Data` em `@Entity` | acessores explícitos, **sem** `equals`/`hashCode` gerados |
| `@AllArgsConstructor` / `@RequiredArgsConstructor` em bean | construtor explícito (injeção por construtor, PADRÕES §4) |
| `@Slf4j` | `private static final Logger log = Logger.getLogger(X.class)` (JBoss Logging) |
| `@Getter @AllArgsConstructor` em `enum` | construtor e acessor escritos à mão |

`@Data` em entidade JPA é armadilha conhecida: gera `equals`/`hashCode` sobre todos os
campos, incluindo o `id` mutável e a coleção `condicoes` com `LAZY`. Em `Estrategia`
isso significa que comparar duas instâncias pode disparar carga da coleção, e que o
hash de uma entidade muda depois do `persist`. Ao sair do Lombok, **não recriar**:
entidade sem `equals`/`hashCode`, ou por `id` apenas quando não nulo.

1. **Entidades** — JPA é o mesmo, só sai o Lombok e ajusta o `equals`/`hashCode` acima.
2. **Repositórios** — `JpaRepository` some. `findByAtivo`, `findByAtivoFetchCondicoes`,
   `save`, `deleteById`, `findById` viram métodos explícitos com `EntityManager` e
   `TypedQuery`, nome derivado por fora. `findById` devolve `Optional` via
   `getResultStream().findFirst()`.
3. **Mappers** — `componentModel` para `JAKARTA_CDI`, MapStruct 1.6.3, e
   `unmappedTargetPolicy=ERROR` ligado. Com os DTO virando `record`, o MapStruct passa a
   usar o construtor canônico — é aqui que campo desalinhado quebra o build, que é o ponto.
4. **Serviços** — `@ApplicationScoped`, `jakarta.transaction.@Transactional`.
   `TransactionSynchronizationManager` (usado em `EstrategiaServiceImpl`) não existe no
   Quarkus: equivalente é `TransactionSynchronizationRegistry` (JTA) ou um evento CDI
   com `@Observes(during = AFTER_SUCCESS)`. **Ponto de atenção real — não é substituição
   mecânica, e falha em silêncio se for portado errado.**
5. **Infra** — Redis vira `RedisDataSource` → `ValueCommands<String, EstrategiaCacheDTO>`;
   Kafka vira `@Channel("estrategia") Emitter<String>`. `RedisConfig` e `KafkaConfig` são
   deletadas inteiras. `KafkaEnum.ESTRATEGIA("strategy")` é o único tópico e nunca há
   consumo, então o canal fixo do Emitter cobre todo o uso atual — a assinatura
   `sendMessage(String topic, ...)` pode perder o parâmetro `topic`.
6. **Web** — controller para JAX-RS, `GlobalExceptionHandler` para `ExceptionMapper`.
   Manter o corpo de erro (`ApiErrorResponse`) **byte a byte igual**, senão quebra o
   `TradeFront`. Atenção: o Quarkus tem mapper próprio para violação de Bean Validation,
   que devolve outro formato — o mapper customizado precisa ter prioridade.
7. **Startup/agendamento** — `StartupScheduledTask` vira observador de `StartupEvent`;
   `EstrategiaAsyncService` usa `ManagedExecutor`.
8. **WebSocket** — a lib `Java-WebSocket` continua igual (não é Spring). Atenção:
   `WebSocketConnectionManager` é `@Component` com construtor recebendo
   `Map<String,List<String>>`, que nenhum container consegue injetar — hoje só funciona
   porque é instanciado à mão. Na migração isso vira **erro de deployment do ArC**
   (falha no build, não em runtime). Corrigir tornando-o objeto criado por fábrica,
   não bean. Os dois `@Value` de reconexão passam a ser parâmetros da fábrica.
9. **Validators** — Bean Validation puro, portam sem mudança; o registro dos
   `ConstraintValidator` passa a ser via CDI.
10. **`Constants`** — hoje é `@Configuration` sem nenhum bean, só constantes estáticas.
    Perde a anotação e vira classe final com construtor privado.

**Saída:** rotas idênticas ao Spring, zero Lombok no `pom.xml`, suíte portada para
`@QuarkusTest` + REST Assured.

### F5 — Paridade e corte

1. Comparar OpenAPI antigo x novo (`/v3/api-docs` x `/q/openapi`) — divergência aqui é contrato quebrado com o front.
2. Comparar o payload real de cada rota (mesma base, mesmas requisições).
3. Validar o consumo do cache Redis com dados gravados pela versão Spring (formato da F0).
4. Validar publicação no Kafka: mesmo tópico, mesma chave, mesmo JSON.
5. Rodar os dois em paralelo por um período contra a mesma base, se o ambiente permitir.

**Saída:** decisão de corte com evidência, não com fé.

### F6 — Gates de build (alinhar com `trade/backend`)

Spotless (google-java-format AOSP), Checkstyle, e o `LayeredArchitectureTest` do ArchUnit —
que já cobre exatamente o que o `PADROES_CODIGO.md` pede: Resource sem JPA/Repository/Entity,
`@Transactional` só em Service. Copiar `config/checkstyle/` do `trade/backend`.

Dívida existente entra congelada (store do ArchUnit + suppressions), como lá. Código novo, nunca.

### F7 — Limpeza (opcional, depois do corte)

- `EstrategiaServiceImpl` e afins: aplicar o limite de ~300 linhas / 5 responsabilidades
  do PADRÕES.
- Avaliar build nativo (GraalVM) — a `Java-WebSocket` e a reflexão do Jackson exigem
  registro de reflexão; só depois de tudo estável, e apenas com ganho medido de
  memória/boot, não por princípio.
- Revisar o `KafkaService.sendMessage(String topic, ...)`: com canal fixo, o parâmetro
  `topic` deixou de ter função.

(A remoção do Lombok saiu daqui — pela D4 acontece na F4.)

## 5. Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Encoding misto no fonte | Mensagens/logs corrompidos, diff ilegível | F0 antes de tudo, commit isolado |
| Zero cobertura de teste hoje | Nenhuma forma de provar paridade, ainda mais com Lombok saindo junto | F0.2 é bloqueante (D8) |
| `DATABASE_JDBC_URL` errada apontando para `crypto_alerts` | Liquibase do CryptoMonitor cria tabela na base do backend | Conferir a URL antes do primeiro `migrate`; a D1 reusa o `admin`, então não há barreira de permissão |
| Framework + Lombok no mesmo commit (D4) | Diff grande demais para revisão no olho | Fatia vertical pequena, suíte verde entre cada uma |
| `equals`/`hashCode` recriados na entidade ao sair do `@Data` | Carga inesperada de `condicoes`, hash mudando após `persist` | Entidade sem `equals`/`hashCode`, ou por `id` não nulo |
| Serialização do cache Redis muda | Cache antigo vira lixo silencioso | Comparar formato (F0.3); invalidar a chave no corte |
| `TransactionSynchronizationManager` sem equivalente direto | Efeito pós-commit deixa de disparar, sem erro | `TransactionSynchronizationRegistry` + teste específico |
| `WebSocketConnectionManager` com injeção impossível | Build ArC falha | Vira objeto de fábrica (F4.8) |
| Mapper de validação do Quarkus vence o customizado | Corpo de erro 400 muda e quebra o `TradeFront` | Prioridade explícita no `ExceptionMapper` + teste de contrato |
| Contrato HTTP mudar sem querer | `TradeFront` quebra | Diff de OpenAPI + payload na F5 |
| Volume `mysql_data` removido sem cópia | Dados atuais perdidos de vez | Dump antes de mexer no compose (F2.2) |

## 6. Ordem resumida

```
F0 encoding + testes  →  F1 Java 21  →  F2 Postgres  →  F3 esqueleto Quarkus
   →  F4 portar por fatia  →  F5 paridade e corte  →  F6 gates  →  F7 limpeza
```

Cada fase termina com a suíte verde e é um ponto de rollback. F1 e F2 acontecem **ainda no Spring**:
isso separa "mudou o Java", "mudou o banco" e "mudou o framework" em três investigações diferentes
em vez de uma só, impossível de depurar.

## 7. Pendências de configuração (valores que faltam)

Nenhuma bloqueia o começo da F0, mas as três primeiras bloqueiam a subida do app na F3.

| Chave | Situação |
|---|---|
| `spring.redis.keys.estrategias.ativas` | Lida por `ParametersProperties`, **não existe** em `config/application.properties` nem em nenhum arquivo do repo. O app não sobe sem ela. Descobrir o valor em uso antes de portar |
| `spring.data.redis.host` / `.port` | Em branco no repo. O `compose.yaml` publica Redis em `6395`, mas nenhum container Redis está de pé |
| `kafka.bootstrap-servers` | Em branco no repo, nenhum broker rodando na máquina |
| Senha do Postgres (`admin`) | Vai por env (`DB_PASSWORD`), nunca no arquivo versionado |

Nenhum desses valores entra no repositório: todos com indireção por variável de ambiente,
no mesmo estilo do `application.properties` do `trade/backend`.
