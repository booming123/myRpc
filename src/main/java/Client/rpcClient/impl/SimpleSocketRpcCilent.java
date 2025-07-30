package Client.rpcClient.impl;

import Client.rpcClient.RpcClient;
import common.Message.RpcRequest;
import common.Message.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 基础版的用于客户端通信的代码
 * sendRequest方法与IOClient方法相同
 */
public class SimpleSocketRpcCilent implements RpcClient {

    private String host;
    private int port;

    public SimpleSocketRpcCilent(String host, int port){
        this.host = host;
        this.port = port;
    }

    @Override
    public RpcResponse sendRequest(RpcRequest request) {
        try {
            Socket socket = new Socket(host, port);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            oos.writeObject(request);
            oos.flush();

            RpcResponse rpcResponse =(RpcResponse) ois.readObject();
            return rpcResponse;
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
