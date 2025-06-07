package Client;

import common.Message.RpcRequest;
import common.Message.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class IOClient {
    //这里负责底层与服务端的通信，发送request，返回response
    public static RpcResponse sendRequest(String host, int port, RpcRequest request){//服务端的主机IP地址、端口号、请求对象
        try {
            Socket socket=new Socket(host, port);//通过socket与服务端建立 TCP 连接。
            //将对象序列化发送到服务端。
            ObjectOutputStream oos=new ObjectOutputStream(socket.getOutputStream());
            //接收并反序列化对象
            ObjectInputStream ois=new ObjectInputStream(socket.getInputStream());
            //将 RpcRequest 对象序列化，并通过输出流发送到服务端。
            oos.writeObject(request);
            //刷新输出流以确保数据完全发送
            oos.flush();

            //从输入流中读取服务端返回的序列化对象，并反序列化为 RpcResponse。
            RpcResponse response=(RpcResponse) ois.readObject();
            return response;
            //与网络通信相关的异常、反序列化对象时找不到对应类的异常
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}