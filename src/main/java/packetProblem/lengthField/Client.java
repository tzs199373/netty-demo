package packetProblem.lengthField;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class Client {
    public static void main(String[] args) {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Channel channel = null;
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast("decoder", new StringDecoder());
//                ch.pipeline().addLast("encoder", new StringEncoder());
                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                        System.out.println("client read:"+msg);
                }
                });
            }
            });

            ChannelFuture f = b.connect("127.0.0.1", 2222).sync();
            channel = f.channel();
            StringBuilder msg = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                msg.append("server hello.");
            }

            ByteBuf buf = Unpooled.buffer(1304);
            buf.writeInt(1300);
            buf.writeBytes(msg.toString().getBytes());

            channel.writeAndFlush(buf);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
