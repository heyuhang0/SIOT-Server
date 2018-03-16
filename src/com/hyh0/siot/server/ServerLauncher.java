package com.hyh0.siot.server;

public class ServerLauncher {
    public static void main(String[] args) {
        if (args.length >= 1) {
            int portNumber = Integer.parseInt(args[0]);
            if (portNumber >= 0 && portNumber <= 65535) {
                Server server = new Server(4342);
                server.run();
            }
        }
        System.out.println("Please start with a port number");
        System.out.println("like \"java -jar filename 4342\"");
    }
}
