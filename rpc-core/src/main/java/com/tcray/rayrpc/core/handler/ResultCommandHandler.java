package com.tcray.rayrpc.core.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tcray.rayrpc.core.meta.CallResultFuture;
import com.tcray.rayrpc.core.meta.MessageType;
import com.tcray.rayrpc.core.meta.RpcContext;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lirui
 */
@Slf4j
public class ResultCommandHandler extends AbstractCommandHandler {
    @Override
    public void processCommand(RpcContext rpcContext, ChannelHandlerContext channelCtx, JSONArray jsonArray) {
        long requestId = jsonArray.getLongValue(1);
        CallResultFuture callResult = rpcContext.getRequestPool().get(requestId);
        if (callResult == null) {
            log.error("{msg:'receive timeout result, maybe server method too slow', requestId:" + requestId + "}");
            return;
        }

        if (callResult.getReturnType() == null) {
            callResult.returnWithVoid();
        } else {
            Object obj = jsonArray.getJSONArray(2).get(0);
            if (JSONObject.class.isAssignableFrom(obj.getClass())){
                JSONObject jsonObj = (JSONObject) obj;
                obj = jsonObj.toJavaObject(callResult.getReturnType());
            }
            Object result = obj;
            callResult.putResultAndReturn(result);
        }
    }

    @Override
    public MessageType msgType() {
        return MessageType.RESULT;
    }
}
