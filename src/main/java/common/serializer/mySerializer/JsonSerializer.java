package common.serializer.mySerializer;

import com.alibaba.fastjson.JSONObject;
import common.Message.RpcRequest;
import common.Message.RpcResponse;

// 序列化器 用于Json对象与字节数组的转换
public class JsonSerializer implements Serializer{

    /**
     * 序列化对象
     * @param obj 待序列化的对象
     * @return 序列化后的字节数组
     */
    @Override
    public byte[] serialize(Object obj) {
        // 将对象转为JSON格式的字节数组
        byte[] bytes = JSONObject.toJSONBytes(obj);
        return bytes;
    }

    /**
     * 反序列化对象
     * @param bytes 待反序列化的字节数组
     * @param messageType 待反序列化的对象类型
     * @return 反序列化后的对象
     */
    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object obj = null;
        switch (messageType){
            case 0: // 将字节数组转为RpcRequest对象
                RpcRequest request = JSONObject.parseObject(bytes, RpcRequest.class);
                // 创建一个Object类型的数组，用于存储解析后的请求参数
                Object[] objects = new Object[request.getParams().length];
                // fastJson可以读出基本数据类型，不用转化
                // 对转化后的request的params属性进行类型判断
                for (int i = 0; i < objects.length; i++) {
                    // paramsType是目标参数类型，request.getParamsType()[i]是类型数组，
                    // 每个元素表示参数目标类型
                    // 由RPC框架在调用方法时动态决定
                    Class<?> paramsType = request.getParamsType()[i];
                    //判断每个对象类型是否和paramsTypes中的一致
                    if(!paramsType.isAssignableFrom(request.getParams()[i].getClass())){
                        // 如果不一致，进行类型转化
                        objects[i] = JSONObject.toJavaObject((JSONObject)request.getParams()[i],
                                request.getParamsType()[i]);
                    }else {
                        //如果一致就直接赋给objects[i]
                        objects[i] = request.getParams()[i];
                    }
                }
                request.setParams(objects);
                obj = request;
                break;
            case 1: // 将字节数组转为RpcResponse对象
                RpcResponse response = JSONObject.parseObject(bytes, RpcResponse.class);
                Class<?> dataType = response.getDataType();
                // 判断转化后的response对象中的data的类型是否正确
                if(!dataType.isAssignableFrom(response.getData().getClass())){
                    response.setData(JSONObject.toJavaObject((JSONObject)response.getData(),dataType));
                }
                obj = response;
                break;
            default:
                System.out.println("暂不支持此种消息");
                throw new RuntimeException();
        }
        return obj;
    }

    //1 代表json序列化方式
    @Override
    public int getType() {
        return 1;
    }
}
