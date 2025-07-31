package common.serializer.mySerializer;

import java.io.*;

// 序列化器（Java自带的序列化器）
public class ObjectSerializer implements Serializer{
    /**
     * 把obj对象转为字节数组
     * 序列化
     * @param obj 要序列化的对象
     * @return 字节数组
     */
    @Override
    public byte[] serialize(Object obj) {
        byte[] bytes = null;
        // 创建一个内存中的输出流，用于存储序列化后的字节数组
        //ByteArrayOutputStream是一个可变大小的字节数据缓冲区，数据都会写入这个缓冲区中
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // 先把对象转为二进制流,再把对象数据写入字节缓冲区bos
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            // 把对象写出输出流，触发序列化
            oos.writeObject(obj);
            oos.flush();
            // 将字节缓冲区的数据转为字节数组
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * 反序列化
     * @param bytes 字节数组
     * @param messageType 0:RpcRequest 1:RpcResponse
     * @return
     */
    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object obj = null;
        // 将字节数组包装为一个字节输入流
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try {
            // 使用ObjectInputStream包装一个ByteArrayInputStream对象
            ObjectInputStream ois = new ObjectInputStream(bis);
            // 反序列化对象 将字节流数据解析为Java对象
            obj = ois.readObject();
            ois.close();
            bis.close();
        }catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return obj;
    }

    @Override
    public int getType() {
        return 0;
    }
}
