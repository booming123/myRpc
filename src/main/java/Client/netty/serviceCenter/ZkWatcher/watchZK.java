package Client.netty.serviceCenter.ZkWatcher;

import Client.netty.cache.serviceCache;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

public class watchZK {
    // curator 提供的zookeeper客户端
    private CuratorFramework client;
    //本地缓存
    serviceCache cache;
    public watchZK(CuratorFramework client, serviceCache cache) {
        this.client = client;
        this.cache = cache;
    }

    /**
     * 监听当前节点和子节点的更新，创建和删除
     * @param path 监听的节点路径
     */
    public void watchToUpdate(String path){
        //用于监视指定路径下的节点变化，并在节点变化时更新本地缓存
        //CuratorCache是Curator提供的一个用于监听节点变化的API
        //他会监听指定路径节点变化，这里监听的是根路径/
        CuratorCache curatorCache = CuratorCache.build(client, "/");
        //注册一个监听器，用于处理节点变化事件
        curatorCache.listenable().addListener(new CuratorCacheListener() {
            /**
             * 节点变化事件处理方法
             * @param type 事件类型(枚举类)
             * @param childData 节点更新前的状态和数据
             * @param childData1 节点更新后的状态和数据
             */
            @Override
            public void event(Type type, ChildData childData,
                              ChildData childData1) {
               switch ( type.name()){
                   case "NODE_CREATED": // 当一个节点被创建时，会触发该事件
                       // 解析childData1节点的路径
                       String[] pathList = pasrePath(childData1);
                       if (pathList.length <= 2) break;  // 跳过无效的路径
                       else{ // 路径中包含服务名和地址
                           // 提取服务名
                           String serviceName = pathList[1];
                           // 提取地址
                           String address = pathList[2];
                           // 添加到缓存中
                           cache.addServiceToCache(serviceName, address);
                       }
                       break;
                   // 当节点的内容发生变化时，会触发 NODE_CHANGED 事件。
                   // childData 是更新前的数据，childData1 是更新后的数据。
                   case "NODE_CHANGED": // 节点更新
                      if(childData.getData() != null){
                          System.out.println("修改前的数据: " + new String(childData.getData()));  // 输出更新前的数据
                      }else {
                          System.out.println("节点第一次赋值!");
                      }
                      // 解析更新前的路径
                       String[] oldPathList = pasrePath(childData);
                      // 解析更新后的路径
                       String[] newPathList = pasrePath(childData1);
                       // 替换缓存中的服务地址
                       cache.replaceServiceAddress(oldPathList[1], oldPathList[2], newPathList[2]);
                       System.out.println("修改后的数据: " + new String(childData1.getData()));
                       break;
                   case "NODE_DELETED": // 当一个节点被删除时，会触发该事件
                       // 解析childData节点的路径
                       String[] pathList_d = pasrePath(childData);
                       if (pathList_d.length <= 2) break;  // 跳过无效的路径
                       else{ // 删除缓存中的服务地址
                           // 提取服务名
                           String serviceName=pathList_d[1];
                           // 提取地址
                           String address=pathList_d[2];
                           //将新注册的服务加入到本地缓存中
                           cache.delete(serviceName,address);
                       }
                       break;
                   default:
                       break;
               }
            }
        });
        // 开启监听
        curatorCache.start();

    }

    /**
     * 解析 Zookeeper 节点的路径，
     * 并将路径按照 / 进行分割，返回一个字符串数组。
     * @param childData 该节点的数据
     * @return 使用'/'分隔后的字符串数组
     */
    public String[] pasrePath(ChildData childData){
        //获取更新的节点的路径
        String path=new String(childData.getPath());
        //按照格式 ，读取
        return path.split("/");
    }

}
