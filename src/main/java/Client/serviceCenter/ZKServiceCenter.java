package Client.serviceCenter;

import Client.netty.cache.serviceCache;
import Client.serviceCenter.ZkWatcher.watchZK;
import Client.serviceCenter.balance.impl.ConsistencyHashBalance;
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

    private static final String RETRY = "CanRetry";

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
            if(serviceList == null || serviceList.isEmpty()){
                serviceList = client.getChildren().forPath("/" + serviceName);
            }

            // 过滤掉非服务地址的节点（如 CanRetry）
            if(serviceList != null) {
                serviceList = serviceList.stream()
                        .filter(service -> service.contains(":")) // 只保留包含冒号的地址
                        .collect(java.util.stream.Collectors.toList());
            }

            // 检查服务列表是否为空
            if(serviceList == null || serviceList.isEmpty()){
                System.err.println("未找到服务: " + serviceName + "，请确保服务端已启动并注册到ZooKeeper");
                return null;
            }

            System.out.println("过滤后的服务列表: " + serviceList);

            // 负载均衡得到地址
            String address = new ConsistencyHashBalance().balance(serviceList);
            System.out.println("负载均衡选择的地址: " + address);

            if(address == null || address.isEmpty()){
                System.err.println("负载均衡返回null或空地址");
                return null;
            }

            // 检查地址格式
            if(!address.contains(":")){
                System.err.println("地址格式不正确，缺少端口号: " + address);
                return null;
            }

            return parseAddress(address);
        } catch (Exception e) {
            System.err.println("服务发现异常: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断某个服务是否可以重试
     * @param serviceName 服务名
     * @return 是否可以重试
     */
    @Override
    public boolean checkRetry(String serviceName) {
        boolean canRetry = false;
        try {
            // 这个serviceList相当于是白名单
            List<String> serviceList = client.getChildren().forPath("/" + RETRY);
            for(String s : serviceList){
                if(s.equals(serviceName)){
                    canRetry = true;
                    System.out.println("服务 " + serviceName + "在白名单上，可以重试");
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return canRetry;
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
        if(address == null || address.isEmpty()){
            throw new IllegalArgumentException("地址不能为空");
        }
        
        String[] result = address.split(":");
        if(result.length != 2){
            throw new IllegalArgumentException("地址格式不正确，应为 host:port，实际为: " + address);
        }
        
        try {
            return new InetSocketAddress(result[0], Integer.parseInt(result[1]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("端口号格式不正确: " + result[1]);
        }
    }
}
