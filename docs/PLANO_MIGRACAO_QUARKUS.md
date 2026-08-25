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
| `springdoc-openapi-starter-webmvc-ui` | `quarkus-smallrye-openapi` — e as anotações `io.swagger.v3.oas.annotations.*` **precisam virar** `org.eclipse.microprofile.openapi.annotations.*` |
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

### F3 + F4 — Quarkus — **CONCLUÍDAS (21/08/2026)**

As duas fases foram num commit só. **Não existe estado intermediário que compile:**
trocar o `pom.xml` para Quarkus deixa todo código anotado com Spring quebrado até o
port terminar. Um commit "F3" isolado seria um commit vermelho — pior que um commit
grande.

**Build.** `quarkus-bom 3.33.3`, a mesma LTS do `trade/backend`. Saíram `spring-boot`,
`spring-security`, `springdoc`, `logback`, `janino` e `lombok`. Entraram `quarkus-rest`,
`hibernate-orm`, `jdbc-postgresql`, `hibernate-validator`, `liquibase`, `redis-client`,
`messaging-kafka`, `scheduler`, `smallrye-openapi`, `smallrye-health` e
`context-propagation`. MapStruct em 1.6.3 com `unmappedTargetPolicy=ERROR` e
`defaultComponentModel=jakarta-cdi`.

**Lombok saiu junto (D4).** DTOs, domínios e DTOs de erro viraram `record`. Entidades
ganharam acessores explícitos e ficaram **sem `equals`/`hashCode`** — o `@Data` gerava
os dois sobre o `id` mutável e sobre a coleção lazy `condicoes`, então comparar duas
instâncias podia disparar carga e o hash mudava depois do `persist`. `@Slf4j` virou
`LoggerFactory` do SLF4J: o Quarkus faz a ponte para o JBoss LogManager, então os
`{}` dos logs seguem iguais — o que evitou reescrever dezenas de chamadas para `%s`.

**Classes que deixaram de existir:** `CryptoMonitorApplication`, `SecurityConfig` (era
`permitAll` puro), `RedisConfig`, `KafkaConfig`, `ParametersProperties` e
`CondicaoEstrategiaRepository`, que não era usado por ninguém.

#### O que não foi substituição mecânica

| Ponto | O que aconteceu |
|---|---|
| `TransactionSynchronizationManager` | Virou `TransactionSynchronizationRegistry` (JTA), com o efeito pós-commit condicionado a `Status.STATUS_COMMITTED`. Ganhou teste de rollback, que antes não existia |
| **Anotações OpenAPI** | `io.swagger.v3` **não é reconhecido pelo Quarkus** — este plano dizia o contrário e estava errado. Migradas para `org.eclipse.microprofile.openapi`. O `@Operation` do MP não tem atributo `responses`, então as `@APIResponse` passaram para o método |
| `MultiSymboPriceHandlerService` | Recebia um `Map` no construtor, que container nenhum injeta. Virou estado interno. A F0 tinha achado isso só no `WebSocketConnectionManager`; eram dois |
| `WebSocketConnectionManager` / `WebSocketClient` | Perderam `@Component`. Sempre foram instanciados à mão; no ArC a anotação viraria erro de build. Os dois `@Value` de reconexão passaram a parâmetros de construtor, vindos de `@ConfigProperty` no `WebSocketServiceImpl` |
| `KafkaTemplate` | Virou `Emitter` com `OutgoingKafkaRecordMetadata`, **preservando o tópico dinâmico por mensagem** em vez de fixá-lo no canal |
| `RedisTemplate` | Virou `RedisDataSource` gravando String JSON explícita, para manter byte a byte o formato congelado na F0 |
| Corpo do erro 400 | Passou a usar o caminho absoluto (`/crypto-monitor/estrategia`). É o que o `getRequestURI()` do Spring devolvia **em produção**; o teste da F0 rodava em MockMvc sem context-path e por isso registrou `/estrategia` |
| MapStruct | Os 3 `Unmapped target properties` previstos na F0 quebraram o build, como esperado. Resolvidos com `@Mapping(ignore = true)` explícito em `id`, `dateCreated`, `dateLastUpdate` e `estrategia` |

#### Testes

80 verdes. Dev Services sobe um `postgres:15` descartável para os `@QuarkusTest`; Redis e
Kafka ficam com `devservices.enabled=false`, já que nenhum teste os exercita de verdade.
O endereço do banco em produção ficou sob `%prod.`, porque Dev Services só liga quando a
url está ausente.

