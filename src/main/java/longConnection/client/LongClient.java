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

    public LongClient(int port, String host,String clientId) throws InterruptedException {
        this.port = port;
        this.host = host;
        start(clientId);
    }

    private void start(String clientId) throws InterruptedException {
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
            System.out.println("connect server  成功---------");
            //将客户端ID绑定至Channel
            Attribute<ClientInfo> attr = socketChannel.attr(CLIENT_INFO_ATTRIBUTE_KEY);
            attr.setIfAbsent(new ClientInfo(clientId,new Date()));
        }
    }
    public static void main(String[]args) throws InterruptedException {
        new LongClient(9999,"localhost","clientId001");

        //屏蔽while循环（没有其他读写事件）就会触发心跳
//        while (true){
//            TimeUnit.SECONDS.sleep(3);
//            AskMsg askMsg=new AskMsg();
//            AskParams askParams=new AskParams();
//            askParams.setAuth("authToken");
//            askMsg.setParams(askParams);
//            client.socketChannel.writeAndFlush(askMsg);
//        }
    }
}
