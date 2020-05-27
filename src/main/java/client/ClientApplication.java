package client;

import com.alibaba.fastjson.JSON;
import contract.HelloService;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import protocol.RequestMessagePacketEncoder;
import protocol.ResponseMessagePacket;
import protocol.ResponseMessagePacketDecoder;
import protocol.serialize.FastJsonSerializer;

@Slf4j
public class ClientApplication {

    public static void main(String[] args) throws Exception {
        int port = 9092;
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(workerGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);
            bootstrap.option(ChannelOption.TCP_NODELAY, Boolean.TRUE);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
                    ch.pipeline().addLast(new LengthFieldPrepender(4));
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    ch.pipeline().addLast(new RequestMessagePacketEncoder(FastJsonSerializer.X));
                    ch.pipeline().addLast(new ResponseMessagePacketDecoder());
                    ch.pipeline().addLast(new ClientHandler());
                }
            });
            ChannelFuture future = bootstrap.connect("localhost", port).sync();
            // 保存Channel实例,暂时不考虑断连重连
            ClientChannelHolder.CHANNEL_REFERENCE.set(future.channel());
            // 构造契约接口代理类实例
            HelloService helloService = ContractProxyFactory.ofProxy(HelloService.class);
            String name = "wbh";
            String result = helloService.sayHello(name);
            log.info("HelloService[{}]调用结果:{}", name, result);
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
