package Client;

import Client.proxy.ClientProxy;
import common.pojo.User;
import common.service.UserService;

public class TestClient {
    public static void main(String[] args) {
        //创建 ClientProxy 对象：初始化 ClientProxy 对象，连接到指定的服务器地址和端口。
     //   ClientProxy clientProxy=new ClientProxy("127.0.0.1",8999,0);
        ClientProxy clientProxy = new ClientProxy();
        // 通过服务发现获取服务端地址
        UserService proxy = clientProxy.getProxy(UserService.class);

        User user = proxy.getUserByUserId(1);
        System.out.println("从服务端得到的user="+user.toString());

        User u=User.builder().id(100).userName("wkk").sex(true).build();
        Integer id = proxy.insertUserId(u);
        System.out.println("向服务端插入user的id"+id);
    }
}