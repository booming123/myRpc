package Client.proxy;

import Client.IOClient;
import Client.rpcClient.RpcClient;
import Client.rpcClient.impl.NettyRpcClient;
import Client.rpcClient.impl.SimpleSocketRpcCilent;
import common.Message.RpcRequest;
import common.Message.RpcResponse;
import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@AllArgsConstructor
public class ClientProxy implements InvocationHandler {

    private RpcClient rpcClient;

//    public ClientProxy(String host, int port, int choose){
//        switch (choose){
//            case 0:
//                rpcClient = new NettyRpcClient(host, port);
//                System.out.println("使用netty版的客户端...");
//                break;
//            case 1:
//                rpcClient = new SimpleSocketRpcCilent(host, port);
//                break;
//        }
//    }

    public ClientProxy(){
        rpcClient = new NettyRpcClient();
    }

    //jdk动态代理，每一次代理对象调用方法，都会经过此方法增强（反射获取request对象，socket发送到服务端）
    //核心逻辑，用于封装请求并处理服务端响应。
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //构建request
        RpcRequest request=RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args).paramsType(method.getParameterTypes()).build();
        //与服务端进行通信，将请求发送出去，并接收 RpcResponse 响应。
        RpcResponse response= rpcClient.sendRequest(request);
        //获取服务端返回的结果，并返回给调用者。
        return response.getData();
    }
    //动态生成一个实现指定接口的代理对象。
    public <T>T getProxy(Class<T> clazz){
        //使用 Proxy.newProxyInstance 方法动态创建一个代理对象，传入类加载器、需要代理的接口和调用处理程序。
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T)o;
    }
}