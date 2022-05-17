package com.risky.server.MQTT;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/23 下午9:54
 * @description：MQTT服务器启动类
 * @modified By：
 * @version: 1.0
 */
@SpringBootApplication(scanBasePackages = {"com.risky.server"})
@ComponentScan(basePackages = {"com.risky.server"})
@EnableCaching
@EnableDiscoveryClient
public class MQTTServerStarter {

    public static void main(String[] args) {
        SpringApplication.run(MQTTServerStarter.class,args);
    }

}
