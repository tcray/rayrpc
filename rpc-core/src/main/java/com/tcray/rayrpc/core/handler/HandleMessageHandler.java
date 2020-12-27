package com.tcray.rayrpc.core.handler;

import com.alibaba.fastjson.JSONArray;
import com.tcray.rayrpc.core.meta.MessageType;
import com.tcray.rayrpc.core.meta.RpcContext;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lirui
 */
@Slf4j
public class HandleMessageHandler implements Runnable{

    private final RpcContext rpcContext;
    private final JSONArray jsonArray;
    private final ChannelHandlerContext channelCtx;

    public HandleMessageHandler(RpcContext rpcContext, ChannelHandlerContext ctx, JSONArray jsonArray) {
        this.rpcContext = rpcContext;
        this.jsonArray = jsonArray;
        this.channelCtx = ctx;
    }

    @Override
    public void run() {
        int commandType = jsonArray.getIntValue(0);
        try {
            CommandHandler handler = HandlerHolderHelper.findHandler(MessageType.get(commandType));

            if (handler == null) {
                throw new Exception("handler is null");
            }
            handler.processCommand(rpcContext, channelCtx, jsonArray);
        } catch (Exception e) {
            log.error("{commandType:" + commandType + "}", e);
        }
    }

}
