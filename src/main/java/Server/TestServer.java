package Server;

import Server.provider.ServiceProvider;
import Server.server.impl.SimpleRPCRPCServer;
import Server.server.impl.ThreadPoolRPCRPCServer;
import common.service.UserService;
import common.service.impl.UserServiceImpl;

public class TestServer {
    public static void main(String[] args) {
        // 1. 根据 UserService 创建一个服务实现类
        UserService userService = new UserServiceImpl();
        // 2. 实例化服务注册中心， 服务端通过 ServiceProvider 将服务注册到中心，供客户端查找并调用。
        ServiceProvider serviceProvider = new ServiceProvider();
        // 3. 注册服务到服务注册中心 ，使得客户端能够根据接口名称或标识查找到对应的服务。
        serviceProvider.provideServiceInterface(userService);
        // 4. 实例化并启动 RPC 服务端 ，可以在这里选择使用简单服务端或线程池服务端
          //SimpleRPCRPCServer rpcServer = new SimpleRPCRPCServer(serviceProvider);
        ThreadPoolRPCRPCServer rpcServer2 = new ThreadPoolRPCRPCServer(serviceProvider);
        // 5. 启动服务端
        rpcServer2.start(9999);
    }
}
