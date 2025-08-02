package Client.serviceCenter;

import java.net.InetSocketAddress;

// 服务中心接口
public interface ServiceCenter {

     InetSocketAddress serviceDiscovery(String serviceName);

     // 判断是否可以重试
     boolean checkRetry(String serviceName);
}
