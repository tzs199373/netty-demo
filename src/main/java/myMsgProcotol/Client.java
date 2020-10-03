package myMsgProcotol;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

public class Client {

    public static void main(String[] args) {

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new MyDecoder());
                            p.addLast(new MyEncoder());
                            p.addLast(new ChannelInboundHandlerAdapter(){
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    StringBuilder sb = new StringBuilder();
                                    for (int i = 0; i < 1000; i++) {
                                        sb.append("abcd");
                                    }
                                    String s = sb.toString();

                                    ctx.writeAndFlush(new MyMessage(new MyHead(s.getBytes("UTF-8").length,1),s));

                                }

                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

                                    ByteBuf in = (ByteBuf) msg;
                                    try {
                                        System.out.println("client get :" + in.toString(CharsetUtil.UTF_8));

                                        ctx.close();
                                    } finally {
                                        //ByteBuf是一个引用计数对象，这个对象必须显示地调用release()方法来释放
                                        ReferenceCountUtil.release(msg);
                                    }
                                }
                            });
                        }
                    });

            // Start the client.
            ChannelFuture f = b.connect("127.0.0.1", 8867).sync();

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Shut down the event loop to terminate all threads.
            group.shutdownGracefully();
        }

    }

}
