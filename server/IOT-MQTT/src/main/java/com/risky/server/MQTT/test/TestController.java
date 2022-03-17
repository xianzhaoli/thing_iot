/*
package com.risky.server.MQTT.test;

import com.risky.server.MQTT.common.cache.redis.connection.MqttConnectionClientCache;
import com.risky.server.MQTT.config.ConnectClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/test")
@RestController
public class TestController {

    @Autowired
    private MqttConnectionClientCache mqttConnectionClientCache;

    @RequestMapping("/test")
    public String test(){
        mqttConnectionClientCache.add("aaaa",new ConnectClient());
        return "ok";
    }

    @RequestMapping("/get")
    public String get(){
        mqttConnectionClientCache.get("aaaa");
        return "ok";
    }
}
*/
