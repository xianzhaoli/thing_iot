package com.risky.server.MQTT.test;

import lombok.SneakyThrows;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MqttClientTest {




    private static MemoryPersistence persistence = new MemoryPersistence();

    static {

    }

    private static final ThreadPoolExecutor THREADPOOL = new ThreadPoolExecutor(96, 128, 6,
            TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1000),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    public static void main(String[] args)  {

        for (int i = 0; i < 10 ; i++) {
            int finalI = i;
            THREADPOOL.execute(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    for (int j = 0; j < 50 ; j++) {
                        MqttClient mqttClient = new MqttClient("tcp://127.0.0.1:1883", UUID.randomUUID().toString() + System.currentTimeMillis() + finalI,persistence);
                        mqttClient.setCallback(new MQTTOnMessageCallback());
                        MqttConnectOptions connOpts = new MqttConnectOptions();
                        connOpts.setCleanSession(true);
                        connOpts.setKeepAliveInterval(120);
                        connOpts.setWill("aa/dd",new String("wo mei le : " + mqttClient.getClientId()).getBytes(),0,false);
                        mqttClient.connect(connOpts);

                        Thread.sleep(1);
                    }
                }
            });

        }
    }
}
