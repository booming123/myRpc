package Server.server;


public interface RpcServer {
    public void start(int port); // 监听端口
    public void stop(); // 停止服务端服务
}
