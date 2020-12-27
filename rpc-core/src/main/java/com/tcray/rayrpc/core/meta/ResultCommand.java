package com.tcray.rayrpc.core.meta;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Data;

/**
 * @author lirui
 */
@Data
public class ResultCommand implements MsgCommandBase {

	private long requestId = 0;
	private Object[] yieldResult = new Object[1];

	public ResultCommand() {

	}

	public ResultCommand(long requestId, Object result) {
		this.requestId = requestId;
		yieldResult[0] = result;
	}

	@Override
	public Object[] fieldToArray() {
		return new Object[] { MessageType.RESULT.getType(), requestId, yieldResult };
	}

	@Override
	public String toCommandJson() {
		return JSONObject.toJSONString(fieldToArray(), SerializerFeature.WriteClassName);
	}

}
