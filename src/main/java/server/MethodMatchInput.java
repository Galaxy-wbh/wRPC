package server;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode
public class MethodMatchInput {
    private String interfaceName;

    private String methodName;

    private List<String> methodArgumentSignatures;

    private int methodArgumentArraySize;
}
