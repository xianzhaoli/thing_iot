package com.risky.server.MQTT.common.store;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document("mqtt_cs_msg")
@Builder
public class SessionStoreMessage {

    @Id
    @Field("_id")
    private String id;

    @Field("clientId")
    private String clientId;

    @Field("topic")
    private String topic;

    @Field("timestamp")
    private long ts;
    @Field("qos")
    private int qos;
    @Field("messagePayload")
    private byte[] payload;

}
