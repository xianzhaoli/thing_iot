package com.risky.server.MQTT.test;

import com.sun.org.apache.bcel.internal.generic.NEW;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class NioServer {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        serverSocketChannel.socket().bind(new InetSocketAddress(InetAddress.getByName("IP"),1888));
        serverSocketChannel.configureBlocking(false);

        Selector selector = Selector.open();
        /*NEW Thread(new Reactor)*/

    }
}
