package com.risky.server.MQTT.util;

import lombok.Data;

import java.io.Serializable;

@Data
public class AlwaysTrueTopicFilter implements MqttTopicFilter, Serializable {

    private static final long serialVersionUID = 6754110322143260941L;

    @Override
    public boolean filter(String topic) {
        return true;
    }
}
