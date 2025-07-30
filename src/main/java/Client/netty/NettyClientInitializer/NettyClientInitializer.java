package Client.netty.NettyClientInitializer;

import Client.netty.handler.NettyClientHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * 用于初始化客户端的Channel和ChannelPipeline
 * Channel 是网络通信的基本单元，而 ChannelPipeline 是一个用于处理消息的责任链，
 * 它包含了一系列的 ChannelHandler，
 * 每个 ChannelHandler 都负责处理不同的操作，如编码、解码、异常处理等。
 */
public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * ch表示一个SocketChannel对象
     * 代表正在初始化的Channel通道
     * pipline 相当于流水线
     * 每个handler相当于流水线上的工人
     * @param ch
     * @throws Exception
     */
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // pipeline表示这个通道中处理所有入站（读）和出站（写）事件的 Handler 链。
        ChannelPipeline pipeline = ch.pipeline();
                 // 入站解码器：将接收到的 二进制 ByteBuf 流，按协议中指定的 长度字段 进行拆包（切分成完整消息）。
        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4,
                0, 4))
                // 出站解码器：在发送消息时，自动在消息内容前面加上长度字段，用于接收方解码
                .addLast(new LengthFieldPrepender(4))
                // 出站编码器： 把java对象转为Bytebuf流
                .addLast(new ObjectEncoder())
                // 入站解码器： 把Bytebuf流反序列化为java对象
                // 通过解析类名加载对应的Class对象
                .addLast(new ObjectDecoder(new ClassResolver() {
                    @Override
                    public Class<?> resolve(String className) throws ClassNotFoundException {
                        return Class.forName(className);
                    }
                }))
                // 自定义的Netty客户端通信的Handler
                .addLast(new NettyClientHandler());

    }
}
