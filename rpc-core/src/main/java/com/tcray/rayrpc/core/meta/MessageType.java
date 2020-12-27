package com.tcray.rayrpc.core.meta;

import java.util.Arrays;

/**
 * @author lirui
 */
public enum MessageType {

    /**
     * message type
     */
    CALL(10), RESULT(20), ERROR(0);

    private int type;

    MessageType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static MessageType get(int type) {
        return Arrays.stream(values()).filter(ms -> ms.getType() == type).findFirst().get();
    }

}
