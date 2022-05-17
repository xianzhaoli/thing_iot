package com.risky.server.MQTT.protocol;

import com.risky.server.MQTT.common.MqttStoreService;
import com.risky.server.MQTT.message.MessageService;
import com.risky.server.MQTT.message.RedisMessagePersistent;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.util.AttributeKey;

/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/25 下午11:45
 * @description：消息回执
 * @modified By：`
 * @version: 1.0
 */
public class PubAck {

    private MqttStoreService mqttStoreService;

    private MessageService messageService;

    private RedisMessagePersistent redisMessagePersistent;


    public PubAck(MqttStoreService mqttStoreService, MessageService messageService, RedisMessagePersistent redisMessagePersistent) {
        this.mqttStoreService = mqttStoreService;
        this.messageService = messageService;
        this.redisMessagePersistent = redisMessagePersistent;
    }

    public void sendPubAckMessage(Channel channel, MqttPubAckMessage mqttPubAckMessage){
        int messageId = mqttPubAckMessage.variableHeader().messageId();
        final String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        //messageService.releaseMessageId(clientId,messageId);
        redisMessagePersistent.removeRetryMessage(clientId,messageId);
    }

}
