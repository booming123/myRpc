package Server.server.work;

import Server.provider.ServiceProvider;
import common.Message.RpcRequest;
import common.Message.RpcResponse;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * 在多线程环境中接收来自客户端的请求，调用本地服务，并将服务的结果返回给客户端。
 */
@AllArgsConstructor
public class WorkThread implements Runnable{

    private Socket socket; // 建立网络连接
    private ServiceProvider serviceProvider; // 本地服务注册中心

    // 在并发环境下
    @Override
    public void run() {
        try {
            // 1. 将响应数据（RpcResponse）发送给客户端
            // 通过Socket的输出流，创建一个对象输出流，将对象序列化写入底层流
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            // 2. 从客户端的网络连接中接收数据，读取序列化的数据
            // 创建对象输入流 从Socket获取输入流，将字节输入流包装为一个对象输入流（反序列化为Java对象）
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            // 3. 先读取客户端传来的request
            RpcRequest rpcRequest = (RpcRequest) ois.readObject();
            // 4. 再通过反射调用服务获取返回值
            RpcResponse rpcResponse = getResponse(rpcRequest);
            // 向客户端写入response
            oos.writeObject(rpcResponse);
            oos.flush();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
    // 处理客户端发送的请求，并返回Response
    private RpcResponse getResponse(RpcRequest rpcRequest){
        // 1. 得到服务名
        String interfaceName = rpcRequest.getInterfaceName();
        // 2. 得到服务端相应的服务实现类
        Object service = serviceProvider.getService(interfaceName);
        // 3. 通过反射调用方法
        try {
            // A. 通过反射获取方法 getMethod(方法名，方法形参类型)
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsType());
            // B. 调用方法 invoke(对象实例，实际参数), 根据被调用方法的不同，返回值(Object)也不同
            Object invoke = method.invoke(service, rpcRequest.getParams());
            return RpcResponse.sussess(invoke);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            System.out.println("方法执行错误");
            return RpcResponse.fail();
        }

    }
}
