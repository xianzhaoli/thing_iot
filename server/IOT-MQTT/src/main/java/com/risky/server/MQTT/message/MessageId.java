package com.risky.server.MQTT.message;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/25 下午10:56
 * @description：消息ID
 * @modified By：`
 * @version: 1.0
 */
@Data
@AllArgsConstructor
public class MessageId {

    private int messageId;

    private long timestamp;

    private String clientId;

    @Override
    public boolean equals(Object o) {
        MessageId messageId1 = (MessageId) o;
        return messageId == messageId1.messageId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, timestamp);
    }

    public MessageId(int messageId) {
        this.messageId = messageId;
    }
}
