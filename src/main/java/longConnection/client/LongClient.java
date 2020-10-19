package longConnection.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.Attribute;
import longConnection.share.module.LoginMsg;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static longConnection.client.Constants.CLIENT_INFO_ATTRIBUTE_KEY;

public class LongClient {
    private int port;
    private String host;
    private Bootstrap bootstrap;

    public LongClient(int port, String host) throws InterruptedException {
        this.port = port;
        this.host = host;
    }

    private void start(String clientId,boolean isOpenHeartBeat) throws InterruptedException {
        LongClient longClient = this;
        EventLoopGroup eventLoopGroup=new NioEventLoopGroup();
        bootstrap=new Bootstrap();
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
                socketChannel.pipeline().addLast(new NettyClientHandler(longClient));
            }
        });
        doConnect(clientId,isOpenHeartBeat);
    }

    protected void doConnect(String clientId,boolean isOpenHeartBeat) throws InterruptedException {
        ChannelFuture future = bootstrap.connect(host,port).sync();

        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture futureListener) throws Exception {
                if (futureListener.isSuccess()) {
                    System.out.println("client["+clientId+"] connect server  成功---------");
                    SocketChannel socketChannel = (SocketChannel)futureListener.channel();
                    //将客户端ID绑定至Channel
                    Attribute<ClientInfo> attr = socketChannel.attr(CLIENT_INFO_ATTRIBUTE_KEY);
                    attr.setIfAbsent(new ClientInfo(clientId,isOpenHeartBeat,new Date()));
                    //登陆请求
                    LoginMsg loginMsg=new LoginMsg();
                    loginMsg.setClientId(clientId);
                    loginMsg.setPassword("yao");
                    loginMsg.setUserName("robin");
                    socketChannel.writeAndFlush(loginMsg);
                } else {
                    System.out.println("Failed to connect to server, try connect after 10s");

                    futureListener.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                doConnect(clientId,isOpenHeartBeat);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 10, TimeUnit.SECONDS);
                }
            }
        });
    }

    public static void main(String[]args) throws InterruptedException {
        new LongClient(9999,"localhost").start("clientId001",false);
    }
}
