package com.risky.server.MQTT.common.cache.redis.subscribe;

import com.google.common.collect.Sets;
import com.risky.server.MQTT.common.cache.redis.MqttRedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
//@CacheConfig(cacheNames = "topic",cacheManager = "caffeineCacheManager")
public class MqttSubScribeCache extends MqttRedisCache<Topic> {

    private Set<Topic> topics = Sets.newConcurrentHashSet();

    private Map<String, Set<String>> subScribeTopics = new ConcurrentHashMap<>();


    public static final String SUBSCRIBE_KEY_REDIS = "SERVER:MQTT:SUBSCRIBE:";

    /*@PostConstruct
    public void initTopic(){
        topics = scan(MqttSubScribeCache.SUBSCRIBE_KEY_REDIS);
    }*/

    /**
     * 添加topic订阅
     * @param topic
     * @return
     */
    public boolean subScribe(Topic topic){
        if(topics.contains(topic)){
            return false;
        }
        if(!subScribeTopics.containsKey(topic.getClientId())){
            subScribeTopics.put(topic.getClientId(),Sets.newConcurrentHashSet());
        }
        subScribeTopics.get(topic.getClientId()).add(topic.getTopic());
        return topics.add(topic);
    }

    /**
     * 通过clientId 批量取消订阅
     * @param clientId
     * @return
     */
    public boolean unSubScribe(String clientId){
        if(subScribeTopics.containsKey(clientId)){
            for (String topicName : subScribeTopics.get(clientId)) {
                topics.remove(new Topic(topicName,clientId));
            }
            subScribeTopics.remove(clientId);
        }
        return !subScribeTopics.containsKey(clientId);
    }

    /**
     * 取消topic订阅
     * @param topicName
     * @param clientId
     */
    public boolean unSubScribe(String topicName,String clientId){
        return topics.remove(new Topic(topicName,clientId));
    }

    @Cacheable(key = "#topicName")
    public Map<String,Topic> entriesEntry(String topicName){
        return hentries(SUBSCRIBE_KEY_REDIS + topicName);
    }

    /**
     * 获取所有匹配的订阅客户端
     * @param topicName
     * @return
     */
    public Set<Topic> matcherTopic(String topicName){
        return topics.stream()
                     .filter(topic -> topic.getMqttTopicFilter().filter(topicName))
                     .collect(Collectors.toSet());
    }

}
