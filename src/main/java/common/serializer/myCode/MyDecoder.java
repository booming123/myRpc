package common.serializer.myCode;

import common.Message.MessageType;
import common.serializer.mySerializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

// 自定义的解码器 用于序列化
public class MyDecoder extends ByteToMessageDecoder {

    /**
     * 负责传入的字节流解码为业务对象，并将解码后的对象添加到out中，供下一个handler处理
     * @param ctx Netty 提供的上下文对象，可用于操作 pipeline、channel
     * @param in 接收到的字节流（netty的缓冲区）
     * @param out List对象，用于存储解码后的对象
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 1.从字节流中读取前两个字节作为消息类型（使用short是为了节省空间）
        short messageType = in.readShort();
        // 2.判断消息类型，是否是请求或响应消息
        if (messageType != MessageType.REQUEST.getCode() &&
                messageType != MessageType.RESPONSE.getCode()) {
            System.out.println("暂不支持此种数据");
            return;
        }
        // 3.再读两个字节标识序列化方式
        short serializerType = in.readShort();
        // 4. 获取序列化器
        Serializer serializer = Serializer.getSerializerByType(serializerType);
        if (serializer == null) {
            throw new RuntimeException("不存在对应的序列化器");
        }
        // 5.读取消息体长度（4个字节）
        int length = in.readInt();
        // 6.从字节流ByteBuf中读取length个字节到bytes数组中
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        // 7.反序列化为Java对象
        Object deserialize = serializer.deserialize(bytes, messageType);
        // 8.将解码结果加入到out中
        out.add(deserialize);
    }
}
