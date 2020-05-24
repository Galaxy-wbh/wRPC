package protocol.serialize;

public interface Serializer {
    byte[] encode(Object target);

    Object decode(byte[] bytes, Class<?> targetClass);
}
