package uploadFile2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.IOException;

import static uploadFile2.Example2.getMultipartData;


public class Client2 {

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
                     ch.pipeline().addLast(new ByteArrayEncoder());
                     ch.pipeline().addLast(new StringEncoder());
                     ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                         @Override
                         public void channelRead(ChannelHandlerContext ctx, Object msg) {
                             System.out.println("client read:"+msg);
                         }
                         @Override
                         public void channelActive(ChannelHandlerContext ctx) throws IOException {
                             getMultipartData(ctx.channel());
                         }
                     });
                 }
             });
            ChannelFuture f = b.connect("192.168.0.105", 8080).sync();
            f.channel().closeFuture().sync();
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }


}

