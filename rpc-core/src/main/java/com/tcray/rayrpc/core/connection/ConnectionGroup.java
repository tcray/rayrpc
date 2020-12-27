package com.tcray.rayrpc.core.connection;

import com.tcray.rayrpc.core.handler.TimerWorkerHandler;
import com.tcray.rayrpc.core.meta.RpcContext;
import com.tcray.rayrpc.core.meta.ServerMeta;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author lirui
 */
@Slf4j
public class ConnectionGroup {

    private TimerWorkerHandler timerWorkerHandler;

    private List<ServerMeta> serverMetaList = new CopyOnWriteArrayList<>();

    List<RpcConnection> connectionList = new CopyOnWriteArrayList<>();

    public void startClient(Map<String, String> serverList, RpcContext rpcContext) {
        timerWorkerHandler = new TimerWorkerHandler();

        for (Map.Entry<String, String> serverPair : serverList.entrySet()) {
            ServerMeta serverMeta = new ServerMeta(serverPair.getKey(), Integer.parseInt(serverPair.getValue()));
            serverMetaList.add(serverMeta);
        }

        RpcConnection lastConnection = null;
        for (ServerMeta serverMeta : serverMetaList) {
            RpcConnection connection = new RpcConnection(serverMeta, rpcContext);
            connection.startClient();
            connectionList.add(connection);
            timerWorkerHandler.addTimerJob(new KeepConnectionJob(connection));
            lastConnection = connection;
        }
        //建立链接
        lastConnection.makeConnectionInCallerThread();

        timerWorkerHandler.start();
    }

    class KeepConnectionJob implements Runnable {
        private RpcConnection connection;

        public KeepConnectionJob(RpcConnection connection) {
            super();
            this.connection = connection;
        }

        @Override
        public void run() {
            if (connection.isNeedOpenConnection()) {
                connection.makeConnectionInCallerThread();
            }
        }
    }

    public void startServer() {
    }

    public void addConnection(RpcConnection connection) {
        connectionList.add(connection);
    }

    public boolean isRpcWithServerOk() {
        for (RpcConnection connection : connectionList) {
            if (connection.isConnectionOk()) {
                return true;
            }
        }

        return false;
    }

    public void sendRpcOneWay(String interfaceName, String methodSign, Object[] args) {
        RpcConnection findConnection = findConnection();
        findConnection.sendRpcOneWay(interfaceName, methodSign, args);
    }

    public void broadcastRpcOneWay(String interfaceName, String methodSign, Object[] args) {
        for (RpcConnection connection : connectionList) {
            try {
                connection.sendRpcOneWay(interfaceName, methodSign, args);
            } catch (Throwable ex) {
                log.error("{msg:\"log for continue loop\"}", ex);
            }
        }
    }

    public Object sendRpc(String interfaceName, String methodSign, Object[] args, Type returnType, long timeoutInMs) throws Throwable {
        RpcConnection connection = findConnection();
        if (connection == null) {
            throw new Throwable("can not find success connection! is server all fail?");
        }
        return connection.sendRpc(interfaceName, methodSign, args, returnType, timeoutInMs);
    }

    /**
     * 1.负载均衡,随机调用.
     * 2.链路检查,确保链路正常.
     *
     * @return
     */
    private RpcConnection findConnection() {
        RpcConnection selectedConnection = null;
        Random rand = new Random();
        for (int i = 0; i < connectionList.size(); i++) {
            int index = rand.nextInt(connectionList.size());
            RpcConnection connection = connectionList.get(index);
            if (connection.isConnectionOk()) {
                selectedConnection = connection;
                break;
            } else {
                log.error("{msg:\"some connection not ok in random choice\"}");
            }
        }
        if (selectedConnection == null) {
            for (RpcConnection connection : connectionList) {
                if (connection.isConnectionOk()) {
                    selectedConnection = connection;
                    break;
                } else {
                    log.error("{msg:\"some connection not ok in loop\"}");
                }
            }
        }

        if (selectedConnection == null) {
            log.error("{msg:\"this connection maybe not ok\"}");
            int index = rand.nextInt(connectionList.size());
            selectedConnection = connectionList.get(index);
        }
        return selectedConnection;
    }

    public void close() {
        //连接关闭
        for (RpcConnection connection : connectionList) {
            connection.close();
        }
        //守护进程关闭
        if (timerWorkerHandler != null) {
            try {
                timerWorkerHandler.close();
            } catch (Throwable ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }

}
