package Server.server.impl;

import Server.netty.nettyInitializer.NettyServerInitializer;
import Server.provider.ServiceProvider;
import Server.server.RpcServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.AllArgsConstructor;

/**
 * 使用netty实现服务端
 */
@AllArgsConstructor
public class NettyRPCRPCServer implements RpcServer {

    private ServiceProvider serviceProvider;
    @Override
    public void start(int port) {
        // netty 服务线程组boss负责建立连接， work负责具体的请求
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(); // 用于监听客户端的连接请求
        NioEventLoopGroup workGroup = new NioEventLoopGroup(); // 对已建立连接的请求进行处理
        System.out.println("netty服务端启动了");
        try {
            //启动netty服务器
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //初始化
            serverBootstrap
                    // 将之前创建的 bossGroup 和 workGroup 传入，分别用于处理客户端连接和 I/O 操作。
                    .group(bossGroup,workGroup)
                    // 指定使用 NioServerSocketChannel 作为服务器的通道类型。
                    // NioServerSocketChannel 是 Netty 中基于 NIO 的服务器端通道，适用于基于非阻塞 I/O 的 TCP/IP 协议。
                    .channel(NioServerSocketChannel.class)
                    // 指定处理器（ChannelInitializer）来初始化每个新连接的 Channel（每个客户端的连接）。
                    // 在这里，NettyServerInitializer 被用来初始化每个连接。
                    .childHandler(new NettyServerInitializer(serviceProvider));
            //同步堵塞
            // bind(port)是一个异步操作，用于绑定端口。
            // 但是sync（）使该方法变为同步操作，用于等待异步操作完成。
            ChannelFuture channelFuture=serverBootstrap.bind(port).sync();
            //死循环监听
            channelFuture.channel().closeFuture().sync();
        }catch (InterruptedException e){
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() {
        
    }
}
