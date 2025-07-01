package Client.rpcClient.impl;


import Client.rpcClient.RpcClient;
import common.Message.RpcRequest;
import common.Message.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * netty版的处理客户端通信的代码
 */
public class NettyRpcClient implements RpcClient {
    private String host;
    private String port;

    // 定义整合netty组件的工具
    private static final Bootstrap bootstrap;

    // 定义线程组
    private static final EventLoopGroup eventLoopGroup;

    public NettyRpcClient(String host, String port){
        this.host = host;
        this.port = port;
    }

    static {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel()

    }

    @Override
    public RpcResponse sendRequest(RpcRequest request) {
        return null;
    }
}
