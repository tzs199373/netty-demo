package uploadFile.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

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
                        ch.pipeline().addLast(new HttpServerCodec());
                        ch.pipeline().addLast(new FileServerHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            ChannelFuture f = bootstrap.bind(8081).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private static class FileServerHandler extends SimpleChannelInboundHandler<HttpObject> {
        private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
        private HttpRequest request = null;
        private HttpPostRequestDecoder decoder;
        private String fileName = "1.zip";
        static {
            DiskFileUpload.baseDirectory = System.getProperty("user.dir");//程序运行环境根目录
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
            if (msg instanceof HttpRequest) {
                request = (HttpRequest) msg;
                System.out.println("uri:"+request.uri());
                if (request.method() == HttpMethod.POST) {
                    if (decoder != null) {
                        decoder.cleanFiles();
                        decoder = null;
                    }
                    try {
                        decoder = new HttpPostRequestDecoder(factory, request);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ctx.channel().close();
                        return;
                    }
                }
            }

            if (decoder != null && msg instanceof HttpContent) {
                HttpContent chunk = (HttpContent) msg;
                try {
                    decoder.offer(chunk);
                } catch (Exception e) {
                    e.printStackTrace();
                    ctx.channel().close();
                    return;
                }
                readHttpDataChunkByChunk();
                if (chunk instanceof LastHttpContent) {
                    reset();
                    return;
                }
            }
        }

        private void reset() {
            request = null;
            decoder.destroy();
            decoder = null;
        }

        private void readHttpDataChunkByChunk() throws IOException {
            try {
                while (decoder.hasNext()) {
                    InterfaceHttpData data = decoder.next();
                    if (data != null) {
                        try {
                            writeHttpData(data);
                        } finally {
                            data.release();
                        }
                    }
                }
            } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {
                System.out.println("end chunk");
            }
        }

        private void writeHttpData(InterfaceHttpData data) throws IOException {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                FileUpload fileUpload = (FileUpload) data;
                if (fileUpload.isCompleted()) {
                    StringBuffer fileNameBuf = new StringBuffer();
                    fileNameBuf.append(DiskFileUpload.baseDirectory)
                        .append(File.separator).append(fileName);
                    fileUpload.renameTo(new File(fileNameBuf.toString()));
                }
            } else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {

            }
        }
    }
}

