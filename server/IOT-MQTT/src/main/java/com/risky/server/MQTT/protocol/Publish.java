package com.risky.server.MQTT.protocol;

import com.risky.server.MQTT.client.SubscribeClient;
import com.risky.server.MQTT.common.MqttStoreService;
import com.risky.server.MQTT.message.MessageService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;

import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/25 下午2:03
 * @description：收到消息时处理
 * @modified By：`
 * @version: 1.0
 */
public class Publish {

    private MqttStoreService mqttStoreService;

    private MessageService messageService;


    public Publish(MqttStoreService mqttStoreService, MessageService messageService) {
        this.mqttStoreService = mqttStoreService;
        this.messageService = messageService;
    }

    public void sendPublishMessage(Channel channel, MqttPublishMessage mqttPublishMessage){
        ByteBuf byteBuf = mqttPublishMessage.payload();
        byte[] payLoad = new byte[byteBuf.readableBytes()];
        int index = byteBuf.readerIndex();
        byteBuf.getBytes(index,payLoad);
        try {
            System.out.println("topic:" +mqttPublishMessage.variableHeader().topicName()+","+new String(payLoad,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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
        //发送消息
        Set<SubscribeClient> subscribeClients = mqttStoreService.filterChannel(mqttPublishMessage.variableHeader().topicName());
        subscribeClients.forEach(subscribeClient-> {
            //消息等级由订阅方决定
            switch (subscribeClient.getMqttQoS()) {
                case AT_MOST_ONCE:
                    MqttPublishMessage mqttPublishMessageQOS0 = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH,false,MqttQoS.AT_MOST_ONCE,false,0)
                            ,new MqttPublishVariableHeader(mqttPublishMessage.variableHeader().topicName(),0), Unpooled.buffer().writeBytes(payLoad)
                    );
                    mqttStoreService.getChannelByClientId(subscribeClient.getClientId()).writeAndFlush(mqttPublishMessageQOS0);
                    break;
                case EXACTLY_ONCE:
                    int messageIdQos2 = messageService.getMessageId(subscribeClient.getClientId());
                    MqttPublishMessage mqttPublishMessageQOS2 = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH,false,MqttQoS.EXACTLY_ONCE,false,0)
                            ,new MqttPublishVariableHeader(mqttPublishMessage.variableHeader().topicName(),messageIdQos2), Unpooled.buffer().writeBytes(payLoad)
                    );
                    mqttStoreService.getChannelByClientId(subscribeClient.getClientId()).writeAndFlush(mqttPublishMessageQOS2);
                    break;
                case AT_LEAST_ONCE:
                    int messageIdQos1 = messageService.getMessageId(subscribeClient.getClientId());
                    MqttPublishMessage mqttPublishMessageQOS1 = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH,false,MqttQoS.AT_LEAST_ONCE,false,0)
                            ,new MqttPublishVariableHeader(mqttPublishMessage.variableHeader().topicName(),messageIdQos1), Unpooled.buffer().writeBytes(payLoad)
                    );
                    mqttStoreService.getChannelByClientId(subscribeClient.getClientId()).writeAndFlush(mqttPublishMessageQOS1);
                    break;
                default:
                    break;
            }

        });

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

    public static void main(String[] args) {
        System.out.println("bbb");
    }

}
