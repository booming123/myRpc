package Server.provider;

import Server.serviceRegister.ServiceRegister;
import Server.serviceRegister.impl.ZKServiceRegister;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * 该类用于实现注册和获取服务
 * 这里的服务是实现类的实例对象
 * 比如 OrderServiceImpl service = new OrderServiceImpl(); 这里的service就是一个服务
 */
public class ServiceProvider {
    // 集合中存放服务的实例，key为实例对象的全类名，value为对应的实例
     private Map<String, Object> interfaceProvider;
     private int port;
     private String host;
     // 注册服务类
    public ServiceRegister serviceRegister;

     public ServiceProvider(String host, int port){
         this.port = port;
         this.host = host;
         this.interfaceProvider = new HashMap<>();
         this.serviceRegister = new ZKServiceRegister();
     }
     // 本地注册服务
    public void provideServiceInterface(Object service, boolean canRetry){ // 接收一个服务实例
         // 1. 获取服务对象的完整类名
        String serviceName = service.getClass().getName();// 获取服务对象的完整类名
        System.out.println("服务对象的完整类名为: "+serviceName);
        // 2. 获取服务对象实现的所有接口
        Class<?>[] interfaces = service.getClass().getInterfaces();
        // 3.遍历所有接口，将他注册到interfaceProvider
        for(Class<?> clazz : interfaces){
            // 本地的映射表
            interfaceProvider.put(clazz.getName(), service);
            // 在注册中心注册服务
            serviceRegister.register(clazz.getName(), new InetSocketAddress(host, port), canRetry);
        }
    }
    // 获取服务
    public Object getService(String interfaceName){
         return interfaceProvider.get(interfaceName);
    }

}
