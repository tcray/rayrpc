package com.tcray.rayrpc.core.meta;

import com.alibaba.fastjson.JSONObject;
import com.tcray.rayrpc.core.exception.TimeoutException;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;

@Data
public class CallResultFuture {
    private Object result;
    private JSONObject detail;
    final Object lock = new Object();

    private Type returnType;

    private String errorMsg;

    public CallResultFuture(Type returnType) {
        this.returnType = returnType;
    }

    public void waitReturn(long timeoutInMs) {
        synchronized (lock) {
            try {
                lock.wait(timeoutInMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        if (hasException()) {
            throw new RuntimeException(errorMsg);
        }

        if (result == null && returnType != null) {
            throw new TimeoutException("{timeoutInMs:" + timeoutInMs + "}");
        }
        if (result instanceof Throwable) {
            Throwable e = (Throwable) result;
            throw new RuntimeException(e);
        }
    }

    public void putResultAndReturn(Object result) {
        this.result = result;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void returnWithVoid() {
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void returnWithErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public boolean hasException() {
        return StringUtils.isEmpty(result) && !StringUtils.isEmpty(errorMsg);
    }

}