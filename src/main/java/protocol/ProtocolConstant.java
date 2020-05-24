package protocol;

import lombok.Data;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Data
public class ProtocolConstant {

    public static final int MAGIC_NUMBER = 6666;

    public static final int VERSION = 1;

    public static final Charset UTF_8 = StandardCharsets.UTF_8;

}
