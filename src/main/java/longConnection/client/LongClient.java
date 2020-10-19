package longConnection.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.Attribute;

import java.util.Date;

import static longConnection.client.Constants.CLIENT_INFO_ATTRIBUTE_KEY;

public class LongClient {
    private int port;
    private String host;

    public LongClient(int port, String host) throws InterruptedException {
        this.port = port;
        this.host = host;
    }

    private void start(String clientId,boolean isOpenHeartBeat) throws InterruptedException {
        EventLoopGroup eventLoopGroup=new NioEventLoopGroup();
        Bootstrap bootstrap=new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE,true);
        bootstrap.group(eventLoopGroup);
        bootstrap.remoteAddress(host,port);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new IdleStateHandler(20,10,0));
                socketChannel.pipeline().addLast(new ObjectEncoder());
                socketChannel.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                socketChannel.pipeline().addLast(new NettyClientHandler());
            }
        });
        ChannelFuture future =bootstrap.connect(host,port).sync();
        if (future.isSuccess()) {
            SocketChannel socketChannel = (SocketChannel)future.channel();
            System.out.println("client["+clientId+"] connect server  成功---------");
            //将客户端ID绑定至Channel
            Attribute<ClientInfo> attr = socketChannel.attr(CLIENT_INFO_ATTRIBUTE_KEY);
            attr.setIfAbsent(new ClientInfo(clientId,isOpenHeartBeat,new Date()));
        }
    }
    public static void main(String[]args) throws InterruptedException {
        new LongClient(9999,"localhost").start("clientId001",true);
    }
}
