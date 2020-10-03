package myMsgProcotol;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.ReferenceCountUtil;

public class Server {
    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // (2)
        int port = 8867;
        try {
            ServerBootstrap b = new ServerBootstrap(); // (3)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (4)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (5)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 4, 0))
                                    .addLast(new MyEncoder())
                                    .addLast(new MyDecoder())
                                    .addLast( new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                            MyMessage in = (MyMessage) msg;
                                            try {
                                                System.out.println("server get :" + in);
                                            } finally {
                                                //ByteBuf是一个引用计数对象，这个对象必须显示地调用release()方法来释放
                                                ReferenceCountUtil.release(msg);
                                            }
                                        }
                                    });
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (6)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (7)

            ChannelFuture f = b.bind(port).sync(); // (8)

            System.out.println("start server....");
            f.channel().closeFuture().sync();
            System.out.println("stop server....");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            System.out.println("exit server....");
        }
    }

}
