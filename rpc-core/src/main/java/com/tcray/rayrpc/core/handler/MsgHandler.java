package com.tcray.rayrpc.core.handler;

import com.alibaba.fastjson.JSONArray;
import com.tcray.rayrpc.core.meta.RpcContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author lirui
 */
@Sharable
public class MsgHandler extends ChannelInboundHandlerAdapter {

    private RpcContext rpcContext;

    public MsgHandler(RpcContext rpcContext) {
        this.rpcContext = rpcContext;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof JSONArray)) {
            super.channelRead(ctx, msg);
        }
        JSONArray jsonArray = (JSONArray) msg;
        HandleMessageHandler handleMessageHandler = new HandleMessageHandler(rpcContext, ctx, jsonArray);

        rpcContext.getExecutePool().submit(handleMessageHandler);
    }
}
