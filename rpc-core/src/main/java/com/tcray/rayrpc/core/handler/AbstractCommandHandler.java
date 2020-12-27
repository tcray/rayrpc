package com.tcray.rayrpc.core.handler;

import com.tcray.rayrpc.core.meta.ProviderMeta;
import com.tcray.rayrpc.core.meta.RpcContext;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * @author lirui
 */

public abstract class AbstractCommandHandler implements CommandHandler {

    protected ProviderMeta findProvider(RpcContext rpcContext, String interfaceName, String methodSign) {
        List<ProviderMeta> providerMetas = rpcContext.getProviderHolder().get(interfaceName);
        if (!CollectionUtils.isEmpty(providerMetas)) {
            Optional<ProviderMeta> providerMeta = providerMetas.stream().filter(provider -> methodSign.equals(provider.getMethodSign())).findFirst();
            return providerMeta.orElse(null);
        }
        return null;
    }

}
