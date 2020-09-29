package simple;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class Server {
    private int port;

    public Server(int port){this.port = port;}

    public void run()throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup(); //用来接收进来的连接
        EventLoopGroup workerGroup = new NioEventLoopGroup(); //用来处理已经被接收的连接

        try {
            ServerBootstrap bootstrap = new ServerBootstrap(); //启动NIO服务的辅助启动类
            bootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class) //服务端
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {

                            //心跳机制 参数:1.读空闲超时时间 2.写空闲超时时间 3.所有类型的空闲超时时间(读、写) 4.时间单位
                            //在Handler需要实现userEventTriggered方法，在出现超时事件时会被触发
                            socketChannel.pipeline().addLast("idleStateHandler", new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));
                            //设置解码器
                            socketChannel.pipeline().addLast("decoder", new ByteArrayDecoder());
                            socketChannel.pipeline().addLast("channelHandler", new ServerHandler());
                            //设置编码器
                            socketChannel.pipeline().addLast("encoder",new ByteArrayEncoder());

                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture cf = bootstrap.bind(port).sync(); //绑定端口，开始接收进来的连接
            cf.channel().closeFuture().sync(); //等待服务器socket关闭

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args)throws Exception{
        new Server(8081).run();
    }

}
