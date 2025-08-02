package Server.serviceRegister.impl;

import Server.serviceRegister.ServiceRegister;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;

/**
 * 服务注册接口实现类
 */
public class ZKServiceRegister implements ServiceRegister {

    // curator 提供的zookeeper客户端
    private CuratorFramework client;
    //zookeeper根路径节点
    private static final String ROOT_PATH = "MyRPC";

    private static final String RETRY = "CanRetry";

    // 负责zookeeper客户端的初始化，并与zookeeper服务端建立连接
    public ZKServiceRegister(){
        // 指数时间重试的策略
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        this.client = CuratorFrameworkFactory
                .builder()
                // 连接到zookeeper的地址
                .connectString("127.0.0.1:2181")
                // 设置会话超时时间（表示客户端与 Zookeeper 服务器之间保持连接的最长时间。）
                .sessionTimeoutMs(4000)
                // 发生异常时的重试策略
                .retryPolicy(policy)
                // 设置了 Zookeeper 客户端的命名空间
                .namespace(ROOT_PATH)
                .build();
        this.client.start();
        System.out.println("zookeeper启动成功");
    }

    // 注册服务到注册中心
    @Override
    public void register(String serviceName, InetSocketAddress serviceAddress,
                         boolean canRetry){
        try {
            // 如果根下不存在名为serviceName的节点，则创建永久节点（下线时也不会被删除）
            if(client.checkExists().forPath("/"+serviceName) == null){
                client.create()
                        // 如果父节点不存在，自动创建父节点。
                        .creatingParentsIfNeeded()
                        // 创建永久节点
                        .withMode(CreateMode.PERSISTENT)
                        // 指定节点路径为 "/serviceName"，也就是服务名称所对应的路径。
                        .forPath("/"+serviceName);
            }
            // 路径地址，一个/代表一个节点
            String path = "/" + serviceName +"/"+
                    getServiceAddress(serviceAddress);
            // 临时节点，服务器下线就删除节点
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(path);
            // 如果这个服务是幂等性，就增加到节点中
            if (canRetry){
                path = "/" + RETRY +"/"+ serviceName;
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(path);
            }
        }catch (Exception e){
            System.out.println("该服务已存在");
        }
    }

    // 地址 -> XXX.XXX.XXX.XXX:port 字符串
    private String getServiceAddress(InetSocketAddress serverAddress) {
        return serverAddress.getHostName() +
                ":" +
                serverAddress.getPort();
    }
    // 字符串解析为地址
    private InetSocketAddress parseAddress(String address) {
        String[] result = address.split(":");
        return new InetSocketAddress(result[0], Integer.parseInt(result[1]));
    }
}
