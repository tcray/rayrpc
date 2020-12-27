package com.tcray.rayrpc.core.handler;

import com.tcray.rayrpc.core.meta.ConnectionStatusConstant;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lirui
 */
@Slf4j
public class ConnectionStateHandler extends ChannelDuplexHandler {

    private AtomicInteger connectionStatus;

    public ConnectionStateHandler(AtomicInteger connectionStatus) {
        super();
        this.connectionStatus = connectionStatus;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        boolean success = connectionStatus.compareAndSet(ConnectionStatusConstant.CONNECTING,ConnectionStatusConstant.ACTIVED);
        if (success) {
        } else {
            log.error("{msg:'channelActive() compareAndSet fail', connLifecycleState:" + connectionStatus.get() + "}");
        }

        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("{msg:'channelInactive()', connectionStatus:" + connectionStatus.get() + "}");

        connectionStatus.set(ConnectionStatusConstant.DISCONNECTED);
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("{msg:'exceptionCaught', connectionStatus:" + connectionStatus.get() + "}");

        connectionStatus.set(ConnectionStatusConstant.DISCONNECTED);
        ctx.close();
        ctx.fireExceptionCaught(cause);
    }
}