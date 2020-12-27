package com.tcray.rayrpc.core.meta;

import com.alibaba.fastjson.JSONObject;

/**
 * @author lirui
 */
public interface MsgCommandBase {

    public Object[] fieldToArray();

    public default String toCommandJson(){
        return JSONObject.toJSONString(fieldToArray());
    }

}
