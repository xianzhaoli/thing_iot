package com.risky.server.MQTT.common.cache.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MqttRedisCache<T> {

    @Resource(name = "redisCacheTemplate")
    public RedisTemplate redisTemplate;

    protected void delKey(String key){
        if(!redisTemplate.delete(key)){
            log.error("删除客户端缓存key{}失败", key);
        }else{
            log.trace("删除客户端缓存key{}成功", key);
        }
    }


    public Set<String> hkeys(String key){
        return redisTemplate.opsForHash().keys(key);
    }

    public T hGet(final String key,final String hashKey){
        return (T) redisTemplate.opsForHash().get(key,hashKey);
    }

    public Map<String,T> hentries(final String key){
        return redisTemplate.opsForHash().entries(key);
    }

    public void hset(final String key ,final String hashKey,T value){
        redisTemplate.opsForHash().put(key,hashKey,value);
    }

    protected boolean hHas(final String key,final String hasKey){
       return redisTemplate.opsForHash().hasKey(key,hasKey);
    }

    public void hdel(final String key,final String... hashKey){
        redisTemplate.opsForHash().delete(key,hashKey);
    }

    protected Set<String> scan(final String match){
        long start = System.currentTimeMillis();
        Set<String> keys = (Set<String>) redisTemplate.execute((RedisCallback) redisConnection -> {
            Set<String> set = new HashSet<>();
            Cursor<byte[]> cursor = redisConnection.scan(new ScanOptions.ScanOptionsBuilder().match(match + "*").count(1000).build());
            while (cursor.hasNext()) {
                set.add(new String(cursor.next()));
            }
            return set;
        });
        long end = System.currentTimeMillis();
        //log.info("scan 耗时{}ms",end-start);
        return keys;
    }

    /**
     * 获取到匹配的key
     * @param topic 发送消息方的topic
     * @param exitsTopic 已存在的topic集合
     * @return
     */
    public Set<String> filterChannel(String topic,Set<String> exitsTopic){
        return exitsTopic.parallelStream()
                .filter(key -> topic.equals(key) | topicMatcher(topic,key))
                .collect(Collectors.toSet());
    }

    /**
     * 获取到匹配的retain消息
     * @param topic 订阅的topic
     * @param exitsTopic 已存在的retain topic集合
     * @return
     */
    public Set<String> filterTetainTopic(String topic,Set<String> exitsTopic){
        return exitsTopic.parallelStream()
                .filter(key -> topic.equals(key) | topicMatcher(key,topic))
                .collect(Collectors.toSet());
    }

    /**
     * topic 匹配，发消息 topic 匹配到所有的channel
     * @param sendTopic
     * @param topicGroup
     * @return
     */
    private boolean topicMatcher(String sendTopic,String topicGroup){
        String[] sendArray = sendTopic.split("/");
        String[] groupArray = topicGroup.split("/");
        int sendLength = sendArray.length;
        for (int i = 0; i < groupArray.length; i++) {
            if(eqWell(groupArray[i])){
                return true;
            }
            if(i > sendLength-1){
                return false;
            }

            if(!groupArray[i].equals(sendArray[i]) & !eqPlus(groupArray[i])){
                return false;
            }
        }
        return groupArray.length == sendLength;
    }

    private boolean eqPlus(String str){
        return "+".equals(str);
    }

    private boolean eqWell(String str){
        return "#".equals(str);
    }
}
