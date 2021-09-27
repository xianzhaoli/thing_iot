package com.risky.server.MQTT.protocol;

import com.risky.server.MQTT.common.MqttStoreService;
import com.risky.server.MQTT.message.MessageService;
import com.risky.server.MQTT.message.RedisMessagePersistent;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.util.AttributeKey;

public class PubComp {

    private MqttStoreService mqttStoreService;

    private MessageService messageService;

    private RedisMessagePersistent redisMessagePersistent;

    public PubComp(MqttStoreService mqttStoreService, MessageService messageService, RedisMessagePersistent redisMessagePersistent) {
        this.mqttStoreService = mqttStoreService;
        this.messageService = messageService;
        this.redisMessagePersistent = redisMessagePersistent;
    }

    public void processPubComp(Channel channel, MqttMessageIdVariableHeader mqttMessageIdVariableHeader){
        int messageId = mqttMessageIdVariableHeader.messageId();
        String clientID = (String)channel.attr(AttributeKey.valueOf("clientId")).get();

        redisMessagePersistent.removePubRelMessage(clientID,messageId);
    }

}
