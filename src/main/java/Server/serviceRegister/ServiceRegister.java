package Server.serviceRegister;

import java.net.InetSocketAddress;

/**
 * 服务注册接口
 */
public interface ServiceRegister {
    // 注册服务
    void register(String interfaceName, InetSocketAddress serviceAddress);
}
