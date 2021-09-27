package com.risky.server.MQTT.protocol;

import com.risky.server.MQTT.common.MqttStoreService;
import com.risky.server.MQTT.message.MessageId;
import com.risky.server.MQTT.message.MessageService;
import com.risky.server.MQTT.message.RedisMessagePersistent;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PubRec {

    private MqttStoreService mqttStoreService;

    private MessageService messageService;

    private RedisMessagePersistent redisMessagePersistent;

    public PubRec(MqttStoreService mqttStoreService, MessageService messageService, RedisMessagePersistent redisMessagePersistent) {
        this.mqttStoreService = mqttStoreService;
        this.messageService = messageService;
        this.redisMessagePersistent = redisMessagePersistent;
    }

    /**
     * 服务端发送QOS = 2 消息时，客户端回复PubRec,服务端回复PubRel
     * @param channel
     * @param mqttMessageIdVariableHeader
     */
    public void processPubRec(Channel channel , MqttMessageIdVariableHeader mqttMessageIdVariableHeader){
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        MqttMessage mqttMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREL,false,MqttQoS.AT_MOST_ONCE,false,0),
                MqttMessageIdVariableHeader.from(mqttMessageIdVariableHeader.messageId()),null
                );
        log.info("PUBREC - clientId: {}, messageId: {}", clientId, mqttMessageIdVariableHeader.messageId());
        messageService.releaseMessageId(clientId,mqttMessageIdVariableHeader.messageId()); //解锁messageId;

        redisMessagePersistent.putPubRelMessage(clientId,new MessageId(mqttMessageIdVariableHeader.messageId(),System.currentTimeMillis(),clientId)); //保存rec 消息ID
        channel.writeAndFlush(mqttMessage);
    }

}
