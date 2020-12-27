package com.tcray.rayrpc.core.codec;

import com.tcray.rayrpc.core.meta.MsgCommandBase;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

/**
 * @author lirui
 */
@Sharable
public class MsgToByteEncoder extends MessageToByteEncoder<MsgCommandBase> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, MsgCommandBase msgCommandBase, ByteBuf byteBuf) throws Exception {
        byteBuf.writeCharSequence(msgCommandBase.toCommandJson(), StandardCharsets.UTF_8);
    }

}
