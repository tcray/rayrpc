package com.tcray.rayrpc.core.exception;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.tcray.rayrpc.core.meta.MessageType;
import com.tcray.rayrpc.core.meta.MsgCommandBase;
import lombok.Data;

@Data
public class ExceptionErrorCommand implements MsgCommandBase {

	private MessageType msgType = MessageType.ERROR;
	private MessageType requestType = MessageType.CALL;
	private long requestId = 0;
	private String details = "{}";
	private String errorUri = "java.lang.RuntimeException";
	private String[] exceptionResult = new String[2];
	
	public ExceptionErrorCommand(long requestId, Throwable ex){
		this.requestId = requestId;
		errorUri = ex.getClass().getName();
		exceptionResult[0] = ex.getMessage();
		Throwable target = ex.getCause();
		exceptionResult[1] = target != null ? target.toString() : null;
	}

	@Override
	public Object[] fieldToArray() {
		return new Object[]{msgType, requestType, requestId, details, errorUri, exceptionResult};
	}

	@Override
	public String toCommandJson() {
		return JSONObject.toJSONString(fieldToArray(), SerializerFeature.WriteClassName);
	}

}