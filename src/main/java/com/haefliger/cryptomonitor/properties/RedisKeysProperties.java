package com.haefliger.cryptomonitor.properties;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "redis.keys")
public interface RedisKeysProperties {

    String estrategiasAtivas();
}
