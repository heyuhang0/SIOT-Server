package com.hyh0.siot.server;

import com.hyh0.siot.server.Data.DataBase;
import com.hyh0.siot.server.Data.DataTable;

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

    private void doSet(String tableName, String key, String newValue) {
        DataTable<String, String> table = dataBase.getTable(tableName);
        table.removeListener(key, this);
        table.put(key, newValue);
        table.addListener(key, this);
        out.println("OK SET " + key + " " + newValue);
        logger.info(key + " is set to " + newValue);
    }

    private void doGet(String table, String key) {
        String value = dataBase.getTable(table).get(key);
        out.println("GET " + key + " " + value);
        logger.info("get th value of " + key + " successfully");
    }

    @Override
    public void handler(String key, String value) {
        if (!closed)
            out.println("HOOK " + key + " " + value);
    }

    private void doHook(String table, String key) {
        dataBase.getTable(table).addListener(key, this);
        out.println("OK HOOK " + key);
    }

    private void doUnhook(String table, String key) {
        dataBase.getTable(table).removeListener(key, this);
        out.println("OK UNHOOK " + key);
    }

    private void doWrongCommand(String inStr) {
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
                if ("close".equals(inputs[0])) {
                    out.println("OK");
                    break;
                }
                if (inputs.length < 3 && !"at".equals(inputs[0])) {
                    doWrongCommand(inStr);
                    continue;
                }
                String command = inputs[0];
                String table = inputs[1];
                String key = inputs[2];

                if (command.equals("set")) {
                    if (inputs.length < 4) {
                        doWrongCommand(inStr);
                        continue;
                    }
                    doSet(table, key, inputs[3]);

                } else if (command.equals("get")) {
                    doGet(table, key);

                } else if (command.equals("hook")) {
                    doHook(table, key);

                } else if (command.equals("unhook")) {
                    doUnhook(table, key);

                } else {
                    doWrongCommand(inStr);
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
