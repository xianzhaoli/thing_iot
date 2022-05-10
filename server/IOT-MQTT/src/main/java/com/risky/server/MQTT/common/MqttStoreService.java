package com.risky.server.MQTT.common;

import com.risky.server.MQTT.common.cache.redis.subscribe.SubscribeClient;
import com.risky.server.MQTT.common.cache.redis.connection.MqttConnectionClientCache;
import com.risky.server.MQTT.common.cache.redis.publish.MqttPublishCache;
import com.risky.server.MQTT.common.cache.redis.retain.MqttRetainCache;
import com.risky.server.MQTT.common.cache.redis.subscribe.MqttClientScribeCache;
import com.risky.server.MQTT.common.cache.redis.subscribe.MqttSubScribeCache;
import com.risky.server.MQTT.common.thread.AsyncWorkerPool;
import com.risky.server.MQTT.config.ConnectClient;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/24 下午10:12
 * @description：保存客户端信息，订阅信息等
 * @modified By：`
 * @version: 1.0
 */
@Component
@Slf4j
public class MqttStoreService {

    private Map<String, Channel> clientIDChannel = new ConcurrentHashMap<>(100);

    private Map<Channel, String> channelClientID = new ConcurrentHashMap<>(100);

    private Map<String, List<SubscribeClient>> subByTopic = new ConcurrentHashMap<>(50);

    private Map<String, List<SubscribeClient>> clientIdSubList = new ConcurrentHashMap<>(50);

    @Autowired
    public MqttSubScribeCache mqttSubScribeCache;

    @Autowired
    public MqttConnectionClientCache mqttConnectionClientCache;

    @Autowired
    public MqttClientScribeCache mqttClientScribeCache;

    @Autowired
    public MqttPublishCache mqttPublishCache;

    @Autowired
    public AsyncWorkerPool asyncWorkerPool;

    @Autowired
    public MqttRetainCache mqttRetainCache;

    /**
     * 当前的客户端数
     * @return
     */
    public Integer acitveChannlSize(){
        return channelClientID.size();
    }

    public ConnectClient getClientInfo(String clientId){
        return mqttConnectionClientCache.get(clientId);
    }

    public boolean binding(String clientId, Channel channel, ConnectClient connectClient){
        if(clientIDChannel.containsKey(clientId)){
            Channel oldChannel =  clientIDChannel.get(clientId);
            boolean isOpen = oldChannel.isOpen();
             mqttSubScribeCache.unSubScribe(clientId);
            mqttClientScribeCache.removeKey(clientId);
            mqttConnectionClientCache.removeKey(clientId);
            clearClientSubscribeTopic(clientId);
            if(isOpen & oldChannel.equals(channel)){
                //此处参考[MQTT-3.1.0-2]
                //在一个网络连接上，客户端只能发送一次CONNECT报文。
                //服务端必须将客户端发送的第二个CONNECT报文当作协议违规处理并断开客户端的连接。
                if(oldChannel != null & oldChannel.equals(channel)){
                    log.info("客户端发送两次connection报文，断开连接{}",clientId);
                    channel.close();
                    unbinding(clientId,channel);
                    return false;
                }
            }
            if(isOpen){
                oldChannel.close();
                log.info("同一个clientID被两个客户端使用，断开旧连接{}",clientId);
            }
            channelClientID.remove(oldChannel);
        }
        mqttConnectionClientCache.add(clientId,connectClient);
        clientIDChannel.put(clientId,channel);
        channelClientID.put(channel,clientId);
        log.info("新的客户端连接[{}],当前在线连接数{}",clientId,clientIDChannel.size());
        return true;
    }

    public void unbinding(String clientID, Channel channel){
        channelClientID.remove(channel);
        clientIDChannel.remove(clientID);
    }


    public Channel getChannelByClientId(String clientId){
        return clientIDChannel.get(clientId);
    }


    public SubscribeClient bindSubscribeChannel(String topic, MqttQoS mqttQoS, String clientId,boolean cleanSession){
        if(!subByTopic.containsKey(topic)){
            subByTopic.put(topic,new CopyOnWriteArrayList<>());
        }
        SubscribeClient subscribeClient = new SubscribeClient(mqttQoS,clientId,topic,cleanSession);
        if(subByTopic.get(topic).contains(subscribeClient)){
            return null;
        }
        subByTopic.get(topic).add(subscribeClient);

        /** 方便维护 **/
        if(!clientIdSubList.containsKey(clientId)){
            clientIdSubList.put(clientId,new CopyOnWriteArrayList<>());
        }
        clientIdSubList.get(clientId).add(subscribeClient);
        return subscribeClient;
    }

    /**
     * 删除
     * @param topic
     * @param clientId
     */
    public void unbindSubscribeChannel(String topic,String clientId){
        clientIdSubList.get(clientId).forEach(subscribeClient -> {
            if(subscribeClient.getTopic().equals(topic) & subByTopic.containsKey(subscribeClient.getTopic())){
                subByTopic.get(topic).remove(subscribeClient);
                clientIdSubList.get(clientId).remove(subscribeClient);
                if(subByTopic.get(subscribeClient.getTopic()).size() == 0){
                    subByTopic.remove(topic);
                }
            }
        });
        if(clientIdSubList.get(clientId).size() == 0){
            clientIdSubList.remove(clientId);
        }
    }

    /**
     * 获取已订阅的客户端
     * @param topic 发送消息方的topic
     * @return
     */
    public Set<SubscribeClient> filterChannel(String topic){
        Map<String, List<SubscribeClient>> subscribeClients = subByTopic.entrySet().parallelStream()
                .filter(key -> topic.equals(key.getKey()) | topicMatcher(topic,key.getKey()))
                .collect(Collectors.toMap(p -> p.getKey(),p ->p.getValue()));
        Set<SubscribeClient> set = new HashSet();
        subscribeClients.forEach((k,v) ->{
            set.addAll(v);
        });
        return set;
    }

    /**
     * topic 匹配，发消息 topic 匹配到所有的channel
     * @param sendTopic
     * @param topicGroup
     * @return
     */
    public boolean topicMatcher(String sendTopic,String topicGroup){
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


    public void clearClientSubscribeTopic(String clientId){
        if(clientIdSubList.containsKey(clientId)){
            clientIdSubList.get(clientId).parallelStream().forEach(subscribeClient -> {
                subByTopic.get(subscribeClient.getTopic()).remove(subscribeClient);
                log.info("清除客户端:{},订阅的topic:{}",clientId,subscribeClient.getTopic());
            });
            clientIdSubList.remove(clientId);
        }
    }
}
