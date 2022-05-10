package com.risky.server.MQTT.common.thread;

import com.risky.server.MQTT.common.cache.redis.subscribe.SubscribeClient;
import com.risky.server.MQTT.common.MqttStoreService;
import com.risky.server.MQTT.common.cache.redis.connection.MqttConnectionClientCache;
import com.risky.server.MQTT.common.store.SessionStoreMessage;
import com.risky.server.MQTT.common.store.batch.BatchQueue;
import com.risky.server.MQTT.common.store.batch.BatchStoreWorker;
import com.risky.server.MQTT.message.MessageService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 异步工作线程池
 */
@Component
public class AsyncWorkerPool {


    ThreadPoolExecutor executor = new ThreadPoolExecutor(1,4,60,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(10));



    @Autowired
    private MqttConnectionClientCache mqttConnectionClientCache;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MqttStoreService mqttStoreService;

    @Resource
    private BatchQueue batchQueue;

    public void storeCleanSessionMessage(SessionStoreMessage sessionStoreMessage){
        //异步写入mongodb
        batchQueue.addToQueue(sessionStoreMessage);
    }

    public void sendOffLineMessage(String clientId,String topic){
        //发送离线消息
        Map<String, List<SessionStoreMessage>> offlineMessage = mqttConnectionClientCache.getStoreMessage(clientId,topic);
        Channel subscribeChannel = mqttStoreService.getChannelByClientId(clientId);

        if(!offlineMessage.isEmpty()){
            executor.execute(new Woker(() -> offlineMessage.entrySet().parallelStream().forEach(stringListEntry -> stringListEntry.getValue().parallelStream().forEach(sessionStoreMessage -> {
                messageService.publishMessage(new SubscribeClient(MqttQoS.valueOf(sessionStoreMessage.getQos()),
                                sessionStoreMessage.getClientId(),stringListEntry.getKey(),false),
                        stringListEntry.getKey(),sessionStoreMessage.getPayload(),subscribeChannel,false);
            }))));
        }
    }

}