`SchemaPostgresIT` virou `SchemaPostgresTest`: com Dev Services a suíte inteira já depende
de Docker, então a separação surefire/failsafe montada na F2 deixou de significar algo — e
o failsafe tinha ido embora junto com o pom do Spring, de modo que o IT não rodou em
nenhuma das execuções até ser renomeado. Ganhou um teste a mais, travando que
`condicoes_estrategia.valor` é `numeric(20,8)`.

#### Verificação com o app de pé

Empacotado e rodado contra o Postgres real:

```
CryptoMonitor 0.0.1-SNAPSHOT on JVM (powered by Quarkus 3.33.3) started in 4.104s
Listening on: http://0.0.0.0:8086

GET  /crypto-monitor/estrategia   -> 200 {"dados":{"estrategias":[]}}
POST /crypto-monitor/estrategia   -> 400 {"timestamp":...,"status":400,"error":"Bad Request",
                                          "errors":[...],"path":"/crypto-monitor/estrategia"}
GET  /crypto-monitor/q/openapi    -> as 4 rotas
GET  /crypto-monitor/q/health     -> banco UP, Redis DOWN (não há Redis rodando)
```

O `/q/health` fica sob o `root-path`, não na raiz. O erro de Redis na subida é registrado
e **não derruba a aplicação** — mesmo comportamento da versão Spring.

**Saída:** aplicação inteira em Quarkus, zero Spring, zero Lombok, 80 testes verdes.

### F5 — Paridade e corte — **CONCLUÍDA (21/08/2026)**

Comparação feita com as **duas versões rodando ao mesmo tempo contra o mesmo banco**:
Spring na 8080 (build do commit da F2, num worktree do git) e Quarkus na 8086. Redis
(`redis:7.2`, porta 6395) e Kafka (`apache/kafka:3.9.0`, porta 9092) subidos pelo
`compose.yaml`.

O serviço Kafka do compose se chama `cryptomonitor-kafka`: já existia um container
parado chamado `kafka` na máquina (`cp-kafka:7.4.0`, de 3 meses atrás, com zookeeper),
e ele não foi tocado. Há também um `cryptomensageria-kafka-1` parado, que é
provavelmente quem consome o tópico `strategy` do outro lado.

**1. Contrato HTTP — idêntico.** Roteiro de 10 cenários (POST válido, GET sem filtro,
GET `ativo=true`, GET `ativo=false`, PUT status, GET depois de desativar, POST inválido,
POST com condições vazia, DELETE, GET final) rodado contra as duas versões com o banco
limpo antes de cada uma. `diff` byte a byte, normalizando id e timestamp: **nenhuma
divergência**.

**2. OpenAPI — idêntico** em rotas, métodos, nomes de parâmetro, `required` e códigos de
resposta. A única diferença que sobrou é uma correção: o Spring documentava `200` e `201`
no POST porque a anotação dizia 200 enquanto o `@ResponseStatus` devolvia 201. A versão
Quarkus documenta só `201`, que é o que sempre aconteceu de verdade.

