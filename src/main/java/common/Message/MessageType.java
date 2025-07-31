package common.Message;

import lombok.AllArgsConstructor;

/**
 * 消息类型枚举
 */
@AllArgsConstructor
public enum MessageType {
    // 枚举类型 表示request 和 response
    REQUEST(0),
    RESPONSE(1);
    //每个枚举值对应的编码
    private  int code;
    //提供对code值得访问
    public int getCode() {
        return code;
    }
}
