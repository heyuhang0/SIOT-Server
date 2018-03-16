package com.hyh0.siot.server;

import com.hyh0.siot.server.Data.DataBase;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class Server extends Thread{

    private int port;
    private static Logger logger;
    private DataBase<String, String> dataBase;

    public Server(int port) {
        this.port = port;
        this.dataBase = new DataBase<>();
        logger = Logger.getLogger("com.hyh0.tcp.test");
    }

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(port);
            logger.info("Server started at port " + port);

            while (true) {
                Socket client = server.accept();
                logger.info("Connected with " + client.toString());
                new Thread(new ServerThread(client, dataBase)).start();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
