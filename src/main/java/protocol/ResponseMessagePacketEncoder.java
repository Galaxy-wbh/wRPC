package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.RequiredArgsConstructor;
import protocol.serialize.Serializer;

import java.util.Map;

@RequiredArgsConstructor
public class ResponseMessagePacketEncoder extends MessageToByteEncoder<ResponseMessagePacket> {

    private final Serializer serializer;

    @Override
    protected void encode(ChannelHandlerContext ctx, ResponseMessagePacket packet, ByteBuf out) throws Exception {
        // 魔数
        out.writeInt(packet.getMagicNumber());
        // 版本
        out.writeInt(packet.getVersion());
        // 流水号
        out.writeInt(packet.getSerialNumber().length());
        out.writeCharSequence(packet.getSerialNumber(), ProtocolConstant.UTF_8);
        // 消息类型
        out.writeByte(packet.getMessageType().getType());
        // 附件size
        Map<String, String> attachments = packet.getAttachments();
        out.writeInt(attachments.size());
        // 附件内容
        attachments.forEach((k, v) -> {
            out.writeInt(k.length());
            out.writeCharSequence(k, ProtocolConstant.UTF_8);
            out.writeInt(v.length());
            out.writeCharSequence(v, ProtocolConstant.UTF_8);
        });
        // error code
        out.writeLong(packet.getErrorCode());
        // message
        String message = packet.getMessage();
        ByteBufferUtils.X.encodeUtf8CharSequence(out, message);
        // payload
        byte[] bytes = serializer.encode(packet.getPayload());
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}