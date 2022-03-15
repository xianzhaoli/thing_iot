package com.risky.server.MQTT.client;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ConnectClient implements Serializable {

    private static final long serialVersionUID = 2934308221419710748L;

    private String clientId;

    private boolean cleanSession;

    private boolean willFlag;

    private String username;

    private byte[] password;

    private WillMessage willMessage;

}
