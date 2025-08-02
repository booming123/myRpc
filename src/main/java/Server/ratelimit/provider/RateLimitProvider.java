package Server.ratelimit.provider;

import Server.ratelimit.RateLimit;
import Server.ratelimit.impl.TokenBucketRateLimitImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * 提供速率相关的服务
 */
public class RateLimitProvider {

    // key为对应的服务接口名称，值是对应的速率限制器实例
     private Map<String, RateLimit> rateLimitMap = new HashMap<>();

    /**
     * 根据接口名称获取对应的速率限制器实例(RateLimit类型)
     * @param interfaceName 接口名称
     * @return 速率限制器实例
     */
     public RateLimit getRateLimit(String interfaceName){
         // 检查rateLimitMap中是否已经有该接口的速率限制器实例
        if(!rateLimitMap.containsKey(interfaceName)){
            // 如果没有，则创建一个新的速率限制器实例
            // 这里我们使用TokenBucketRateLimitImpl实现类，假设它使用令牌桶算法进行速率限制
            RateLimit rateLimit=new TokenBucketRateLimitImpl(100,10);
            // 将新创建的速率限制器存入map中，以接口名称为键
            rateLimitMap.put(interfaceName,rateLimit);
            // 返回新创建的速率限制器
            return rateLimit;
        }
        // 存在，则直接返回
        return rateLimitMap.get(interfaceName);
    }
}
