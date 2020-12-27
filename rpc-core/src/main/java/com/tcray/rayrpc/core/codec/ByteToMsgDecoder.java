package com.tcray.rayrpc.core.codec;

import com.alibaba.fastjson.JSONArray;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * @author lirui
 */
@Sharable
public class ByteToMsgDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        final byte[] array;
        final int offset;
        final int length = byteBuf.readableBytes();

        if (byteBuf.hasArray()) {
            array = byteBuf.array();
            offset = byteBuf.arrayOffset() + byteBuf.readerIndex();
        } else {
            array = new byte[length];
            byteBuf.getBytes(byteBuf.readerIndex(), array, 0, length);
            offset = 0;
        }


        JSONArray jsonArray = JSONArray.parseArray(new String(array, offset, length, "UTF-8"));
        list.add(jsonArray);
    }
}
