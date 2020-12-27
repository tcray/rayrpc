package com.tcray.rayrpc.core.handler;

import com.tcray.rayrpc.core.connection.ConnectionGroup;
import com.tcray.rayrpc.core.meta.ApiProxyMeta;
import com.tcray.rayrpc.core.stub.StubSkeletonHelper;
import com.tcray.rayrpc.core.utils.Methods;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lirui
 */
@Slf4j
public class RpcInvocationHandler implements InvocationHandler {

	private ConnectionGroup connectionGroup;
	Map<Method, ApiProxyMeta> apiHolder = new HashMap<>();

	public RpcInvocationHandler(ConnectionGroup connectionGroup) {
		super();
		this.connectionGroup = connectionGroup;
	}

	public <T> T generateProxy(Class<?> clazz) {
		String clazzName = clazz.getName();
		Method[] methods = clazz.getMethods() ;
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			String methodSign = Methods.methodSign(method);

			ApiProxyMeta apiMeta = new ApiProxyMeta();
			apiMeta.setItfName(clazzName);
			apiMeta.setMethodSign(methodSign);
			apiMeta.setParameterTypes(method.getParameterTypes());
			apiMeta.setReturnType(method.getGenericReturnType());
			apiHolder.put(method, apiMeta);
		}
		Object proxy = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, this);
		return (T) proxy;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// 过滤掉hashCode()/toString()/equals等本地方法
		if (!StubSkeletonHelper.checkRpcMethod(method)){
			return null ;
		}

		ApiProxyMeta meta = apiHolder.get(method);
		return connectionGroup.sendRpc(meta.getItfName(),meta.getMethodSign(), args, meta.getReturnType(), 300000);
	}

}