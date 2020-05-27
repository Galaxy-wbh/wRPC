package client;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import exception.SendRequestException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import protocol.*;
import protocol.serialize.FastJsonSerializer;
import protocol.serialize.Serializer;
import util.SerialNumberUtils;

import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class ContractProxyFactory {

    private static final RequestArgumentExtractor EXTRACTOR = new DefaultRequestArgumentExtractor();
    private static final ConcurrentMap<Class<?>, Object> CACHE = Maps.newConcurrentMap();
    static final ConcurrentMap<String/* request id*/, ResponseFuture> RESPONSE_FUTURE_TABLE = Maps.newConcurrentMap();
    //定义请求的最大超市时间为3秒
    private static final long REQUEST_TIMEOUT_MS = 3000;
    private static final ExecutorService EXECUTOR;
    private static final ScheduledExecutorService CLIENT_HOUSE_KEEPER;
    private static final Serializer SERIALIZER = FastJsonSerializer.X;


    @SuppressWarnings("unchecked")
    public static <T> T ofProxy(Class<T> interfaceKlass) {
        // 缓存契约接口的代理类实例
        return (T) CACHE.computeIfAbsent(interfaceKlass, x ->
                Proxy.newProxyInstance(interfaceKlass.getClassLoader(), new Class[]{interfaceKlass}, (target, method, args) -> {
                    RequestArgumentExtractInput input = new RequestArgumentExtractInput();
                    input.setInterfaceKlass(interfaceKlass);
                    input.setMethod(method);
                    RequestArgumentExtractOutput output = EXTRACTOR.extract(input);
                    // 封装请求参数
                    RequestMessagePacket packet = new RequestMessagePacket();
                    packet.setMagicNumber(ProtocolConstant.MAGIC_NUMBER);
                    packet.setVersion(ProtocolConstant.VERSION);
                    packet.setSerialNumber(SerialNumberUtils.X.generateSerialNumber());
                    packet.setMessageType(MessageType.REQUEST);
                    packet.setInterfaceName(output.getInterfaceName());
                    packet.setMethodName(output.getMethodName());
                    packet.setMethodArgumentSignatures(output.getMethodArgumentSignatures().toArray(new String[0]));
                    packet.setMethodArguments(args);
                    Channel channel = ClientChannelHolder.CHANNEL_REFERENCE.get();
                    return sendRequestSync(channel, packet, method.getReturnType());
                }));
    }


    /**
     * 同步发送请求
     * @param channel
     * @param packet
     * @param returnType
     * @return
     */
    static Object sendRequestSync(Channel channel, RequestMessagePacket packet, Class<?> returnType){
        long beginTimestamp = System.currentTimeMillis();
        ResponseFuture responseFuture = new ResponseFuture(packet.getSerialNumber(), REQUEST_TIMEOUT_MS);
        RESPONSE_FUTURE_TABLE.put(packet.getSerialNumber(), responseFuture);
        try{
            Future<ResponseMessagePacket> packetFuture = EXECUTOR.submit(()->{
                channel.writeAndFlush(packet).addListener((ChannelFutureListener)
                        future -> responseFuture.setSendRequestSucceed(true));
                return responseFuture.waitResponse(REQUEST_TIMEOUT_MS- (System.currentTimeMillis()-beginTimestamp));
            });
            ResponseMessagePacket responsePacket = packetFuture.get(
                    REQUEST_TIMEOUT_MS - (System.currentTimeMillis()-beginTimestamp), TimeUnit.MILLISECONDS);
            if(null == responsePacket){
                //超时导致响应包获取失败
                throw new SendRequestException(String.format("ResponseMessagePacket获取超时,请求ID:%s", packet.getSerialNumber()));
            }else{
                ByteBuf payload = (ByteBuf) responsePacket.getPayload();
                byte[] bytes = ByteBufferUtils.X.readBytes(payload);
                return SERIALIZER.decode(bytes, returnType);
            }
        }catch(Exception e){
            log.error("同步发送请求异常,请求包:{}", JSON.toJSONString(packet), e);
            if( e instanceof RuntimeException){
                throw (RuntimeException) e;
            }else{
                throw new SendRequestException(e);
            }
        }
    }

    static void scanResponseFutureTable(){
        log.info("开始执行ResponseFutureTable清理任务...");
        Iterator<Map.Entry<String, ResponseFuture>> iterator = RESPONSE_FUTURE_TABLE.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, ResponseFuture> entry = iterator.next();
            ResponseFuture responseFuture = entry.getValue();
            if(responseFuture.timeout()){
                iterator.remove();
                log.warn("移除过期的请求ResponseFuture,请求ID:{}", entry.getKey());
            }
        }
        log.info("执行ResponseFutureTable清理任务结束..");
    }

    static {
        int n  = Runtime.getRuntime().availableProcessors();
        EXECUTOR = new ThreadPoolExecutor(n*2, n*2, 0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(50), runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            thread.setName("CLIENT_REQUEST_EXECUTOR");
            return thread;
        });
        CLIENT_HOUSE_KEEPER = new ScheduledThreadPoolExecutor(1, runnable->{
           Thread thread = new Thread(runnable);
           thread.setDaemon(true);
           thread.setName("CLIENT_HOUSE_KEEPER");
           return thread;
        });
        CLIENT_HOUSE_KEEPER.scheduleWithFixedDelay(ContractProxyFactory::scanResponseFutureTable, 5, 5, TimeUnit.SECONDS);
    }
}
