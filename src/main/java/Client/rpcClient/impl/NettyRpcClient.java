package Client.rpcClient.impl;


import Client.netty.NettyClientInitializer.NettyClientInitializer;
import Client.netty.serviceCenter.ServiceCenter;
import Client.netty.serviceCenter.ZKServiceCenter;
import Client.rpcClient.RpcClient;
import common.Message.RpcRequest;
import common.Message.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;

/**
 * netty版的处理客户端通信的代码
 */
public class NettyRpcClient implements RpcClient {

    // 定义整合netty组件的辅助类
    private static final Bootstrap bootstrap;

    // 定义线程组：专门用来处理网络事件（连接、读写）
    private static final EventLoopGroup eventLoopGroup;

    // 服务中心
    private ServiceCenter serviceCenter;

    public NettyRpcClient(){
       // 从固定端口号和地址改为 zk 传入
       this.serviceCenter = new ZKServiceCenter();
    }

    // static保证只初始化一次，减少资源消耗
    static {
        // NIO 事件循环组
         // 每个线程绑定一个 Selector，能高效处理多路复用的 I/O 事件。
        eventLoopGroup = new NioEventLoopGroup();
        // 创建一个 Netty 客户端的启动器对象（Bootstrap 专用于客户端）
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                 .channel(NioSocketChannel.class) //指定NIO Socket通道作为客户端连接通道
                 .handler(new NettyClientInitializer()); // 配置 pipeline 处理逻辑
    }

    @Override
    public RpcResponse sendRequest(RpcRequest request) {
        // 从注册中心获取host和 port
        InetSocketAddress address = serviceCenter.serviceDiscovery(request.getInterfaceName());
        String host = address.getHostName();
        int port = address.getPort();
        try {
            // 向服务端发起连接，阻塞直到连接成功或失败
            // sync方法表示堵塞直到connect完成
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            // 获取与远程服务器建立的 Channel
            Channel channel = channelFuture.channel();
            // 发送数据
            channel.writeAndFlush(request);
            // 这是一个阻塞操作，直到连接被关闭。在这里，它等待服务端返回结果后，客户端的连接才会关闭。
            channel.closeFuture().sync();
            // 获取服务端返回的响应(通过key获取)
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("RPCResponse");
            RpcResponse response = channel.attr(key).get();
            System.out.println("RpcResponse：" + response);
            return response;
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return null;
    }
}
