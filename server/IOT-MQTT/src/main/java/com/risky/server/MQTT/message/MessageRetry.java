package com.risky.server.MQTT.message;

import lombok.Data;

import java.util.List;

/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/26 下午11:28
 * @description：消息重发
 * @modified By：`
 * @version: 1.0
 */
@Data
public class MessageRetry {

    private byte[] payload;

    private long timestamp;

    private List<MessageId> messageIds;

}
