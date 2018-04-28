package com.hyh0.siot.server;

import com.hyh0.fstcpsocket.server.TCPServer;
import com.hyh0.siot.server.Data.DataBase;

public class ServerLauncher {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please start with a port number");
            System.out.println("like \"java -jar filename 4342\"");
        }
        int portNumber;
        try {
            portNumber = Integer.parseInt(args[0]);
            if (portNumber >= 0 && portNumber <= 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.out.println("Port number is not valid");
            return;
        }

        /*
        TCPServer sever = new TCPServer.Builder()
                .withHandler(new ServerThread(null, new DataBase<String, String>()))
                .
                */
        Server server = new Server(portNumber);
        server.run();
    }
}
