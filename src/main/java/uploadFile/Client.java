package uploadFile;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import unv.HttpDecoder;
import unv.HttpEncoder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class Client {
    private static final int READ_TIME_OUT = 120;


    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.channel(NioSocketChannel.class)
             .option(ChannelOption.SO_KEEPALIVE, true)
             .group(group)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ch.pipeline().addLast(new IdleStateHandler(READ_TIME_OUT, READ_TIME_OUT, READ_TIME_OUT, TimeUnit.SECONDS));
                     ch.pipeline().addLast(new ByteArrayEncoder());
//                     ch.pipeline().addLast(new HttpDecoder());
//                     ch.pipeline().addLast(new HttpEncoder());
//                     ch.pipeline().addLast("aggregator", new HttpObjectAggregator(10 * 1024 * 1024));
                     ch.pipeline().addLast(new MessageToMessageEncoder(){
                         @Override
                         protected void encode(ChannelHandlerContext channelHandlerContext, Object o, List list) throws Exception {
                             System.out.println("如果有解码器处理不了什么，记得最后一定要输出(这里是list.add)，不能空实现");
                             list.add(o);
                         }
                     });
                     ch.pipeline().addLast(new ReadTimeoutHandler(READ_TIME_OUT));

                     ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                         @Override
                         public void channelRead(ChannelHandlerContext ctx, Object msg) {
                             System.out.println("client read:"+msg);
                         }
                         @Override
                         public void channelActive(ChannelHandlerContext ctx) throws IOException {
                             MultipartDataUtil.postMultipartData(ctx.channel());
                         }
                     });
                 }
             });
            ChannelFuture f = b.connect("192.168.137.67", 8081).sync();
            f.channel().closeFuture().sync();
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }
}