**3. Cache Redis — idêntico e cruzado.** As duas versões gravam uma `string` com o mesmo
JSON, byte a byte. E o mais importante: cache **escrito pela versão Spring foi lido pela
Quarkus** na subida, sem reescrita (340 bytes intactos, nenhum aviso de "nenhuma
estratégia ativa"). O corte não invalida o cache em circulação.

**4. Kafka — publicação real verificada.** O gatilho de produção depende de fechamento de
candle na Bybit e não dá para forçar, então a publicação foi disparada por um teste
descartável (não commitado) chamando o `KafkaService` real contra o broker. Consumido do
tópico:

```
RSI   -> 🏆 Estratégia de RSI acionada para o ativo BTCUSDT valor RSI < 30 intervalo 60m
PRECO -> 💰 Estratégia de Preço acionada para o ativo BTCUSDT valor $70000
```

Tópico `strategy`, chave pelo nome do indicador, texto e emoji intactos — igual ao que os
testes das duas versões travam.

#### Divergência encontrada e corrigida: 500 no lugar de 400

Único defeito real que a fase achou. No Spring, `@RequestParam` sem default é obrigatório
e a chamada sem o parâmetro morria em **400** antes de chegar ao serviço. Em JAX-RS,
`@QueryParam` ausente vira `null` — então `DELETE /estrategia` e `PUT /estrategia/status`
sem parâmetros passavam `null` adiante e estouravam **500**.

Corrigido com `@NotNull` nos parâmetros, que joga a violação no mesmo `ExceptionMapper`.
O corpo é superconjunto do que o Spring devolvia (mesmos `timestamp`, `status`, `error`,
`path`, mais o `errors` dizendo qual parâmetro faltou), e o `required` do OpenAPI passou a
bater. Dois testes novos travam isso. **82 testes verdes.**

Vale registrar por que passou despercebido: nenhum teste da F0 chamava as rotas sem
parâmetro, porque no Spring aquilo nem chegava ao controller. Comparar as duas versões de
pé achou o que a suíte não achava.

#### Achado sem ação: ordem das mensagens de erro

O array `errors` sai em ordem diferente a cada chamada — **nas duas versões**. São 5
chamadas idênticas ao Spring produzindo 5 ordens distintas, e o mesmo no Quarkus. É
`Set<ConstraintViolation>`, sem ordem definida; não é regressão da migração e não foi
"corrigido" aqui. Fica o alerta: **o `TradeFront` não pode depender da ordem** — e não
podia antes também.

**Saída:** paridade demonstrada com evidência nos quatro canais (HTTP, OpenAPI, Redis,
Kafka). O corte pode acontecer.

### F6 — Gates de build — **CONCLUÍDA (21/08/2026)**

Mesma configuração do `trade/backend`, para os dois projetos cobrarem a mesma coisa.
Spotless e Checkstyle rodam na fase `validate`, então `mvn test` já os executa.

| Gate | Comando | O que trava |
|---|---|---|
| Formatação | `mvn spotless:check` | google-java-format AOSP. Conserta com `mvn spotless:apply` |
| Estilo | `mvn checkstyle:check` | `System.out`, `printStackTrace`, import não usado, método > 150 linhas, > 10 parâmetros, `if` sem chaves |
| Camadas | `ArquiteturaEmCamadasTest` | 7 regras do `PADROES_CODIGO.md` |

**Spotless.** Mesmas exceções do outro projeto e mesmo racional: sem formatar Javadoc e
sem `importOrder` próprio. Só **dois arquivos** estavam fora do formato — o resto do diff
é reflow do formatador.

**Checkstyle.** Ruleset copiado. As cinco violações apontadas eram `if` sem chaves,
herdadas verbatim do código Spring, e foram **corrigidas em vez de suprimidas**: o
`suppressions.xml` nasce vazio. Diferente do `trade/backend`, aqui não há dívida
congelada — a migração reescreveu o código todo e nada ficou devendo.

**ArchUnit.** Sete regras: Controller sem JPA, sem Repository e sem Entity;
`@Transactional` só em classe de serviço; Repository sem `jakarta.ws.rs`; Entity sem
conhecer camada de cima; e dependência sempre para dentro.

#### Armadilha: o engine do ArchUnit não funciona no JUnit 6

A primeira versão usava `archunit-junit5` com `@ArchTest`, igual ao `trade/backend`. O
build passava — **com zero regra executada**:

```
Tests run: 0, Failures: 0, Errors: 0 -- in ArquiteturaEmCamadasTest
```

O engine `archunit-junit5` mira o JUnit Platform **1.x**, e o Quarkus 3.33 já traz o
**6.0.3**, então o engine não é descoberto e os campos `@ArchTest` nunca viram teste.
Subir o ArchUnit de 1.3.0 para 1.4.1 não resolveu — nenhuma versão atual suporta a
plataforma 6.

Solução: usar a **API core** do ArchUnit (`com.tngtech.archunit:archunit`) dentro de
testes JUnit comuns, importando as classes com `ClassFileImporter` e chamando
`rule.check(...)`. Cada regra vira um `@Test` de verdade, reportado individualmente.

**O `trade/backend` está na mesma situação.** O `dependency:tree` do módulo `app` de lá
resolve exatamente a combinação que aqui não funcionou:

```
org.junit.platform:junit-platform-engine:jar:6.0.3:test
com.tngtech.archunit:archunit-junit5:jar:1.3.0:test
```

O `<junit.version>5.11.4</junit.version>` do pom fixa só o `junit-jupiter`; a plataforma
continua vindo do `quarkus-bom` em 6.0.3. Ou seja: o `LayeredArchitectureTest` e todo o
mecanismo de `archunit_store` daquele projeto provavelmente **não executam nada** — o gate
de camadas mais o congelamento de dívida, ambos silenciosamente inertes.

Não rodei o build de lá para confirmar na prática: o `CLAUDE.md` daquele projeto avisa que
um build reescreve o `archunit_store`, e mexer no working tree dele está fora do escopo
desta migração. Para conferir com segurança, basta olhar a linha `Tests run:` do
`LayeredArchitectureTest` no próximo build — se disser `0`, é o mesmo problema, e a
correção é a mesma: API core em vez do engine.

**Regras conferidas com violação injetada.** Um `EstrategiaRepository` e uma `Estrategia`
como campo do Controller quebraram três regras (`Controller nao deve depender de
Repository`, `Controller nao deve depender de Entity` e `Dependencia sempre para
dentro`). Gate que nunca falhou não é gate — este falha.

**Saída:** 89 testes verdes, três gates ativos, nenhuma dívida congelada.

### F7 — Limpeza — **CONCLUÍDA (21/08/2026)**

**Tamanho de classe: nada a fazer.** O limite de ~300 linhas do `PADROES_CODIGO.md` já
está satisfeito. Maior arquivo é `WebSocketClient` com 207 linhas; o
`EstrategiaServiceImpl`, que o plano mandava fatiar, tem 139. A remoção do Lombok fez os
arquivos crescerem em acessores explícitos e mesmo assim ninguém passou do limite.

**Código morto removido** — nenhum destes era chamado em produção:

| Item | Situação |
|---|---|
| `CryptoPriceDomain` | Record inteiro, sem uma única referência no projeto |
| `KafkaEnum.fromTopic` | Nunca chamado |
| `WebSocketConnectionManager.isConnected` | Nunca chamado |
| `MultiSymboPriceHandler.getPrices` | Declarado na interface e implementado, nunca usado |
| `KafkaService.sendMessage(String, String, Object)` | As estratégias sempre publicaram por `sendMessageEstrategias`; o método genérico só era exercitado pelo próprio teste. Levou junto `validateInputs` e `serialize`, que só existiam para ele |

O plano previa revisar o parâmetro `topic` desse método por ter "perdido a função" com o
canal fixo. Não perdeu — a F4 manteve o tópico dinâmico por metadata. O método inteiro é
que era morto.

**Cabeçalhos `Author`/`Date`** saíram de 10 arquivos: autoria e data são do git, e o
`PADROES_CODIGO.md` proíbe narrativa de data em comentário.

Resultado: 2.546 linhas de `main`, 87 testes verdes com os três gates ligados.

#### Achado: o `pom.xml` não tinha o perfil `native`

A primeira tentativa de build nativo terminou em `BUILD SUCCESS` **em 7,8 segundos** — o
que é impossível para uma compilação nativa. O `pom.xml` desta migração foi escrito à mão
e não tinha o perfil `native` que o gerador do Quarkus cria, então `-Dnative` na linha de
comando não ligava nada: o build seguia normal e terminava verde.

É a mesma classe de falha do ArchUnit na F6 — **o comando passa sem fazer o que se
pretendia**. Perfil adicionado ao `pom.xml`, com comentário dizendo exatamente isso.

#### Medição do consumo em JVM (base de comparação para o nativo)

| Configuração | Boot | RSS depois de servir requisições |
|---|---|---|
| Default (heap máx. 6 GB, 25% da RAM) | 4,30 s | **355 MB** |
| `-Xms64m -Xmx256m -XX:MaxMetaspaceSize=128m` | 4,32 s | **324 MB** |

Limitar o heap economiza só 31 MB: o peso **não está no heap**, e sim em metaspace, code
cache, threads e buffers nativos do Netty. Ou seja, a saída barata (mexer em `-Xmx`) não
resolve — se o objetivo for reduzir memória, o nativo é o caminho de verdade.

#### Build nativo: funciona, e mede-se assim

Compilado com Mandrel em container (`quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:jdk-25`).
Três tentativas até sair:

| Tentativa | `native-image-xmx` | Resultado |
|---|---|---|
| 1 | default | `exit 137` (OOM) em 5m33 |
| 2 | `10g` | `exit 137` — **159% da memória do sistema**, segundo o próprio log |
| 3 | `4g` | **BUILD SUCCESS** em 8m38, binário de 143,7 MB |

A causa das duas primeiras não era o app: **o Docker desta máquina só enxerga 5,59 GB**
(limite da VM do Docker Desktop), então pedir 10 GB garantia o SIGKILL. Quem for repetir
precisa ou subir o limite do Docker, ou manter `-Dquarkus.native.native-image-xmx` abaixo
dele.

**Medição, que é o critério que este plano exigia:**

| | Boot | RSS servindo requisições |
|---|---|---|
| JVM default (heap máx. 6 GB) | 4,30 s | 355 MB |
| JVM `-Xms64m -Xmx256m` | 4,32 s | 324 MB |
| **Nativo** | **1,73 s** | **168 MB** |

Metade da memória e boot 2,5x mais rápido. Para um processo que fica de pé o tempo todo
numa VPS, o que interessa é o RSS — e limitar o heap na JVM economiza só 31 MB, porque o
peso está em metaspace, code cache e buffers do Netty, não no heap.

**Os riscos que este plano previa não se confirmaram:** a `Java-WebSocket` conecta na
Bybit normalmente no nativo (`WebSocket Aberto` no log) e o Gson funciona. Liquibase roda
os 7 changesets, Kafka conecta, e as rotas respondem idênticas, incluindo o 400 do
parâmetro ausente.

**O risco que ninguém tinha previsto — e que falha calado:**

```
No serializer found for class EstrategiaCacheDTO ... This appears to be a native image,
in which case you may need to configure reflection for the class that is to be serialized
```

Os DTOs de cache são serializados **direto pelo `ObjectMapper`, fora do REST**, então o
Quarkus não descobre esses records sozinho na compilação nativa. E o erro cai no `catch`
do `RedisServiceImpl`: no nativo o cache **nunca populava, em silêncio**, e toda leitura
ia ao banco. Só apareceu porque o Redis foi conferido depois de exercitar as rotas, em vez
de tomar o `200 OK` como prova.

Corrigido com `@RegisterForReflection` nos dois records e verificado num segundo build
nativo: `Estrategias ativas salvas no Redis: 1`, zero erro de serialização.

#### Corrida na atualização assíncrona do cache

Durante os testes do nativo apareceu uma vez:

```
GenericJDBCException: Could not extract column [15] from JDBC ResultSet
[Este ResultSet está fechado.]
```

Aconteceu quando a tarefa de startup e a do `afterCommit` de um POST rodaram ao mesmo
tempo, em threads de executor diferentes. `atualizaEstrategiasWS` chamava o repositório
**fora de transação e fora de contexto de request**, e o `EntityManager` injetado não é
seguro entre threads nessa condição. O efeito é o pior possível: o cache é apagado pelo
`afterCommit` e a reescrita falha, deixando a chave vazia sem ninguém perceber.

**Não é defeito do nativo.** Não reproduziu em mais quatro tentativas — duas no nativo e
duas na JVM, incluindo POSTs concorrentes com o banco já populado. É corrida
intermitente, e o nativo só teve o azar de exibi-la.

Corrigido com `@Transactional` em `RedisServiceImpl.buscarEstrategiasAtivasRedis`, para
cada chamada assíncrona ganhar sessão própria. **Ressalva honesta:** como o erro não
reproduz sob demanda, a correção é justificada pelo mecanismo, não provada por um teste
que falhava antes e passa depois. O que dá para afirmar é que o caminho segue verde sob
concorrência (5 POSTs simultâneos, cache reescrito, zero erro de sessão).

#### Recomendação sobre o nativo

Funciona e o ganho é real, mas **não adotar agora**. O corte para Quarkus já é a mudança
grande desta rodada; trocar também o modo de execução dobraria a superfície de risco no
mesmo deploy. O binário compilado prova que o caminho está aberto — e o
`@RegisterForReflection` que ele exigiu já está no código, então a dívida técnica para
adotar depois é praticamente zero.

Quando for adotar, lembrar: o build nativo **não roda nos testes**. Um `@QuarkusIntegrationTest`
exercitando o cache seria o gate que teria pego o erro de reflexão antes do deploy.

**Saída da F7:** código morto removido, 87 testes verdes, build nativo avaliado com número
em vez de palpite, e dois defeitos corrigidos que só apareceram por rodar o app de verdade.


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
