package protocol.serialize;

import com.alibaba.fastjson.JSON;


public enum FastJsonSerializer implements Serializer {
    // 单例
    X;

    @Override
    public byte[] encode(Object target) {
        return JSON.toJSONBytes(target);
    }

    @Override
    public Object decode(byte[] bytes, Class<?> targetClass) {
        return JSON.parseObject(bytes, targetClass);
    }
}
