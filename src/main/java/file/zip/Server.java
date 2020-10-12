package file.zip;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import java.io.IOException;

@Deprecated
public class Server {
    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>(){
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new HttpRequestDecoder());
                        p.addLast(new HttpResponseEncoder());
                        p.addLast(new HttpObjectAggregator(8192));
                        p.addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                if(msg instanceof HttpRequest){
                                    HttpPostRequestDecoder decoder = new HttpPostRequestDecoder((HttpRequest)msg);
                                    while (decoder.hasNext()) {
                                        InterfaceHttpData httpData = decoder.next();
                                        if (httpData instanceof Attribute) {
                                            Attribute attr = (Attribute)httpData;
                                            System.out.println("收到mutlipart属性：" + attr);
                                        } else if (httpData instanceof FileUpload) {
                                            FileUpload fileUpload = (FileUpload)httpData;
                                            System.out.println("收到multipart文件：" + fileUpload.getFilename());
                                            try {
                                                fileUpload.getFile();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    decoder.destroy();
                                }
                            }
                        });

                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.SO_RCVBUF, 1048576*200)//接收缓冲区大小，会限制文件大小
                .option(ChannelOption.SO_SNDBUF, 1048576*200)//接收缓冲区大小，会限制文件大小
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            ChannelFuture f = bootstrap.bind(8888).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
