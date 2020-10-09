package longTimeTask;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.net.InetSocketAddress;

public class EchoServer {
    private final int port;
    public EchoServer(int port) {
        this.port = port;
    }
    public void start() throws Exception {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();
        EventExecutorGroup business = new DefaultEventExecutorGroup(200,(Runnable r)->{
            Thread thread =  new Thread(r);
            thread.setDaemon(false);
            thread.setName("netty-context-business");
            return  thread;
        });
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(business, new ChannelInboundHandlerAdapter(){
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                    //模拟耗时任务
                                    try {
                                        Thread.sleep(5000);
                                        System.out.printf("%s execute time 5s \n", Thread.currentThread().getName());
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                    ctx.writeAndFlush(msg );
                                }
                            });
                        }
                    });
            ChannelFuture f = b.bind().sync();
            System.out.println(String.format("%s started and listen on %s", EchoServer.class.getName(), f.channel().localAddress()));
            f.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully().sync();
            worker.shutdownGracefully().sync();
            business.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws Exception {
        new EchoServer(8080).start();
    }
}



