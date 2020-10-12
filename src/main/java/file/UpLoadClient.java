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
import io.netty.util.CharsetUtil;

import java.io.File;
import java.util.List;

public class UpLoadClient {
    private EventLoopGroup group = null;
    private HttpDataFactory factory = null;
    private ChannelFuture future = null;

    public UpLoadClient(String host, int port) throws Exception {
        group = new NioEventLoopGroup();
        factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
        Bootstrap b = new Bootstrap();
        b.option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_SNDBUF, 1048576*200)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .group(group).channel(NioSocketChannel.class)
                .handler(new UpLoadClientIntializer());
        future = b.connect(host, port).sync();
    }

    public void uploadFile(String path) {
        File file = new File(path);
        try {
            HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "");
            HttpPostRequestEncoder bodyRequestEncoder = new HttpPostRequestEncoder(factory, request, false);
            bodyRequestEncoder.addBodyFileUpload("file", file, "application/x-zip-compressed", false);
            List<InterfaceHttpData> bodylist = bodyRequestEncoder.getBodyListAttributes();
            HttpRequest request2 = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, file.getName());
            HttpPostRequestEncoder bodyRequestEncoder2 = new HttpPostRequestEncoder(factory, request2, true);
            bodyRequestEncoder2.setBodyHttpDatas(bodylist);
            bodyRequestEncoder2.finalizeRequest();
            Channel channel = this.future.channel();
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
        UpLoadClient client = new UpLoadClient("127.0.0.1",8888);
        client.uploadFile("C:\\Users\\asus\\Desktop\\image\\meout.jpg");
    }
}

