package com.tcray.rayrpc.core.meta;

import lombok.Data;

import java.lang.reflect.Type;

/**
 * @author lirui
 */
@Data
public class ApiProxyMeta {
	private String itfName;
	private String methodSign;
	private Type returnType;
	private Class<?>[] parameterTypes;
	private boolean paramTypeGeneric;

}