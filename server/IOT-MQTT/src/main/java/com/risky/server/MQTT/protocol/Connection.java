package com.risky.server.MQTT.protocol;

import com.risky.server.MQTT.client.WillMessage;
import com.risky.server.MQTT.common.MqttStoreService;
import com.risky.server.MQTT.common.cache.redis.connection.MqttConnectionClientCache;
import com.risky.server.MQTT.config.ConnectClient;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;

/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/25 下午1:42
 * @description：连接处理方法
 * @modified By：`
 * @version: 1.0
 */
public class Connection {

    private MqttStoreService mqttStoreService;

    public Connection(MqttStoreService mqttStoreService) {
        this.mqttStoreService = mqttStoreService;
    }

    public void sendConnAckMessage(Channel channel , MqttConnectMessage mqttConnectMessage){
        MqttConnAckMessage mqttConnAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.CONNACK,false,MqttQoS.AT_MOST_ONCE,false,0)
                ,new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED,true),null);
        mqttStoreService.mqttClientScribeCache.entriesEntry(mqttConnectMessage.payload().clientIdentifier()).entrySet().parallelStream()
                .forEach(stringTopicEntry -> mqttStoreService.mqttSubScribeCache.unSubScribe(stringTopicEntry.getKey(),mqttConnectMessage.payload().clientIdentifier()));
        mqttStoreService.mqttClientScribeCache.removeKey(mqttConnectMessage.payload().clientIdentifier());
        mqttStoreService.mqttConnectionClientCache.removeKey(mqttConnectMessage.payload().clientIdentifier());
        mqttStoreService.clearClientSubscribeTopic(mqttConnectMessage.payload().clientIdentifier());
        channel.writeAndFlush(mqttConnAckMessage);
        boolean cleanSession = mqttConnectMessage.variableHeader().isCleanSession();
        WillMessage willMessage1 = null;
        if(mqttConnectMessage.variableHeader().isWillFlag()){
            willMessage1 = new WillMessage(mqttConnectMessage.variableHeader().willQos(),mqttConnectMessage.payload().willTopic(),
                    mqttConnectMessage.payload().willMessageInBytes(),mqttConnectMessage.variableHeader().isWillRetain());
        }
        ConnectClient connectClient = ConnectClient.builder()
                .clientId(mqttConnectMessage.payload().clientIdentifier())
                .cleanSession(cleanSession)
                .password(mqttConnectMessage.payload().passwordInBytes())
                .username(mqttConnectMessage.payload().userName())
                .willFlag(mqttConnectMessage.variableHeader().isWillFlag())
                .willMessage(willMessage1)
                .build();
        boolean success = mqttStoreService.binding(mqttConnectMessage.payload().clientIdentifier(),channel,connectClient);

        if(success){
            //离线消息
            mqttStoreService.asyncWorkerPool.sendOffLineMessage(mqttConnectMessage.payload().clientIdentifier(),null);

            int keepAliveSeconds = mqttConnectMessage.variableHeader().keepAliveTimeSeconds();
            channel.attr(AttributeKey.valueOf("clientId")).set(mqttConnectMessage.payload().clientIdentifier()); //连接clientId
            channel.attr(AttributeKey.valueOf("username")).set(mqttConnectMessage.payload().userName()); //连接用户名
            channel.attr(AttributeKey.valueOf("cleanSession")).set(cleanSession); //是否清除会话
            channel.attr(AttributeKey.valueOf("keepAliveSeconds")).set(keepAliveSeconds); //心跳间隔时长
            //mqttStoreService.mqttConnectionClientCache.add(connectClient.getClientId(),connectClient);
            //处理遗嘱信息
            if (mqttConnectMessage.variableHeader().isWillFlag()){
                MqttPublishMessage willMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH,false, MqttQoS.valueOf(mqttConnectMessage.variableHeader().willQos())
                                ,mqttConnectMessage.variableHeader().isWillRetain(),0),
                        new MqttPublishVariableHeader(mqttConnectMessage.payload().willTopic(),0),
                        Unpooled.buffer().writeBytes(mqttConnectMessage.payload().willMessageInBytes())
                );
                channel.attr(AttributeKey.valueOf("willMessage")).set(willMessage);
            }
            if (channel.pipeline().names().contains("noConnectedKill")){
                channel.pipeline().remove("noConnectedKill");
            }
            //处理连接心跳包
            if (mqttConnectMessage.variableHeader().keepAliveTimeSeconds() > 0){
                if (channel.pipeline().names().contains("idle")){
                    channel.pipeline().remove("idle");
                }
                channel.pipeline().addFirst("idle",new IdleStateHandler(0, 0, Math.round(mqttConnectMessage.variableHeader().keepAliveTimeSeconds() * 1.5f)));
            }else{
                if (channel.pipeline().names().contains("idle")){
                    channel.pipeline().remove("idle");
                }
            }

        }
    }
}
