package Server.ratelimit.impl;

import Server.ratelimit.RateLimit;

public class TokenBucketRateLimitImpl implements RateLimit {

    // 桶容量
    private static int CAPACITY;

    // 当前桶容量
    private static int curCapcity;

    // 令牌填充速度（表示每隔 RATE 毫秒生成一个令牌）
    private static int RATE;
    // 时间戳(上一次消费令牌的时间)
    private volatile long timeStamp = System.currentTimeMillis();

    public TokenBucketRateLimitImpl(int capacity, int rate) {
        CAPACITY = capacity;
        RATE = rate;
        curCapcity = capacity;
    }
    @Override
    public synchronized boolean getToken() {
        // 1.如果桶内还存在令牌，直接消费一个令牌并返回
        if(curCapcity > 0){
            curCapcity--;
            return true;
        }
        // 2.如果桶内没有令牌，开始计算生成令牌的情况
        long now = System.currentTimeMillis();
        // 这段时间有令牌生成
        if(now - timeStamp >= RATE){
            // 只有当产生的令牌数大于等于2，才把他加入令牌桶中
            // 因为有一个令牌要被新请求消耗
            if ((now - timeStamp) / RATE >= 2) {
                // 这期间新产生的令牌(-1表示算上新请求消耗的令牌)
                long newToken = (now - timeStamp) / RATE - 1;
                curCapcity += newToken;
            }
                if (curCapcity > CAPACITY){
                    curCapcity = CAPACITY;
                }
                // 刷新时间戳
                timeStamp = now;
                return true;
        }
        // 获得不到，返回false
        return false;
    }
}
