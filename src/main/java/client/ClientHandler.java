package client;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import protocol.ResponseMessagePacket;

@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<ResponseMessagePacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ResponseMessagePacket responseMessagePacket) throws Exception {
        log.info("接收到响应包,内容:{}", JSON.toJSONString(responseMessagePacket));
        ResponseFuture responseFuture = ContractProxyFactory.RESPONSE_FUTURE_TABLE.get(responseMessagePacket.getSerialNumber());
        if(null != responseFuture){
            responseFuture.putResponse(responseMessagePacket);
        }else {
            log.warn("接收响应包查询ResponseFuture不存在,请求ID:{}", responseMessagePacket.getSerialNumber());
        }
    }
}
