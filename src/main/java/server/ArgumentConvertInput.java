package server;

import lombok.Data;

import java.lang.reflect.Method;
import java.util.List;

@Data
public class ArgumentConvertInput {
    /**
     * 目标方法
     */
    private Method method;

    /**
     * 方法参数类型列表
     */
    private List<Class<?>> parameterTypes;

    /**
     * 方法参数列表
     */
    private List<Object> arguments;

}
