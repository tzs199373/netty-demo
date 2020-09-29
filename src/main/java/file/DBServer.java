package file;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class DBServer{

    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;

    private void startServer(int port) throws Exception {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup);

            b.option(ChannelOption.TCP_NODELAY, true);
            b.option(ChannelOption.SO_TIMEOUT, 60000);
            b.option(ChannelOption.SO_SNDBUF, 1048576*200);

            b.option(ChannelOption.SO_KEEPALIVE, true);

            b.channel(NioServerSocketChannel.class);
            b.childHandler(new DBServerInitializer());

            // �������󶨶˿ڼ���
            ChannelFuture f = b.bind(port).sync();

            System.out.println("������������...");
            // �����������رռ���
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        DBServer server = new DBServer();
        server.startServer(8888);

    }
}

