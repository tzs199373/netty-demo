package concurrencyProblem;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class Client {
    public static void main(String[] args) {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new LineBasedFrameDecoder(10240));
                ch.pipeline().addLast("decoder", new StringDecoder());
                ch.pipeline().addLast("encoder", new StringEncoder());
                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                        System.out.println("client read:"+msg);
                }
                });
            }
            });

            ChannelFuture f = b.connect("127.0.0.1", 2222).sync();
            Channel channel = f.channel();
            for (int i = 0; i < 100; i++) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String name = Thread.currentThread().getName();
                        channel.writeAndFlush(name+System.lineSeparator());
                    }
                });
                t.setName(i+"");
                t.start();
                if(i == 0){
                    Thread.sleep(2000);//保证线程i=0第一个请求服务端
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
