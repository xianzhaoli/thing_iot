package com.risky.server.MQTT.util;

/**
 * @author lxz
 */
public interface MqttTopicFilter {

    boolean filter(String topic);

}
