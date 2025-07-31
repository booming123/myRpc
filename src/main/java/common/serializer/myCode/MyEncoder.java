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
     * @param ctx netty上下文
     * @param msg 待编码的消息数据
     * @param out netty提供的字节缓存区，编码后的数据将写入该缓存区
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        // 打印消息对象的类名，用于调式编码过程中消息的类型
        System.out.println("消息类型：" + msg.getClass());
        // 判断消息是否是RpcRequest或RpcResponse类型，根据类型写入类型标识
        if(msg instanceof RpcRequest){
            out.writeShort(MessageType.REQUEST.getCode());
        }else if (msg instanceof RpcResponse){
            out.writeShort(MessageType.RESPONSE.getCode());
        }
        //写入当前序列化器的类型标识（short类型）
        out.writeShort(serializer.getType());
        //将消息转化为字符数组
        byte[] serializeBytes = serializer.serialize(msg);
        //写入消息的字节长度
        out.writeInt(serializeBytes.length);
        //将字节数据内容写入输出缓冲区中
        out.writeBytes(serializeBytes);
    }
}
