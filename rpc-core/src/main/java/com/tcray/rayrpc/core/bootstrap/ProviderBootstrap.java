package com.tcray.rayrpc.core.bootstrap;

import com.tcray.rayrpc.core.annotation.EnableProvider;
import com.tcray.rayrpc.core.codec.ByteToMsgDecoder;
import com.tcray.rayrpc.core.codec.MsgToByteEncoder;
import com.tcray.rayrpc.core.connection.ConnectionGroup;
import com.tcray.rayrpc.core.connection.RpcConnection;
import com.tcray.rayrpc.core.handler.MsgHandler;
import com.tcray.rayrpc.core.meta.RpcContext;
import com.tcray.rayrpc.core.stub.StubSkeletonHelper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author lirui
 */
@Slf4j
@Component
@ConditionalOnBean(name = "rpcProviderApplication")
public class ProviderBootstrap implements Closeable {

    private RpcContext rpcContext = new RpcContext();

    private ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(300);
    private ExecutorService threadPool = new ThreadPoolExecutor(4, 10, 60, TimeUnit.SECONDS, workQueue);

    EventLoopGroup bossEventLoop = new NioEventLoopGroup();
    EventLoopGroup workerEventLoop = new NioEventLoopGroup();

    private int listenPort = 6300;

    ConnectionGroup connectionGroup = new ConnectionGroup();

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void close() throws IOException {
        bossEventLoop.shutdownGracefully();
        workerEventLoop.shutdownGracefully();
    }

    @PostConstruct
    public void start() {
        buildProvider();
        initRpcServer();
    }

    private void buildProvider() {
        String[] beansName = applicationContext.getBeanDefinitionNames();
        for (int i = 0; i < beansName.length; i++) {
            String beanName = beansName[i];
            Object bean = applicationContext.getBean(beanName);
            EnableProvider provider = AnnotationUtils.findAnnotation(bean.getClass(), EnableProvider.class);
            if (provider == null) {
                continue;
            }
            Class<?>[] classes = bean.getClass().getInterfaces();
            if (classes == null || classes.length == 0) {
                continue;
            }
            Arrays.stream(classes).forEach(c -> this.createProvider(c, bean));
        }
    }

    private void initRpcServer() {
        rpcContext.setExecutePool(threadPool);

        ServerBootstrap nettyBoot = new ServerBootstrap();

        final LengthFieldPrepender frameEncoder = new LengthFieldPrepender(4, true);
        //Message -> ByteBuf
        final MsgToByteEncoder msgEncoder = new MsgToByteEncoder();
        //ByteBuf -> Message
        final ByteToMsgDecoder msgDecoder = new ByteToMsgDecoder();
        //handler
        final MsgHandler msgHandler = new MsgHandler(rpcContext);

        nettyBoot.group(bossEventLoop, workerEventLoop)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        //in:1,2,3 ,out 5,4
                        pipeline
                                .addLast("4", frameEncoder)//out 4
                                .addLast("5", msgEncoder)//out 5
                                .addLast("1", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, -4, 4))//in 1
                                .addLast("2", msgDecoder)//in 2
                                .addLast("3",msgHandler)//in 3
                        ;
                        RpcConnection rpcConnection = new RpcConnection(ch, rpcContext);
                        connectionGroup.addConnection(rpcConnection);
                    }
                });
        try {
            //启动
            ChannelFuture channelFuture = nettyBoot.bind(listenPort).sync();
        } catch (Throwable ex) {
            log.error("{bindPort:" + listenPort + "}", ex);
            throw new RuntimeException(ex);
        }
    }

    private void createProvider(Class<?> clazz, Object bean) {
        StubSkeletonHelper.createProvider(clazz, bean, rpcContext);
    }

}
