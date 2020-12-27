package com.tcray.rayrpc.core.meta;

import com.tcray.rayrpc.core.handler.RpcInvocationHandler;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @author lirui
 */
public class RpcContext {

    @Getter
    private MultiValueMap<String, ProviderMeta> providerHolder = new LinkedMultiValueMap<>();

    @Getter
    @Setter
    private ExecutorService executePool;

    @Getter
    @Setter
    private Map<Long, CallResultFuture> requestPool = new ConcurrentHashMap<>();

    @Getter
    @Setter
    private Map<String, RpcInvocationHandler> ConsumerHolder = new HashMap<>();

}
