package Client.netty.serviceCenter;

import Client.netty.cache.serviceCache;
import Client.netty.serviceCenter.ZkWatcher.watchZK;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetSocketAddress;
import java.util.List;

// 使用zookeeper作为服务注册中心
public class ZKServiceCenter implements ServiceCenter{

    // curator 提供的zookeeper客户端
    private CuratorFramework client;
    //zookeeper根路径节点
    private static final String ROOT_PATH = "MyRPC";

    private serviceCache cache; // 添加本地缓存

    //负责zookeeper客户端的初始化，并与zookeeper服务端进行连接
    public ZKServiceCenter(){

        // 指数时间重试
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        // zookeeper的地址固定，不管是服务提供者还是，消费者都要与之建立连接
        // sessionTimeoutMs 与 zoo.cfg中的tickTime 有关系，
        // zk还会根据minSessionTimeout与maxSessionTimeout两个参数重新调整最后的超时值。默认分别为tickTime 的2倍和20倍
        // 使用心跳监听状态
        // sessionTimeoutMs：会话超时时间（40s）
        // retryPolicy 重试策略
        // namespace(ROOT_PATH) 设置命名空间，所有操作都在/MyRpc路径下
        this.client = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
                .sessionTimeoutMs(40000).retryPolicy(policy).namespace(ROOT_PATH).build();
        this.client.start();
        System.out.println("zookeeper 连接成功");

        // 初始化本地缓存
        cache = new serviceCache();
        // 加入zookeeper事件监听器
        watchZK watchZK = new watchZK(client, cache);
        // 监听启动
        watchZK.watchToUpdate(ROOT_PATH);
    }
    //根据服务名（接口名）返回地址
    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        try {
            // 先从本地缓存中找
            List<String> serviceList = cache.getServiceFromCache(serviceName);
            // 如果本地缓存中没有，则从zookeeper中获取
            if(serviceList == null){
                serviceList = client.getChildren().forPath("/" + serviceName);
            }

            // 这行代码用于获取指定 serviceName（服务名称）路径下的所有子节点。
            // 每个子节点通常表示一个服务实例的地址，存储的格式一般是 ip:port
//            List<String> strings = client.getChildren().forPath("/" + serviceName);

            // 这里默认用的第一个，后面加负载均衡
            String string = serviceList.get(0);
            return parseAddress(string);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    // 地址 -> XXX.XXX.XXX.XXX:port 字符串
    // 常用于注册服务时写入 ZooKeeper。
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
