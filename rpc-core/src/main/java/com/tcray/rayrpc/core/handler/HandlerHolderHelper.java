package com.tcray.rayrpc.core.handler;

import com.tcray.rayrpc.core.meta.MessageType;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author lirui
 */
public class HandlerHolderHelper {

    public static CommandHandler findHandler(MessageType messageType) {
        ServiceLoader<CommandHandler> commandHandlers = ServiceLoader.load(CommandHandler.class);
        Iterator<CommandHandler> iterator = commandHandlers.iterator();
        while (iterator.hasNext()) {
            CommandHandler commandHandler =  iterator.next();
            if (messageType.equals(commandHandler.msgType())) {
                return commandHandler;
            }
        }
        return null;
    }
}
