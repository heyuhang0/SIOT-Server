package com.hyh0.siot.server;

import com.hyh0.siot.server.Data.DataBase;
import com.hyh0.siot.server.Data.DataTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Logger;

class ServerThread extends Thread {
    private Socket client;
    private DataBase<String, String> dataBase;
    private static Logger logger = null;
    private boolean closed = false;

    private PrintStream socketOut;

    private Listener<String, String> hookListener = new Listener<String, String>() {
        @Override
        public void handler(String key, String value) {
            if (!closed)
                socketOut.println("HOOK " + key + " " + value);
        }
    };

    public ServerThread(Socket client, DataBase<String, String> dataBase) {
        this.client = client;
        this.dataBase = dataBase;
        logger = Logger.getLogger("com.hyh0.tcp.test");
    }

    private void doSet(String tableName, String key, String newValue) {
        DataTable<String, String> table = dataBase.getTable(tableName);
        table.removeListener(key, hookListener);
        table.put(key, newValue);
        table.addListener(key, hookListener);
        socketOut.println("OK SET " + key + " " + newValue);
        logger.info(key + " is set to " + newValue);
    }

    private void doGet(String table, String key) {
        String value = dataBase.getTable(table).get(key);
        socketOut.println("GET " + key + " " + value);
        logger.info("get th value of " + key + " successfully");
    }

    private void doHook(String table, String key) {
        dataBase.getTable(table).addListener(key, hookListener);
        socketOut.println("OK HOOK " + key);
    }

    private void doUnhook(String table, String key) {
        dataBase.getTable(table).removeListener(key, hookListener);
        socketOut.println("OK UNHOOK " + key);
    }

    private void doWrongCommand(String inStr) {
        socketOut.println("ERROR WRONG_COMMAND " + inStr);
    }

    @Override
    public void run() {
        try {
            socketOut = new PrintStream(client.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
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
                    socketOut.println("OK");
                    continue;
                }
                if ("close".equals(inputs[0])) {
                    socketOut.println("OK");
                    break;
                }
                if (inputs.length < 3 && !"at".equals(inputs[0])) {
                    doWrongCommand(inStr);
                    continue;
                }
                String command = inputs[0];
                String table = inputs[1];
                String key = inputs[2];

                switch (command) {
                    case "set":
                        if (inputs.length < 4) {
                            doWrongCommand(inStr);
                            continue;
                        }
                        doSet(table, key, inputs[3]);

                        break;
                    case "get":
                        doGet(table, key);

                        break;
                    case "hook":
                        doHook(table, key);

                        break;
                    case "unhook":
                        doUnhook(table, key);

                        break;
                    default:
                        doWrongCommand(inStr);
                        break;
                }
            }
            socketOut.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closed = true;
            dataBase.removeListener(hookListener);
        }
    }

}
