package com.risky.server.MQTT.common.store.batch;

import java.util.List;

@FunctionalInterface
public interface BatchFunction<T> {

    void batchSave(List<T> collection);

}
