package com.risky.server.MQTT.process;

import com.risky.server.MQTT.common.MqttStoreService;
import com.risky.server.MQTT.message.MessageService;
import com.risky.server.MQTT.protocol.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/25 下午1:47
 * @description：MQTT协议处理器
 * @modified By：`
 * @version: 1.0
 */
@Component
public class MqttProtocolProcess {

    private Connection connection;

    private DisConnection disConnection;

    private PubRel pubRel;

    private Ping ping;

    private Publish publish;

    private Subscribe subscribe;

    private UnSubscribe unSubscribe;

    private PubAck pubAck;

    @Resource
    private MqttStoreService mqttStoreService;

    @Resource
    private MessageService messageService;

    public Connection connection(){
        if(connection == null){
            connection = new Connection(mqttStoreService);
        }
        return connection;
    }

    public DisConnection disConnection(){
        if(disConnection == null){
            disConnection = new DisConnection(mqttStoreService);
        }
        return disConnection;
    }

    public PubRel pubRel(){
        if(pubRel == null){
            pubRel = new PubRel(mqttStoreService);
        }
        return pubRel;
    }

    public Ping ping(){
        if(ping == null){
            ping = new Ping(mqttStoreService);
        }
        return ping;
    }

    public Publish publish(){
        if(publish == null){
            publish = new Publish(mqttStoreService,messageService);
        }
        return publish;
    }

    public Subscribe subscribe(){
        if(subscribe == null){
            subscribe = new Subscribe(mqttStoreService);
        }
        return subscribe;
    }

    public UnSubscribe unSubscribe(){
        if(unSubscribe == null){
            unSubscribe = new UnSubscribe(mqttStoreService);
        }
        return unSubscribe;
    }

    public PubAck pubAck(){
        if(pubAck == null){
            pubAck = new PubAck(mqttStoreService,messageService);
        }
        return pubAck;
    }
}
