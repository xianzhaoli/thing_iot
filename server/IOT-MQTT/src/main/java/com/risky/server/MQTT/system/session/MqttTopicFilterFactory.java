package com.risky.server.MQTT.system.session;


import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author lxz
 */
@Slf4j
public class MqttTopicFilterFactory {

    private static final ConcurrentMap<String, MqttTopicFilter> filters = new ConcurrentHashMap<>();

    public static MqttTopicFilter toFilter(String topicFilter) {
        if (topicFilter == null || topicFilter.isEmpty()) {
            throw new IllegalArgumentException("Topic filter can't be empty!");
        }
        return filters.computeIfAbsent(topicFilter, filter -> {
            if (filter.equals("#")) {
                return new AlwaysTrueTopicFilter();
            } else if (filter.contains("+") || filter.contains("#")) {
                String regex = filter
                        .replace("\\", "\\\\")
                        .replace("+", "[^/]+")
                        .replace("/#", "($|/.*)");
                log.debug("Converting [{}] to [{}]", filter, regex);
                return new RegexTopicFilter(regex);
            } else {
                return new EqualsTopicFilter(filter);
            }
        });
    }

}
