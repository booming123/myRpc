package Client.netty.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 建立本地存储服务的缓存
public class serviceCache {
    // key：serviceName服务名
    // value：addressList服务提供者的ip地址
    private static Map<String, List<String>> cache = new HashMap<>();

    // 添加服务
    public void addServiceToCache(String serviceName, String address){
        // 如果缓存中已经存在该服务，则添加对应的address
        if(cache.containsKey(serviceName)){
            List<String> addressList = cache.get(serviceName);
            addressList.add(address);
            cache.put(serviceName, addressList);
            System.out.println("将name为"+serviceName+"和地址为"+address+"的服务添加到本地缓存中");
        }else{ // 缓存中不存在该服务
            ArrayList<String> addressList = new ArrayList<>();
            addressList.add(address);
            cache.put(serviceName, addressList);
        }
    }
    // 修改服务地址
    public void replaceServiceAddress(String serviceName, String oldAddress, String newAddress){
        if(cache.containsKey(serviceName)){
            List<String> addressList = cache.get(serviceName);
            addressList.remove(oldAddress);
            addressList.add(newAddress);
            cache.put(serviceName, addressList);
            System.out.println("将name为"+serviceName+"和地址为"+newAddress+"的服务添加到本地缓存中");
        }else { // 缓存中不存在该服务
            System.out.println("修改失败，服务不存在！");
        }
    }
    // 从缓存中获取服务地址
    public List<String> getServiceFromCache(String serviceName){
        // 缓存中不存在该服务,就返回null
        return cache.getOrDefault(serviceName, null);
    }
    // 从缓存中删除服务地址
    public void delete(String serviceName, String address){
        List<String> addressList = cache.get(serviceName);
        addressList.remove(address);
        System.out.println("将name为"+serviceName+"和地址为"+address+"的服务从本地缓存中删除");
    }

}
