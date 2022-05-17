package com.risky.server.MQTT.system.session;

/**
 * @author lxz
 */
public interface MqttTopicFilter {

    boolean filter(String topic);

}
