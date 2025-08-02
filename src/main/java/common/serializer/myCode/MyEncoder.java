package common.serializer.myCode;

import common.Message.MessageType;
import common.Message.RpcRequest;
import common.Message.RpcResponse;
import common.serializer.mySerializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

/**
 * 自定义编码器
 * 依次按照自定义的消息格式写入，传入的数据为request或者response
 * 需要持有一个serialize器，负责将传入的对象序列化成字节数组
 */
@AllArgsConstructor
public class MyEncoder extends MessageToByteEncoder {

    private Serializer serializer;
    /**
     * 编码
     * @param ctx Netty 上下文对象，提供对通道、事件和处理器的访问。
     * @param msg 需要编码的消息对象。可以是任何 Java 对象，在这里它可能是 RpcRequest 或 RpcResponse。
     * @param out ByteBuf 对象，它是 Netty 的字节缓冲区，用于存储编码后的字节流。
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        // 打印消息对象的类名，用于调式编码过程中消息的类型
        System.out.println("消息类型：" + msg.getClass());
        // 1.判断消息是否是RpcRequest或RpcResponse类型，根据类型写入类型标识
        // 注意这里写入的是Short类型
        if(msg instanceof RpcRequest){
            out.writeShort(MessageType.REQUEST.getCode());
        }else if (msg instanceof RpcResponse){
            out.writeShort(MessageType.RESPONSE.getCode());
        }
        // 2.写入序列化器的类型标识（Short）
        out.writeShort(serializer.getType());
        // 3.将消息对象序列化为字节数组
        byte[] serializeBytes = serializer.serialize(msg);
        // 4.写入消息的字节长度（Int类型）
        out.writeInt(serializeBytes.length);
        // 5.写入消息的字节内容
        out.writeBytes(serializeBytes);
    }
}
