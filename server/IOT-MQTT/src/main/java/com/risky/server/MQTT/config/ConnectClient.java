package com.risky.server.MQTT.config;

import com.risky.server.MQTT.client.WillMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConnectClient implements Serializable {

    private static final long serialVersionUID = 2934308221419710748L;

    private String clientId;

    private boolean cleanSession;

    private boolean willFlag;

    private String username;

    private byte[] password;

    private WillMessage willMessage;

}