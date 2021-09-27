package com.risky.server.MQTT.system;

import cn.hutool.core.util.StrUtil;
import com.risky.server.MQTT.client.SubscribeClient;
import com.risky.server.MQTT.message.MessageService;
import com.risky.server.MQTT.protocol.Subscribe;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SystemTopic {

    private final static String SYS_ACTIVE_INFO_SUBSCRIBE_BASE = "$SYS/broker";

    private final static String SYS_VERSION = SYS_ACTIVE_INFO_SUBSCRIBE_BASE + "/version" ;

    private final static String SYS_TIMESTAMP = SYS_ACTIVE_INFO_SUBSCRIBE_BASE + "/timestamp" ;

    @Value("${iot-server.version:Beta-0.0.1}")
    private String version;

    private static List<String> systemBorders = new ArrayList<>();

    static{
        systemBorders.add(SYS_VERSION);
        systemBorders.add(SYS_TIMESTAMP);
    }

    @Autowired
    private MessageService messageService;

    public void sendSysInfo(SubscribeClient subscribeClient){
        switch (subscribeClient.getTopic()){
            case SYS_VERSION:
                messageService.publishMessage(subscribeClient,subscribeClient.getTopic(), StrUtil.bytes(version));
                break;
            default:
                break;
        }
    }

    public List<String> getSystemBorders(){
        return systemBorders;
    }



}
