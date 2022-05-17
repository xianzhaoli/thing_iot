package com.risky.server.MQTT.service;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MqttCmdContorller {

    @RequestMapping("/mqtt/info")
    public String info(){
        return "MQTT Server";
    }
}
