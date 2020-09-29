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
        EventLoopGroup bossGroup = new NioEventLoopGroup(); //�������ս���������
        EventLoopGroup workerGroup = new NioEventLoopGroup(); //���������Ѿ������յ�����

        try {
            ServerBootstrap bootstrap = new ServerBootstrap(); //����NIO����ĸ���������
            bootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class) //�����
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {

                            //�������� ����:1.�����г�ʱʱ�� 2.д���г�ʱʱ�� 3.�������͵Ŀ��г�ʱʱ��(����д) 4.ʱ�䵥λ
                            //��Handler��Ҫʵ��userEventTriggered�������ڳ��ֳ�ʱ�¼�ʱ�ᱻ����
                            socketChannel.pipeline().addLast("idleStateHandler", new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));
                            //���ý�����
                            socketChannel.pipeline().addLast("decoder", new ByteArrayDecoder());
                            socketChannel.pipeline().addLast("channelHandler", new ServerHandler());
                            //���ñ�����
                            socketChannel.pipeline().addLast("encoder",new ByteArrayEncoder());

                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture cf = bootstrap.bind(port).sync(); //�󶨶˿ڣ���ʼ���ս���������
            cf.channel().closeFuture().sync(); //�ȴ�������socket�ر�

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
