package Server.server.impl;

import Server.provider.ServiceProvider;
import Server.server.RpcServer;
import Server.server.work.WorkThread;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 用于启动一个简单的 RPC 服务器，并监听客户端的连接请求，处理客户端请求，并通过线程并发处理每个连接。
 */
@AllArgsConstructor
public class SimpleRPCRPCServer implements RpcServer {
    private ServiceProvider serviceProvider; // 本地服务注册中心
    @Override
    public void start(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("服务器启动了...");
            while(true){
                // 若没有连接，会一直阻塞
                Socket socket = serverSocket.accept();
                // 有连接，则会创建一个新的线程执行处理
                new Thread(()->{
                    new WorkThread(socket, serviceProvider).run();
                }).start();
                new WorkThread(socket, serviceProvider);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {

    }
}
