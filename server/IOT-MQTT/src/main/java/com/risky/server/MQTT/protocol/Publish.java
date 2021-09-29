package com.risky.server.MQTT.protocol;

import cn.hutool.core.util.StrUtil;
import com.risky.server.MQTT.client.SubscribeClient;
import com.risky.server.MQTT.common.MqttStoreService;
import com.risky.server.MQTT.message.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/25 下午2:03
 * @description：收到消息时处理
 * @modified By：`
 * @version: 1.0
 */
@Slf4j
public class Publish {

    private MqttStoreService mqttStoreService;

    private MessageService messageService;

    private RedisMessagePersistent redisMessagePersistent;

    private RetainMessage retainMessage;

    public Publish(MqttStoreService mqttStoreService, MessageService messageService, RedisMessagePersistent redisMessagePersistent, RetainMessage retainMessage) {
        this.mqttStoreService = mqttStoreService;
        this.messageService = messageService;
        this.redisMessagePersistent = redisMessagePersistent;
        this.retainMessage = retainMessage;
    }

    public void sendPublishMessage(Channel channel, MqttPublishMessage mqttPublishMessage){
        ByteBuf byteBuf = mqttPublishMessage.payload();
        byte[] payLoad = new byte[byteBuf.readableBytes()];
        int index = byteBuf.readerIndex();
        byteBuf.getBytes(index,payLoad);

        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        log.info("客户端:{},topic:{},QOS:[{}],发布消息:{}",clientId,mqttPublishMessage.variableHeader().topicName(),
                            mqttPublishMessage.fixedHeader().qosLevel().value(), StrUtil.str(payLoad,"UTF-8"));
        switch (mqttPublishMessage.fixedHeader().qosLevel()){
            case AT_LEAST_ONCE:
                sendQos1Message(channel,mqttPublishMessage);
                break;
            case EXACTLY_ONCE:
                sendQos2Message(channel,mqttPublishMessage);
                break;
            default:
                break;
        }

        //保留消息
        if(mqttPublishMessage.fixedHeader().isRetain()){
            retainMessage.putRetainMessage(mqttPublishMessage.variableHeader().topicName(),
                    new MessageRetry(payLoad,clientId,mqttPublishMessage.variableHeader().topicName(),mqttPublishMessage.fixedHeader().qosLevel().value(),null));
        }

        //发送消息
        Set<SubscribeClient> subscribeClients = mqttStoreService.filterChannel(mqttPublishMessage.variableHeader().topicName());
        if(!subscribeClients.isEmpty()){
            subscribeClients.parallelStream().forEach(subscribeClient-> {
                //消息等级由订阅方决定
                Channel subscribeChannel = mqttStoreService.getChannelByClientId(subscribeClient.getClientId());
                if(subscribeChannel != null){
                    messageService.publishMessage(subscribeClient,mqttPublishMessage.variableHeader().topicName(),payLoad,subscribeChannel);
                }else if(!subscribeClient.isCleanSession()){
                    redisMessagePersistent.putCleanSessionMessage(subscribeClient.getClientId(),new MessageRetry(payLoad,subscribeClient.getClientId(),
                                                                  mqttPublishMessage.variableHeader().topicName(),subscribeClient.getMqttQoS().value(),null));
                }
            });
        }
    }


    private void sendQos1Message(Channel channel,MqttPublishMessage mqttPublishMessage){
        MqttPubAckMessage pubAckMessage = (MqttPubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBACK,false,MqttQoS.AT_MOST_ONCE,false,0)
                ,MqttMessageIdVariableHeader.from(mqttPublishMessage.variableHeader().packetId()),null
        );
        channel.writeAndFlush(pubAckMessage);

    }
    private void sendQos2Message(Channel channel,MqttPublishMessage mqttPublishMessage){
        MqttMessage pubRecMessage =  MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREC,false,MqttQoS.AT_MOST_ONCE,false,0)
                ,MqttMessageIdVariableHeader.from(mqttPublishMessage.variableHeader().packetId()),null
        );
        channel.writeAndFlush(pubRecMessage);
    }

}
