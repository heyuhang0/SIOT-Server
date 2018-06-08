package com.hyh0.siot.server;

import com.hyh0.fstcpsocket.server.TCPServer;

import java.io.IOException;

public class ServerLauncher {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Please start with a port number");
            System.out.println("like \"java -jar filename 4342\"");
        }
        int portNumber;
        try {
            portNumber = Integer.parseInt(args[0]);
            if (!(portNumber >= 0 && portNumber <= 65535)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.out.println("Port number is not valid");
            return;
        }

        TCPServer server = new TCPServer.Builder()
                .port(portNumber)
                .withHandler(new SIOTServerCore())
                .build();

        System.out.println("Server started at port " + portNumber);
        server.start();
    }
}
