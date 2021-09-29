package com.risky.server.MQTT.test;

import com.alibaba.fastjson.JSONObject;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MQTTOnMessageCallback implements MqttCallback {


    final private static Logger log = LoggerFactory.getLogger(MQTTOnMessageCallback.class);

    @Override
    public void connectionLost(Throwable throwable) {
        log.info("连接断开！");
        throwable.printStackTrace();
    }


    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) {
        // subscribe后得到的消息会执行到这里面
        /*System.out.println("接收消息主题:" + s);
        System.out.println("接收消息Qos:" + mqttMessage.getQos());
        System.out.println("数据格式"+ MqttTypeEnum.getTypeByValue(mqttMessage.getPayload()[0]).getDesc());
        System.out.println("接收消息内容:" + JSONObject.parseObject(new String(mqttMessage.getPayload(),MQTT_UP_JSON_SIZE_END_INDEX,mqttMessage.getPayload().length-MQTT_UP_JSON_SIZE_END_INDEX)));*/
        log.info(new String(mqttMessage.getPayload()));

        log.info("客户端接收消息内容:" + new String(mqttMessage.getPayload()));
//        MqttDataProcessMain mqttDataProcessMain = new MqttDataProcessMainExtend(mqNetData,SpringUtils.getBean(RabbitTemplate.class))
//                .addQuerier(SpringUtils.getBean(MqttQuerier.class)).addResolver(SpringUtils.getBean(MqttBaseResolver.class));
//        mqttDataProcessMain.setAutosave(true);
//        mqttDataProcessMain.prccess();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        //publish的时候调用这里
        System.out.println("deliveryComplete---------" +    iMqttDeliveryToken.isComplete());
    }

}
