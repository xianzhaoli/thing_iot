package com.risky.server.MQTT;


import com.risky.server.MQTT.common.MqttStoreService;
import com.risky.server.MQTT.handler.MQTTNettyHandler;
import com.risky.server.MQTT.process.MqttProtocolProcess;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

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
public class MQTTServerStarter {

    @Resource
    private MqttProtocolProcess mqttProtocolProcess;

    public static void main(String[] args) {
        SpringApplication.run(MQTTServerStarter.class,args);
    }
    @PostConstruct
    public void mqttServerRUN(){
        EventLoopGroup bossGroup = new NioEventLoopGroup();

        EventLoopGroup eventExecutors = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,eventExecutors)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,50000)
                    .handler(new LoggingHandler(LogLevel.TRACE))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) {
                            ChannelPipeline p = socketChannel.pipeline();
                            p.addLast(new MqttDecoder());
                            p.addLast(MqttEncoder.INSTANCE);
                            MQTTNettyHandler mqttNettyHandler = new MQTTNettyHandler(mqttProtocolProcess);
                            p.addLast(mqttNettyHandler);
                        }
                    });
            ChannelFuture f = bootstrap.bind(1883).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            eventExecutors.shutdownGracefully();
        }
    }


}
