package Server.ratelimit;

/**
 * 限流接口
 */
public interface RateLimit {
   // 获取访问权限
    boolean getToken();
}
