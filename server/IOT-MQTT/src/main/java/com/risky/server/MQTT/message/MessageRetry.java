package com.risky.server.MQTT.message;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/26 下午11:28
 * @description：消息重发
 * @modified By：`
 * @version: 1.0
 */
@Data
@AllArgsConstructor
public class MessageRetry implements Serializable {

    private static final long serialVersionUID = 3496321435292942886L;

    private byte[] payload;

    private String clientId;

    private String topic;

    private int Qos;

    private int messageId;



}
