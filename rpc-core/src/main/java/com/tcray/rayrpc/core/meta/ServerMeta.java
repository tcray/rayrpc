package com.tcray.rayrpc.core.meta;

import com.alibaba.fastjson.JSON;
import lombok.Data;

/**
 * @author lirui
 */
@Data
public class ServerMeta {

    private String serverName;
    private int port;
    private int connectErrorCount;
    private int connectSuccessCount;

    public ServerMeta(String serverName, int port) {
        this.serverName = serverName;
        this.port = port;
    }

    public String toString() {
        return JSON.toJSONString(this);
    }

    public void addConnectSuccessCount() {
        if (connectSuccessCount > Integer.MAX_VALUE - 10000) {
            connectSuccessCount = 0;
        }
        this.connectSuccessCount++;
    }

    public void addConnectErrorCount() {
        if (connectErrorCount > Integer.MAX_VALUE - 10000) {
            connectErrorCount = 0;
        }
        connectErrorCount++;
    }

}
