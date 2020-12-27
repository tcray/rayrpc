package com.tcray.rayrpc.core.stub;

import com.tcray.rayrpc.core.connection.ConnectionGroup;
import com.tcray.rayrpc.core.handler.RpcInvocationHandler;
import com.tcray.rayrpc.core.meta.ProviderMeta;
import com.tcray.rayrpc.core.meta.RpcContext;
import com.tcray.rayrpc.core.utils.Methods;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;

/**
 * @author lirui
 */
public class StubSkeletonHelper {

    public static void createProvider(Class<?> clazz, Object serviceImpl, RpcContext rpcContext) {
        String clazzName = clazz.getName();
        Class<?> callClass = serviceImpl.getClass();

        Method[] methodList = callClass.getMethods();
        for (Method method : methodList) {
            if (!checkRpcMethod(method)) {
                continue;
            }
            ProviderMeta providerMeta = buildProviderMeta(method, serviceImpl);

            MultiValueMap<String, ProviderMeta> providerHolder = rpcContext.getProviderHolder();
            providerHolder.add(clazzName, providerMeta);
        }
    }

    private static ProviderMeta buildProviderMeta(Method method, Object serviceImpl) {
        String methodSign = Methods.methodSign(method);
        ProviderMeta providerMeta = new ProviderMeta();
        providerMeta.setMethod(method);
        providerMeta.setServiceImpl(serviceImpl);
        providerMeta.setMethodSign(methodSign);
        return providerMeta;
    }

    public static boolean checkRpcMethod(final Method method) {
        //本地方法不代理
        if ("toString".equals(method.getName()) ||
                "hashCode".equals(method.getName()) ||
                "notifyAll".equals(method.getName()) ||
                "equals".equals(method.getName()) ||
                "wait".equals(method.getName()) ||
                "getClass".equals(method.getName()) ||
                "notify".equals(method.getName())) {
            return false;
        }
        return true;
    }

    public static <T> T createConsumer(ConnectionGroup connectionGroup, Class<?> clazz, RpcContext rpcContext) {
        String clazzName = clazz.getName();
        RpcInvocationHandler proxyHandler = rpcContext.getConsumerHolder().get(clazzName);
        if (proxyHandler == null) {
            proxyHandler = new RpcInvocationHandler(connectionGroup);
            rpcContext.getConsumerHolder().put(clazzName, proxyHandler);
        }
        return proxyHandler.generateProxy(clazz);
    }

}
