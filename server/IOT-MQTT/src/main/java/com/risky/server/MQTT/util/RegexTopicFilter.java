package com.risky.server.MQTT.util;

import lombok.Data;

import java.util.regex.Pattern;

/**
 * @author lxz
 */
@Data
public class RegexTopicFilter implements MqttTopicFilter {

    private final Pattern regex;

    public RegexTopicFilter(String regex) {
        this.regex = Pattern.compile(regex);
    }

    @Override
    public boolean filter(String topic) {
        return regex.matcher(topic).matches();
    }
}
