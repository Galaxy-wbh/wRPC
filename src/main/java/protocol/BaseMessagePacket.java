package protocol;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
@Data
public class BaseMessagePacket implements Serializable {

    /**
     * 魔数
     */
    private int magicNumber;

    /**
     * 版本号
     */
    private int version;

    /**
     * 流水号
     */
    private String serialNumber;

    /**
     * 消息类型
     */
    private MessageType messageType;

    /**
     * 附件 k-v形式
     */
    private Map<String, String> attachments = new HashMap<String, String>();

    /**
     * 添加附件
     */
    public void addAttachment(String key, String value){
        attachments.put(key, value);
    }


}
