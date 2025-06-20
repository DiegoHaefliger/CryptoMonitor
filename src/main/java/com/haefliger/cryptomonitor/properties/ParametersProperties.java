package com.haefliger.cryptomonitor.properties;


import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Author diego-haefliger
 * Date 12/06/25
 */

@Configuration
@NoArgsConstructor
@Getter
public class ParametersProperties {

    @Value("${kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    @Value("${spring.redis.keys.estrategias.ativas}")
    private String redisEstrategiasAtivasKey;

}
