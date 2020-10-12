package file;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.File;
import java.util.List;

public class UpLoadClient {
    public ChannelFuture initClient(String host, int port) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_SNDBUF, 1048576*200)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .group(group).channel(NioSocketChannel.class)
                .handler(new UpLoadClientIntializer());
        return b.connect(host, port).sync();
    }

    public void uploadFile(String uri,File file,Channel channel,String contentType) {
        try {
            HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
            HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "");
            HttpPostRequestEncoder bodyRequestEncoder = new HttpPostRequestEncoder(factory, request, false);
            bodyRequestEncoder.addBodyFileUpload("file", file, contentType, false);
            List<InterfaceHttpData> bodylist = bodyRequestEncoder.getBodyListAttributes();
            HttpRequest request2 = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri);
            HttpPostRequestEncoder bodyRequestEncoder2 = new HttpPostRequestEncoder(factory, request2, true);
            bodyRequestEncoder2.setBodyHttpDatas(bodylist);
            bodyRequestEncoder2.finalizeRequest();
            if(channel.isActive() && channel.isWritable()) {
                channel.writeAndFlush(request2);
                if (bodyRequestEncoder2.isChunked()) {
                    channel.writeAndFlush(bodyRequestEncoder2).awaitUninterruptibly();
                }
                bodyRequestEncoder2.cleanFiles();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class UpLoadClientIntializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("decoder", new HttpResponseDecoder());
            pipeline.addLast("encoder", new HttpRequestEncoder());
            pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
        }
    }

    public static void main(String[] args) throws Exception {
        UpLoadClient client = new UpLoadClient();
        ChannelFuture f = client.initClient("127.0.0.1",8888);
        client.uploadFile("1.zip",new File("C:\\Users\\asus\\Desktop\\1.zip"),f.channel(),"application/x-zip-compressed");
    }
}

