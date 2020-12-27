package com.tcray.rayrpc.core.handler;

import com.alibaba.fastjson.JSONArray;
import com.tcray.rayrpc.core.meta.MessageType;
import com.tcray.rayrpc.core.meta.RpcContext;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author lirui
 */
public interface CommandHandler {

    void processCommand(RpcContext rpcContext, ChannelHandlerContext channelCtx, JSONArray jsonArray);

    MessageType msgType();

}
