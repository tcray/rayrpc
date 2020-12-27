package com.tcray.rayrpc.core.connection;

import com.tcray.rayrpc.core.codec.ByteToMsgDecoder;
import com.tcray.rayrpc.core.codec.MsgToByteEncoder;
import com.tcray.rayrpc.core.exception.TimeoutException;
import com.tcray.rayrpc.core.handler.ConnectionStateHandler;
import com.tcray.rayrpc.core.handler.MsgHandler;
import com.tcray.rayrpc.core.meta.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lirui
 */
@Slf4j
public class RpcConnection {

    private ServerMeta serverMeta;
    private RpcContext rpcContext;

    private volatile Channel channel = null;

    private NioEventLoopGroup eventLoopGroup;

    private AtomicInteger connectionStatus = new AtomicInteger(ConnectionStatusConstant.DISCONNECTED);

    public RpcConnection(ServerMeta serverMeta, RpcContext rpcContext) {
        this.serverMeta = serverMeta;
        this.rpcContext = rpcContext;
    }

    public RpcConnection(Channel channel, RpcContext rpcContext) {
        this.channel = channel;
        this.rpcContext = rpcContext;
    }

    public void startClient() {
    }

    public void makeConnectionInCallerThread() {
        connect();
    }

    private void connect() {
        if (!connectionStatus.compareAndSet(ConnectionStatusConstant.DISCONNECTED, ConnectionStatusConstant.CONNECTING)) {
            return;
        }

        if (eventLoopGroup != null) {
            try {
                eventLoopGroup.shutdownGracefully().sync();
                eventLoopGroup = null;
            } catch (InterruptedException e) {
                log.error("释放netty资源出现异常", e);
            }

        }

        eventLoopGroup = new NioEventLoopGroup();
        //分包
        final LengthFieldPrepender frameEncoder = new LengthFieldPrepender(4, true);
        //encode
        final MsgToByteEncoder msgEncoder = new MsgToByteEncoder();
        //decode
        final ByteToMsgDecoder msgDecoder = new ByteToMsgDecoder();
        //handler
        final MsgHandler msgHandler = new MsgHandler(rpcContext);
        //connect status
        final ConnectionStateHandler connectionStateHandler = new ConnectionStateHandler(connectionStatus);

        Bootstrap clientBoot = new Bootstrap();
        clientBoot.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15 * 1000)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();

                        pipeline.addLast(connectionStateHandler)
                                .addLast("4", frameEncoder)//out 4
                                .addLast("5", msgEncoder)//out 5
                                .addLast("1", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, -4, 4))//in 1
                                .addLast("2", msgDecoder)//in 2
                                .addLast("3",msgHandler)//in 3
                        ;
                    }
                });

        try {
            //链接
            ChannelFuture channelFuture = clientBoot.connect(serverMeta.getServerName(), serverMeta.getPort());
            channel = channelFuture.channel();
            channelFuture.addListener((ChannelFutureListener) future -> {
                if (future.isDone() && future.cause() != null) {
                    connectionStatus.set(ConnectionStatusConstant.DISCONNECTED);
                    log.error("{serverName:\"" + serverMeta.getServerName() + "\", serverPort:" + serverMeta.getPort() + "}", future.cause());
                } else {
                    log.info("{msg:\"connected\", serverName:\"" + serverMeta.getServerName() + "\", serverPort:" + serverMeta.getPort() + "}");
                    serverMeta.addConnectSuccessCount();
                }
            });

            boolean isSuccess = channelFuture.awaitUninterruptibly(15, TimeUnit.SECONDS);
            if (!isSuccess) {
                serverMeta.addConnectErrorCount();
                channelFuture.cancel(true);
                channel = null;
            }
        } catch (Throwable ex) {
            channel = null;
            log.error(serverMeta.toString(), ex);
            serverMeta.addConnectErrorCount();
        }
    }

    public boolean isConnectionOk() {
        if (channel == null) {
            return false;
        }
        if (connectionStatus.get() == ConnectionStatusConstant.ACTIVED && channel.isActive()) {
            return true;
        }
        return false;
    }

    public boolean isNeedOpenConnection() {
        if (channel == null) {
            return true;
        }
        if (connectionStatus.get() == ConnectionStatusConstant.DISCONNECTED) {
            return true;
        }
        return false;
    }

    public void close() {
        closeResource();
    }

    public synchronized void closeResource() {
        if (channel != null) {
            try {
                channel.close();
            } catch (Throwable ex) {
                log.error(ex.getMessage(), ex);
            } finally {
                channel = null;
            }
        }
    }

    public void sendRpcOneWay(String interfaceName, String methodSign, Object[] args) {
        CallCommand callCmd = new CallCommand();
        callCmd.setItfName(interfaceName);
        callCmd.setMethodSign(methodSign);
        callCmd.setArgs(args);
        channel.write(callCmd);
    }

    public Object sendRpc(String interfaceName, String methodSign, Object[] args, Type returnType, long timeoutInMs) {
        CallCommand callCmd = new CallCommand();
        callCmd.setItfName(interfaceName);
        callCmd.setMethodSign(methodSign);
        callCmd.setArgs(args);

        CallResultFuture future = new CallResultFuture(returnType);
        rpcContext.getRequestPool().put(callCmd.getRequestId(), future);
        try {
            boolean sent = false;
            for (int i = 0; i < 150; i++) {
                if (channel == null || !channel.isActive()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                } else {
                    channel.writeAndFlush(callCmd);
                    sent = true;
                    break;
                }
            }
            if (sent) {
                future.waitReturn(timeoutInMs);

                return future.getResult();
            } else {
                throw new TimeoutException("timeout exceed 300000ms");
            }
        } finally {
            rpcContext.getRequestPool().remove(callCmd.getRequestId());
        }
    }

}
