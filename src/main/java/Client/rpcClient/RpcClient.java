package Client.rpcClient;

import common.Message.RpcRequest;
import common.Message.RpcResponse;

/**
 * 用于定义客户端发送通信
 */
public interface RpcClient {
    RpcResponse sendRequest(RpcRequest request);
}
