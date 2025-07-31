package common.serializer.mySerializer;

/*
 * 自定义编码器接口
 * 用于为对象提供序列化和反序列化的功能
 * 通过一个静态工厂方法根据类型代码返回具体的序列化器实例。
 */
public interface Serializer {
     // 把对象序列化成字节数组
    byte[] serialize(Object obj);
    // 把字节数组反序列化成消息（解码）
    Object deserialize(byte[] bytes, int messageType);
    // 返回使用的序列器，是哪个
    // 0：java自带序列化方式, 1: json序列化方式
    int getType();
    //静态工厂方法
    //根据序号取出序列化器，暂时有两种实现方式，需要其他方式，实现这个接口即可
    static Serializer getSerializerByType(int code){
        return switch (code) {
            case 0 -> new ObjectSerializer();
            case 1 -> new JsonSerializer();
            default -> null;
        };
    }
}
