package com.risky.server.MQTT.client;

import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class WillMessage implements Serializable {

    private static final long serialVersionUID = 8126878379435321189L;

    private int qos;

    private String topic;

    private byte[] message;

    private boolean retain;
}
