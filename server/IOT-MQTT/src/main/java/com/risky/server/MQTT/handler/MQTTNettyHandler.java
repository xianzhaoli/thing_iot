package com.risky.server.MQTT.handler;

import com.risky.server.MQTT.common.MqttStoreService;
import com.risky.server.MQTT.process.MqttProtocolProcess;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import javafx.scene.layout.BackgroundRepeat;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;

import java.io.UnsupportedEncodingException;

/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/24 下午7:26
 * @description：MQTT通信主处理流程
 * @modified By：
 * @version: 1.0
 */
@AllArgsConstructor
public class MQTTNettyHandler extends SimpleChannelInboundHandler<MqttMessage> {

    private MqttProtocolProcess mqttProtocolProcess;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) {

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

    }

}
