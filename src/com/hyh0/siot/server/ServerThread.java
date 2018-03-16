package com.hyh0.siot.server;

import com.hyh0.siot.server.Data.DataBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Logger;

class ServerThread extends Thread implements Listener<String, String> {
    private Socket client;
    private DataBase<String, String> dataBase;
    private static Logger logger = null;
    private boolean closed = false;

    private PrintStream out;
    private BufferedReader in;

    public ServerThread(Socket client, DataBase<String, String> dataBase) {
        this.client = client;
        this.dataBase = dataBase;
        logger = Logger.getLogger("com.hyh0.tcp.test");
    }

    private void setCommandHandler(String table, String key, String newValue) {
        dataBase.getTable(table).put(key, newValue);
        out.println("OK SET " + key + " " + dataBase.getTable(table).get(key));
        logger.info(key + " is set to " + newValue);
    }

    private void getCommandHandler(String table, String key) {
        String value = dataBase.getTable(table).get(key);
        out.println("GET " + key + " " + value);
        logger.info("get th value of " + key + " successfully");
    }

    @Override
    public void handler(String key, String value) {
        if (!closed)
            out.println("HOOK " + key + " " + value);
    }

    private void hookCommandHandler(String table, String key) {
        dataBase.getTable(table).addListener(key, this);
        out.println("OK HOOK " + key);
    }

    private void unhookCommandHandler(String table, String key) {
        dataBase.getTable(table).removeListener(key, this);
    }

    private void wrongCommandHandler(String inStr) {
        out.println("ERROR WRONG_COMMAND " + inStr);
    }

    @Override
    public void run() {
        try {
            out = new PrintStream(client.getOutputStream());
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            while (true) {
                String inStr = in.readLine();
                if (inStr == null) {
                    logger.info("Received nothing");
                    logger.info("Connection with " + client + " terminated");
                    break;
                }
                logger.info("received: " + inStr);
                String[] inputs = inStr.split(" ");
                if ("at".equals(inputs[0])) {
                    out.println("OK");
                    continue;
                }
                if (inputs.length < 3 && !"at".equals(inputs[0])) {
                    wrongCommandHandler(inStr);
                    continue;
                }
                String command = inputs[0];
                String table = inputs[1];
                String key = inputs[2];

                if (command.equals("set")) {
                    if (inputs.length < 4) {
                        wrongCommandHandler(inStr);
                        continue;
                    }
                    setCommandHandler(table, key, inputs[3]);

                } else if (command.equals("get")) {
                    getCommandHandler(table, key);

                } else if (command.equals("hook")) {
                    hookCommandHandler(table, key);

                } else if (command.equals("unhook")) {
                    unhookCommandHandler(table, key);

                } else {
                    wrongCommandHandler(inStr);
                }
            }
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closed = true;
            dataBase.removeListener(this);
        }
    }

}
