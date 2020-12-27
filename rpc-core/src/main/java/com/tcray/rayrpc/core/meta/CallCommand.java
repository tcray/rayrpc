package com.tcray.rayrpc.core.meta;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author lirui
 */
@Data
public class CallCommand implements MsgCommandBase {

	private long requestId = 1;

	private JSONObject options = new JSONObject();

	private String itfName;

	private String methodSign;

	private Object[] args;

	static AtomicLong requestIdPool = new AtomicLong(1);

	public CallCommand(){
		requestId = requestIdPool.incrementAndGet();
	}

	@Override
	public Object[] fieldToArray() {
		return new Object[]{MessageType.CALL.getType(), requestId, options, itfName,methodSign, args};
	}

	@Override
	public String toCommandJson() {
		return JSONObject.toJSONString(fieldToArray());
	}

}