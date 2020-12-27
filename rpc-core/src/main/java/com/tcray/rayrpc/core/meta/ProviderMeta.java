package com.tcray.rayrpc.core.meta;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author lirui
 */
@Data
public class ProviderMeta {

    private Object serviceImpl;

    private Method method;

    private String methodSign;

}
