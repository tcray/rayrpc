package com.tcray.rayrpc.core.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tcray.rayrpc.core.exception.ExceptionErrorCommand;
import com.tcray.rayrpc.core.meta.*;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * @author lirui
 */
@Slf4j
public class CallCommandHandler extends AbstractCommandHandler {
    @Override
    public void processCommand(RpcContext rpcContext, ChannelHandlerContext channelCtx, JSONArray jsonArray) {
        CallCommand callCmd = new CallCommand();
        try {
            //数据解析
            long requestId = jsonArray.getLongValue(1);
            JSONObject options = jsonArray.getJSONObject(2);
            String interfaceName = jsonArray.getString(3);
            String methodSign = jsonArray.getString(4);
            String paramValues = jsonArray.getString(5);

            //封装
            callCmd.setRequestId(requestId);
            callCmd.setOptions(options);
            callCmd.setItfName(interfaceName);
            callCmd.setMethodSign(methodSign);

            ProviderMeta provider = findProvider(rpcContext, interfaceName, methodSign);
            if (provider == null) {
                log.info("收到客户端调用接口:{},未找到对应的提供bean,请确认是否未配置提供的bean!", interfaceName);
                ExceptionErrorCommand errCmd = new ExceptionErrorCommand(callCmd.getRequestId(),new Throwable("未找到对应的提供bean,请确认是否未配置提供的bean!") );
                channelCtx.writeAndFlush(errCmd) ;
                return;
            }
            Method method = provider.getMethod();
            Object[] args = JSONObject.parseArray(paramValues, method.getGenericParameterTypes()).toArray();

            Object result = method.invoke(provider.getServiceImpl(), args);

            ResultCommand resultCmd = new ResultCommand(callCmd.getRequestId(), result);
            channelCtx.writeAndFlush(resultCmd);
        } catch (Throwable e) {
            log.error("{procedureUri:'" + callCmd.getItfName() + callCmd.getMethodSign() + "'}", e);
            ExceptionErrorCommand errCmd = new ExceptionErrorCommand(callCmd.getRequestId(), e);
            channelCtx.writeAndFlush(errCmd);
        }
    }

    @Override
    public MessageType msgType() {
        return MessageType.CALL;
    }
}
