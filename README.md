# CryptoMonitor

## Documentation Swagger UI

http://localhost:8080/crypto-monitor/swagger-ui/index.html


## Adicionar Novas Estratégias

Sempre que precisar adicionar uma nova estratégia deve seguir os seguintes passos:

1. Criar o enum na classe *TipoIndicadorEnum*;
2. Criar a classe para estratégia no diretório *com.haefliger.cryptomonitor.strategy*;
3. Criar uma nova instância para a estratégia na classe *SimboloMonitoradoFactory*.
