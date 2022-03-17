package com.risky.server.MQTT.common.cache.redis.publish;

import com.risky.server.MQTT.common.cache.redis.MqttRedisCache;
import com.risky.server.MQTT.common.cache.redis.subscribe.Topic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MqttPublishCache extends MqttRedisCache<Topic> {

    /*private final static String PUBLISH_KEY_REDIS = "SERVER:MQTT:PUBLISH:";

    public Set<String> matcherTopic(String topic){
        Set<String> topics = scan(MqttSubScribeCache.SUBSCRIBE_KEY_REDIS);
        return filterChannel(topic,topics);
    }*/



}
