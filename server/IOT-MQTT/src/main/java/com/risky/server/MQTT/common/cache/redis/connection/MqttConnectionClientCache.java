package com.risky.server.MQTT.common.cache.redis.connection;

import com.risky.server.MQTT.client.WillMessage;
import com.risky.server.MQTT.common.cache.redis.MqttRedisCache;
import com.risky.server.MQTT.common.store.SessionStoreMessage;
import com.risky.server.MQTT.config.ConnectClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@CacheConfig(cacheManager ="caffeineCacheManager",cacheNames = "connection_client")
public class MqttConnectionClientCache {

    private final String CLIENT_KEY_REDIS = "SERVER:MQTT:CLIENT:";

    @Resource(name = "redisCacheTemplate")
    public RedisTemplate redisTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @CachePut(key = "#clientId")
    public ConnectClient add(String clientId, ConnectClient value){
        redisTemplate.opsForValue().set(CLIENT_KEY_REDIS + clientId,value);
        return value;
    }
    @CacheEvict(key = "#key")
    public void removeKey(String key) {
        redisTemplate.delete(CLIENT_KEY_REDIS + key);
    }

    @Cacheable(key = "#clientId")
    public ConnectClient get(String clientId){
        return (ConnectClient) redisTemplate.opsForValue().get(CLIENT_KEY_REDIS + clientId);
    }

    public Map<String,List<SessionStoreMessage>> getStoreMessage(String clientId,String topic){
        Criteria criteria = new Criteria("clientId").is(clientId);
        if(StringUtils.isNotBlank(topic)){
            criteria.and("topic").is(topic);
        }
        Query query = new Query();
        List<SessionStoreMessage> sessionStoreMessages = mongoTemplate.find(query,SessionStoreMessage.class);
        if(sessionStoreMessages != null){
            mongoTemplate.remove(query,SessionStoreMessage.class);
        }
        return sessionStoreMessages == null ? Collections.emptyMap() :
                sessionStoreMessages.parallelStream()
                .collect(Collectors.groupingBy( d -> (d.getTopic())));
    }


}
