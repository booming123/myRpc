package Client.serviceCenter.balance.impl;

import Client.serviceCenter.balance.LoadBalance;

import java.util.List;
import java.util.Random;

/**
 * 随机 负载均衡算法
 */
public class RandomLoadBalance implements LoadBalance {
    @Override
    public String balance(List<String> addressList) {
        Random random = new Random();
        int choose = random.nextInt(addressList.size());
        System.out.println("随机负载均衡算法，随机选择一个"+choose+"服务节点" );
        return null;
    }

    @Override
    public void addNode(String node) {

    }

    @Override
    public void delNode(String node) {

    }
}
