package com.risky.server.MQTT.system.session;

import lombok.Data;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * @author lxz
 */
@Data
public class RegexTopicFilter implements MqttTopicFilter,Serializable {

    private static final long serialVersionUID = -3830116437499354280L;

    private final Pattern regex;

    public RegexTopicFilter(String regex) {
        this.regex = Pattern.compile(regex);
    }

    @Override
    public boolean filter(String topic) {
        return regex.matcher(topic).matches();
    }
}
