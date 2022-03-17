package com.risky.server.MQTT.handler;

import com.risky.server.MQTT.common.cache.redis.subscribe.SubscribeClient;
import com.risky.server.MQTT.process.MqttProtocolProcess;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Set;

/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/24 下午7:26
 * @description：MQTT通信主处理流程
 * @modified By：
 * @version: 1.0
 */
@AllArgsConstructor
@Slf4j
public class MQTTNettyHandler extends SimpleChannelInboundHandler<MqttMessage> {


    private MqttProtocolProcess mqttProtocolProcess;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) {

        long start = System.currentTimeMillis();

        Channel channel = ctx.channel();

        if(msg.decoderResult().isSuccess()){
            switch (msg.fixedHeader().messageType()){
                case CONNECT:
                    //连接处理
                    mqttProtocolProcess.connection().sendConnAckMessage(channel, (MqttConnectMessage) msg);
                    break;
                case PINGREQ:
                    //心跳包
                    mqttProtocolProcess.ping().sendPongMessage(channel,msg);
                    break;
                case PUBLISH: //发布消息
                    mqttProtocolProcess.publish().sendPublishMessage(channel, (MqttPublishMessage) msg);
                    break;
                case PUBREC:
                    System.out.println("PUBREC ------");
                    mqttProtocolProcess.pubRec().processPubRec(channel, (MqttMessageIdVariableHeader) msg.variableHeader());
                    break;
                case PUBACK:
                    System.out.println("PUBACK ------");
                    mqttProtocolProcess.pubAck().sendPubAckMessage(channel, (MqttPubAckMessage) msg);
                    break;
                case SUBACK:
                    System.out.println("SUBACK ------");
                    break;
                case SUBSCRIBE:
                    mqttProtocolProcess.subscribe().sendSubscribeMessage(channel, (MqttSubscribeMessage) msg);
                    break;
                case PUBREL:
                    // PUBREC 消息回复
                    mqttProtocolProcess.pubRel().sendPubCompMessage(channel, (MqttMessageIdVariableHeader) msg.variableHeader());
                    break;
                case PUBCOMP:
                    System.out.println("PUBCOMP ------");
                    mqttProtocolProcess.pubComp().processPubComp(channel, (MqttMessageIdVariableHeader) msg.variableHeader());
                    break;
                case DISCONNECT:
                    mqttProtocolProcess.disConnection().disConnectionProcess(channel,msg);
                    break;
                case UNSUBACK:
                    System.out.println("UNSUBACK ------");
                    break;
                case UNSUBSCRIBE:
                    mqttProtocolProcess.unSubscribe().sendUnSubscribeMessage(channel, (MqttUnsubscribeMessage) msg);
                    break;
                default:
                    break;
            }
        }else{
            ctx.channel().close();
        }
        long e = System.currentTimeMillis();
        log.error("处理耗时 ----> {}ms",e-start);
    }



    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        /*Channel channel = ctx.channel();
        if (channel.pipeline().names().contains("idleState")){
            channel.pipeline().remove("idleState");
        }
        //120秒不发送连接信息，就kill掉
        channel.pipeline().addFirst("idleState",new IdleStateHandler(0, 0, 120));*/
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            // 远程主机强迫关闭了一个现有的连接的异常
            String clientId = (String) ctx.channel().attr(AttributeKey.valueOf("clientId")).get();
            mqttProtocolProcess.disConnection().disConnectionProcess(ctx.channel(), null);
            ctx.close();
            log.info("异常关闭连接----->");
        } else {
            mqttProtocolProcess.disConnection().disConnectionProcess(ctx.channel(),null);
            super.exceptionCaught(ctx, cause);
        }
    }



    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        Channel channel = ctx.channel();
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        if (evt instanceof IdleStateEvent) {
            //心跳超时
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.ALL_IDLE) {
                MqttPublishMessage willMessage = (MqttPublishMessage) channel.attr(AttributeKey.valueOf("willMessage")).get();

                if(StringUtils.isNotBlank(clientId) && willMessage != null) {
                    ByteBuf byteBuf = willMessage.payload();
                    byte[] payLoad = new byte[byteBuf.readableBytes()];
                    int index = byteBuf.readerIndex();
                    byteBuf.getBytes(index,payLoad);
                    String topic = willMessage.variableHeader().topicName();
                    Set<SubscribeClient> subscribeClients = mqttProtocolProcess.mqttStoreService.filterChannel(topic);
                    subscribeClients.parallelStream().forEach(subscribeClient -> {
                        Channel channel1 = mqttProtocolProcess.mqttStoreService.getChannelByClientId(subscribeClient.getClientId());
                        if(channel1 != null & channel1.isActive()){
                            mqttProtocolProcess.messageService.publishMessage(subscribeClient,topic,payLoad,channel1);
                        }
                    });
                }
                channel.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
