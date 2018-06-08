package com.hyh0.siot.server;

import com.hyh0.fstcpsocket.socket.MessageHandler;
import com.hyh0.fstcpsocket.socket.TCPSocket;
import com.hyh0.siot.server.Data.DataBase;
import com.hyh0.siot.server.Data.DataTable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class SIOTServerCore implements MessageHandler {

    private static Logger logger;
    private DataBase<String, String> dataBase;
    private final Map<TCPSocket, Listener<String, String>> hookListenersMap = new ConcurrentHashMap<>();

    public SIOTServerCore() {
        logger = Logger.getLogger("com.hyh0.siot.server");
        this.dataBase = new DataBase<>();
    }

    @Override
    public boolean handler(TCPSocket socket, String msg) {
        logger.info("received: " + msg);
        String[] inputs = msg.split(" ");

        try {
            switch (inputs[0]) {
                case "set":
                    doSet(socket, inputs[1], inputs[2], inputs[3]);
                    break;

                case "get":
                    doGet(socket, inputs[1], inputs[2]);
                    break;

                case "hook":
                    doHook(socket, inputs[1], inputs[2]);
                    break;

                case "unhook":
                    doUnhook(socket, inputs[1], inputs[2]);
                    break;

                case "at":
                    doAt(socket);
                    break;

                case "close":
                    doClose(socket);
                    break;

                default:
                    doWrongCommand(socket, msg);
                    break;
            }
        } catch (IndexOutOfBoundsException e) {
            doWrongCommand(socket, msg);
        }

        return true;
    }

    private class HookCallBackListener implements Listener<String,String> {
        private TCPSocket socket;
        HookCallBackListener(TCPSocket socket) {
            this.socket = socket;
        }
        @Override
        public void handler(String key, String value) {
            if (!this.socket.isActive()) {
                hookListenersMap.remove(socket);
                dataBase.removeListener(this);
                return;
            }
            this.socket.sendMessage("HOOK " + key + " " + value);
        }
    }

    private void doAt(TCPSocket socket) {
        socket.sendMessage("OK");
    }

    private void doClose(TCPSocket socket) {
        socket.close();
    }

    private void doSet(TCPSocket socket, String tableName, String key, String newValue) {
        DataTable<String, String> table = dataBase.getTable(tableName);
        table.removeListener(key, hookListenersMap.get(socket));
        table.put(key, newValue);
        table.addListener(key, hookListenersMap.get(socket));
        socket.sendMessage("OK SET " + key + " " + newValue);
        logger.info(key + " is set to " + newValue);
    }

    private void doGet(TCPSocket socket, String table, String key) {
        String value = dataBase.getTable(table).get(key);
        socket.sendMessage("GET " + key + " " + value);
        logger.info("get th value of " + key + " successfully");
    }

    private void doHook(TCPSocket socket, String table, String key) {
        // initialize hook callback listener during socket's first call
        if (hookListenersMap.get(socket) == null) {
            hookListenersMap.put(socket, new HookCallBackListener(socket));
        }
        dataBase.getTable(table).addListener(key, hookListenersMap.get(socket));
        socket.sendMessage("OK HOOK " + key);
    }

    private void doUnhook(TCPSocket socket, String table, String key) {
        dataBase.getTable(table).removeListener(key, hookListenersMap.get(socket));
        socket.sendMessage("OK UNHOOK " + key);
    }

    private void doWrongCommand(TCPSocket socket, String inStr) {
        socket.sendMessage("ERROR WRONG_COMMAND " + inStr);
    }
}
