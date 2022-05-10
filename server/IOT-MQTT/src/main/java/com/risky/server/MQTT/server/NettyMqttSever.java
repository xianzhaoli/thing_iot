package com.risky.server.MQTT.server;

import com.risky.server.MQTT.handler.MQTTNettyHandler;
import com.risky.server.MQTT.process.MqttProtocolProcess;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Component
public class NettyMqttSever  {

    private NioEventLoopGroup bossGroup;

    private NioEventLoopGroup workGroup;

    private Channel serverChanel;

    @Value("${iot-server.port:1883}")
    private int port;
    @Value("${iot-server.host:0.0.0.0}")
    private String host;
    @Resource
    private MqttProtocolProcess mqttProtocolProcess;

    @PostConstruct
    public void startup() {
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup(6);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.SO_RCVBUF, 10485760);
            bootstrap.childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            bootstrap.group(bossGroup,workGroup)
                    .handler(new LoggingHandler(LogLevel.TRACE))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            ChannelPipeline p = socketChannel.pipeline();
                            p.addLast(new MqttDecoder(65535));
                            p.addLast(MqttEncoder.INSTANCE);
                            MQTTNettyHandler mqttNettyHandler = new MQTTNettyHandler(mqttProtocolProcess);
                            p.addLast(mqttNettyHandler);
                        }
                    });
            serverChanel = bootstrap.bind(host,port).sync().channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @PreDestroy
    public void shutDown(){
        try {
            System.out.println("程序关闭");
            serverChanel.close().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
