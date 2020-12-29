package http.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class Client {
    public static void main(String[] args) {
        new Client().start("key=value");
    }

    public void start(String msg){
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                            .addLast("HttpClientCodec",new HttpClientCodec())
                            .addLast("HttpServerCodec",new HttpServerCodec())
                            .addLast("aggregator", new HttpObjectAggregator(512 * 1024))
                            .addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                    System.out.println("client read:"+msg);
                                }
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    Channel channel = ctx.channel();
                                    channel.writeAndFlush(buildRequest(HttpMethod.POST,"/hello",msg,
                                            new HashMap<String,String>(){{
                                                put(HttpHeaderNames.CONTENT_TYPE.toString(),
                                                        HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString());
                                            }}));
                                }
                            });

                }
            });

            ChannelFuture f = b.connect("127.0.0.1", 2223).sync();
//            Channel channel = f.channel();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static FullHttpRequest buildRequest(HttpMethod method,String uri,String msg,Map<String,String> headerMap) {
        ByteBuf content = Unpooled.copiedBuffer(msg.getBytes(Charset.forName("UTF-8")));
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,method,uri,content);
        HttpHeaders headers = request.headers();
        headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
        .set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        headerMap.forEach((key, value) -> headers.set(key, value));
        return request;
    }
}
