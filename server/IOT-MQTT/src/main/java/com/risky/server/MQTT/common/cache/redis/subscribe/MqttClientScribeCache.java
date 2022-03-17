package com.risky.server.MQTT.common.cache.redis.subscribe;

import com.risky.server.MQTT.common.cache.redis.MqttRedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class MqttClientScribeCache extends MqttRedisCache<Topic> {

    public final static String PUBLISH_KEY_REDIS = "SERVER:MQTT:CLIENT_BIND_TOPIC:";

    public void bindClientTopic(String clientId,Topic topic){
        hset(PUBLISH_KEY_REDIS + clientId,topic.getTopic(),topic);
    }

    public boolean contains(String clientId,String topicName){
        return hHas(clientId,topicName);
    }

    public void unBindClientTopic(String clientId,String topicName){
        hdel(PUBLISH_KEY_REDIS + clientId,topicName);
    }

    public Map<String,Topic> entriesEntry(String clientId){
        return hentries(PUBLISH_KEY_REDIS + clientId);
    }

    public void removeKey(String key) {
        delKey(PUBLISH_KEY_REDIS + key);
    }
}
