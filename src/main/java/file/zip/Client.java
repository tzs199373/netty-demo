package file.zip;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;

import java.io.File;

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
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new HttpRequestEncoder());
                    p.addLast(new HttpResponseDecoder());
                    p.addLast(new HttpObjectAggregator(8192));
                    p.addLast(new HttpContentDecompressor());
                }
            });

            ChannelFuture f = b.connect("192.168.137.67", 8888).sync();
            uploadZipFile("/upload",new File("C:\\Users\\asus\\Desktop\\1.zip"),f.channel());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void uploadZipFile(String uri,File file,Channel channel) throws Exception {
        DefaultFullHttpRequest multipartRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST,uri);
        HttpPostRequestEncoder encoder = new HttpPostRequestEncoder(multipartRequest, true);
        encoder.addBodyAttribute("key1", "value1");
        encoder.addBodyFileUpload("file",file,"application/x-zip-compressed", false);
        HttpRequest requestToBeSend = encoder.finalizeRequest();
        channel.writeAndFlush(requestToBeSend);
        //发送多个"chunk"，即分段发送多个属性及文件内容
        while (true) {
            HttpContent chunk = encoder.readChunk(channel.alloc());
            if (chunk == null) {
                break;
            }
            channel.writeAndFlush(chunk);
            if (encoder instanceof LastHttpContent) {
                break;
            }
        }
    }
}
