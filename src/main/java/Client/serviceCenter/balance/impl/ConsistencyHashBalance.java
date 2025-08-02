package Client.serviceCenter.balance.impl;

import Client.serviceCenter.balance.LoadBalance;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 一致性hash
 */
@Slf4j
public class ConsistencyHashBalance implements LoadBalance {

    // 添加虚拟节点，使负载均衡更均衡
    private static final int VIRTUAL_NUM = 10;

    // 虚拟节点分配，key是hash值，value是虚拟节点服务器名称
    private SortedMap<Integer, String> shards = new TreeMap<>();

    // 真实节点列表
    private List<String> realNodes = new LinkedList<>();

    // 模拟初始服务器
    private String[] servers = null;

    /**
     * 初始化
     * @param serviceList 服务列表
     */
    private void init(List<String> serviceList){
        for(String server : serviceList){
            realNodes.add(server);
            log.info("真实节点["+server+"] 被添加...");
            //遍历 serviceList（真实节点列表），
            // 每个真实节点都会生成 VIRTUAL_NUM 个虚拟节点，并计算它们的哈希值
            for (int i = 0; i < VIRTUAL_NUM; i++) {
                String virtualNode = server + "_VN" + i;
                // 计算每个虚拟节点的哈希值，然后将虚拟节点添加到 TreeMap 中
                int hash = getHash(virtualNode);
                // shards 是一个 SortedMap，会根据哈希值对虚拟节点进行排序。
                shards.put(hash, virtualNode);
                log.info("虚拟节点["+virtualNode+"] hash:" + hash + "被添加...");
            }
        }
    }

    /**
     * 获取被分配的节点名
     * @param node 目标节点，待处理的请求
     * @param serviceList 服务列表
     * @return 被分配的真实节点名
     */
    public String getServer(String node, List<String> serviceList){
        //首先调用 init(serviceList) 初始化哈希环（即真实节点和虚拟节点）
        init(serviceList);
        // 获取节点的哈希值
        int hash = getHash(node);
        // 获取大于等于该哈希值的第一个节点
        // tailMap 返回的是一个 一个所有的键都大于等于 hash的键值对，value 是对应的 value。
        SortedMap<Integer, String> subMap = shards.tailMap(hash);
        Integer key = null; // 下一个选择的节点的key
        // 如果没有找到，说明该请求的哈希值大于所有虚拟节点的哈希值，则选择哈希值最大的虚拟节点
        // 否则，选择tailMap中第一个虚拟节点
        if (subMap.isEmpty()){
            key = shards.lastKey(); // 哈希值最大的虚拟节点
        }else{
            key = subMap.firstKey(); // subMap中的第一个虚拟节点
        }
        //返回真实节点：
        // 从选中的虚拟节点 virtualNode 中提取出真实节点的名称（即虚拟节点名称去掉 _VN<i> 部分）。
        String virtualNode = shards.get(key);
        return virtualNode.substring(0, virtualNode.indexOf("_"));
    }

    /**
     * 模拟负载均衡，
     * 从一个服务器地址列表中根据一定的规则
     *（在这个例子中是通过生成一个随机的 UUID）选择一个服务器
     * @param addressList 服务器列表
     * @return 选中的服务器地址
     */
    @Override
    public String balance(List<String> addressList) {
        // 生成一个唯一的 UUID（通用唯一标识符）
        // 将 UUID 转换为字符串形式。
        // 返回的是一个 32 字符的十六进制字符串，通常用于标识符或哈希值。
        String random = UUID.randomUUID().toString();
        return getServer(random, addressList);
    }

    /**
     * 添加一个新的真实节点及其虚拟节点到哈希环中
     * @param node 新插入的真实节点
     */
    @Override
    public void addNode(String node) {
        if(!realNodes.contains(node)){
            realNodes.add(node);
            log.info("真实节点["+node+"] 被上线添加...");
            for (int i = 0; i < VIRTUAL_NUM; i++){
                String virtualNode = node + "_VN" + i;
                int hash = getHash(virtualNode);
                shards.put(hash, virtualNode);
                log.info("虚拟节点["+virtualNode+"] hash:" + hash + "被添加...");
            }
        }
    }

    /**
     * 删除一个真实节点及其虚拟节点
     * @param node
     */
    @Override
    public void delNode(String node) {
        if(realNodes.contains(node)){
            realNodes.remove(node);
            log.info("真实节点["+node+"] 被下线删除...");
            for (int i = 0; i < VIRTUAL_NUM; i++){
                String virtualNode = node + "_VN" + i;
                int hash = getHash(virtualNode);
                shards.remove(hash);
                log.info("虚拟节点["+virtualNode+"] hash:" + hash + "被删除...");
            }
        }
    }

    /**
     * FNV1_32_HASH算法
     * 该方法用于计算字符串的哈希值，哈希算法基于一个常见的算法，
     * 使用了 FNV-1a 哈希和一些额外的位运算，确保哈希值均匀分布
     * 这些扰动值（额外的位运算），目的是为了防止相似的输入生成相同的哈希值，避免哈希冲突
     * 增大雪崩效应（输入数据的微小变化可以引起哈希值较大的改变），这样就可以减少碰撞
     * 提升分布均匀性
     * @param node 节点
     * @return 哈希值
     */
    private static int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++)
            hash = (hash ^ str.charAt(i)) * p;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        // 如果算出来的值为负数则取其绝对值
        if (hash < 0)
            hash = Math.abs(hash);
        return hash;
    }
}
