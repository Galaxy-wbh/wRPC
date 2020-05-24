package server;

import lombok.Data;

@Data
public class HostClassMethodInfo {
    private Class<?> hostClass;

    private Class<?> hostUserClass;

    private Object hostTarget;
}
