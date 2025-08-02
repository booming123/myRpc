package Client.serviceCenter.balance;

import java.util.List;

/**
 * 实现负载均衡的接口
 * 给定服务地址列表，根据不同的负载均衡策略选择一个
 */
public interface LoadBalance {
    // 负责实现具体算法，返回分配的地址
    String balance(List<String> addressList);
    // 添加节点
    void addNode(String node);
    // 删除节点
    void delNode(String node);

}
