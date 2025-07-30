package Client.netty.handler;

import common.Message.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

/**
 * 用来处理服务器响应的处理器
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    /**
     * channelRead0是SimpleChannelInboundHandler 中的核心方法
     * 用于处理接收到的消息
     * 当客户端接收到Response时触发
     * @param ctx 为当前这个 Handler 的上下文对象
     * @param rpcResponse
     * @throws Exception
     * ctx.channel 获取当前的channel连接
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
        // 获取当前的Channel
        Channel channel = ctx.channel();
        // 把数据存在Channel中
        /**
         * AttributeKey<T> 是 Netty 提供的 通道属性键，用于在 Channel 中存放与获取数据。
         * 通过 AttributeKey 可以给 Channel 绑定任意类型的对象
         */
        // 创建一个名为 "RPCResponse" 的 AttributeKey，用于存储 RpcResponse 类型的数据。
        AttributeKey<RpcResponse> key = AttributeKey.valueOf("RPCResponse");
        // channel.attr(key) 获取当前 Channel 的 Attribute（属性容器）
        // .set(rpcResponse) 将 RpcResponse 对象存储进去。
        channel.attr(key).set(rpcResponse);
        // 在接收到响应后，主动关闭当前的连接，采用短连接模式。
        ctx.channel().close();
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //异常处理
        cause.printStackTrace();
        ctx.close();
    }

}
