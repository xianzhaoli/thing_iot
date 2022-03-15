package com.risky.server.MQTT.common.cache.redis.subscribe;

import com.risky.server.MQTT.common.cache.redis.MqttRedisCache;
import com.risky.server.MQTT.common.cache.redis.Topic;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class MqttSubScribeCache extends MqttRedisCache<Topic> {

    public static final String SUBSCRIBE_KEY_REDIS = "SERVER:MQTT:SUBSCRIBE:";

    /**
     * 添加topic订阅
     * @param topicName
     * @param clientId
     * @param topic
     * @return
     */
    public void subScribe(String topicName, String clientId, Topic topic){
       hset(SUBSCRIBE_KEY_REDIS + topicName,clientId,topic);
    }

    /**
     * 取消topic订阅
     * @param topicName
     * @param clientId
     */
    public void unSubScribe(String topicName,String clientId){
        hdel(SUBSCRIBE_KEY_REDIS + topicName,clientId);
    }


    public Map<String,Topic> entriesEntry(String topicName){
        return hentries(SUBSCRIBE_KEY_REDIS + topicName);
    }


}
