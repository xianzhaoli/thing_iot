package com.risky.server.MQTT.common.cache.redis.connection;

import com.risky.server.MQTT.client.ConnectClient;
import com.risky.server.MQTT.common.cache.redis.MqttRedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MqttConnectionClientCache extends MqttRedisCache<ConnectClient> {

    private final String CLIENT_KEY_REDIS = "SERVER:MQTT:CLIENT:";

    public void add(String clientId,ConnectClient value){
        redisTemplate.opsForValue().set(CLIENT_KEY_REDIS + clientId,value);
    }

    public void removeKey(String key) {
        delKey(CLIENT_KEY_REDIS + key);
    }


    public ConnectClient get(String clientId){
        return (ConnectClient) redisTemplate.opsForValue().get(CLIENT_KEY_REDIS + clientId);
    }
}
