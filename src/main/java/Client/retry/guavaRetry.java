package Client.retry;

import Client.rpcClient.RpcClient;
import common.Message.RpcRequest;
import common.Message.RpcResponse;
import com.github.rholder.retry.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 用于发送重试机制
 */
public class guavaRetry {

    private RpcClient rpcClient;

    public RpcResponse sendServiceWithRetry(RpcRequest request, RpcClient rpcClient){
       this.rpcClient = rpcClient;
       // 创建重试机制
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                // 无论出现什么异常，都进行重试
                .retryIfException()
                // 如果返回结果为error，也进行重试
                .retryIfResult(response -> Objects.equals(response.getCode(), 500))
                // 重试等待策略：等待2秒后重试
                .withWaitStrategy(WaitStrategies.fixedWait(2, TimeUnit.SECONDS))
                // 重试停止策略：最多重试3次
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                // 重试监听器
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        System.out.println("重试第" + attempt.getAttemptNumber() + "次");
                    }
                })
                .build();
        try {
            // retryer.call执行RPC请求，进行重试。
            //传入的 Lambda 表达式是要执行的操作，即 rpcClient.sendRequest(request)，
            // 这会发送请求并返回一个 RpcResponse 对象。
            return retryer.call(() -> rpcClient.sendRequest(request));
        }catch (Exception e){
            e.printStackTrace();
        }
        return RpcResponse.fail();
    }


}
