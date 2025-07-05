package com.monobogdan.engine.multiplayer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.ServerSocketChannel;

public class MPServer {
    private int port;
    private ServerSocket listener;
    private Thread thread;

    public MPServer(int port) {
        this.port = port;

        try {
            ServerSocketChannel channel = ServerSocketChannel.open();
            channel.configureBlocking(false);
            listener = new ServerSocket(port, 256);

        } catch (UnknownHostException e) {
            throw new RuntimeException("WTF?", e);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create listener socket");
        }
    }

    public void start() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Socket sock = null;


            }
        });
        thread.start();
    }
}
