package com.risky.server.MQTT.common.cache.redis.subscribe;

import com.google.common.collect.Sets;
import com.risky.server.MQTT.common.cache.redis.MqttRedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
@CacheConfig(cacheNames = "topic")
public class MqttSubScribeCache extends MqttRedisCache<Topic> {

    /*@Autowired
    private TopicCache topicCache;*/

    private Set<String> topics = Sets.newConcurrentHashSet();

    public static final String SUBSCRIBE_KEY_REDIS = "SERVER:MQTT:SUBSCRIBE:";

    @PostConstruct
    public void initTopic(){
        topics = scan(MqttSubScribeCache.SUBSCRIBE_KEY_REDIS);
    }

    public Set<String> matcherTopic(String topic){
        //Set<String> topics = scan(MqttSubScribeCache.SUBSCRIBE_KEY_REDIS);
        return filterChannel(topic,topics);
    }




    /**
     * 添加topic订阅
     * @param topicName
     * @param clientId
     * @param topic
     * @return
     */
    @CachePut(key = "#topicName")
    public Map<String,Topic> subScribe(String topicName, String clientId, Topic topic){
       hset(SUBSCRIBE_KEY_REDIS + topicName,clientId,topic);
       topics.add(topicName);
       return hentries(SUBSCRIBE_KEY_REDIS + topicName);
    }

    /**
     * 取消topic订阅
     * @param topicName
     * @param clientId
     */
    @CachePut(key = "#topicName")
    public Map<String,Topic> unSubScribe(String topicName,String clientId){
        hdel(SUBSCRIBE_KEY_REDIS + topicName,clientId);
        return hentries(SUBSCRIBE_KEY_REDIS + topicName);
    }

    @Cacheable(key = "#topicName")
    public Map<String,Topic> entriesEntry(String topicName){
        return hentries(SUBSCRIBE_KEY_REDIS + topicName);
    }


}
