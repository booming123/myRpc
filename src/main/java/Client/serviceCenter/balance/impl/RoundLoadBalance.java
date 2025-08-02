package Client.serviceCenter.balance.impl;

import Client.serviceCenter.balance.LoadBalance;

import java.util.List;

/**
 * 轮询负载均衡
 */
public class RoundLoadBalance implements LoadBalance {

    // 表示当前选择的服务节点的索引(-1表示还未选择过任何节点)
    private int choose = -1;

    @Override
    public String balance(List<String> addressList) {
        choose++;
        choose = choose % addressList.size();
        System.out.println("轮询负载均衡选择第" + choose + "个节点");
        return addressList.get(choose);
    }

    @Override
    public void addNode(String node) {

    }

    @Override
    public void delNode(String node) {

    }
}
