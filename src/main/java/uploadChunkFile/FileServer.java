package uploadChunkFile;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;

public class FileServer{
    private void startServer(int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_TIMEOUT, 60000)
                    .option(ChannelOption.SO_SNDBUF, 1048576*200)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new FileServerInitializer());
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class FileServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        public void initChannel(SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("decoder", new HttpRequestDecoder());
            pipeline.addLast("encoder", new HttpResponseEncoder());
            pipeline.addLast("deflater", new HttpContentCompressor());
            pipeline.addLast("handler", new FileServerHandler());
        }
    }

    private static class FileServerHandler extends SimpleChannelInboundHandler<HttpObject> {
        private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
        private String uri = null;
        private HttpRequest request = null;
        private HttpPostRequestDecoder decoder;
        private int num = 0;
        private String type = "message";

        static {
            DiskFileUpload.baseDirectory = System.getProperty("user.dir");//�������л�����Ŀ¼
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
            System.out.println(num++);//�������֪��һ���ļ������Ϊ�����վ�¼�
            if (msg instanceof HttpRequest) {
                request = (HttpRequest) msg;
                uri = sanitizeUri(request.uri());
                if (request.method() == HttpMethod.POST) {
                    if (decoder != null) {
                        decoder.cleanFiles();
                        decoder = null;
                    }
                    try {
                        decoder = new HttpPostRequestDecoder(factory, request);
                    } catch (Exception e) {
                        e.printStackTrace();
                        writeResponse(ctx.channel(), HttpResponseStatus.INTERNAL_SERVER_ERROR, e.toString());
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
                    writeResponse(ctx.channel(), HttpResponseStatus.INTERNAL_SERVER_ERROR, e.toString());
                    ctx.channel().close();
                    return;
                }
                readHttpDataChunkByChunk();
                if (chunk instanceof LastHttpContent) {
                    writeResponse(ctx.channel(), HttpResponseStatus.OK, "");
                    reset();
                    return;
                }
            }
        }

        private String sanitizeUri(String uri) {
            try {
                uri = URLDecoder.decode(uri, "UTF-8");
            } catch(UnsupportedEncodingException e) {
                try {
                    uri = URLDecoder.decode(uri, "ISO-8859-1");
                } catch(UnsupportedEncodingException e1) {
                    throw new Error();
                }
            }
            return uri;
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
//                fileNameBuf.append(DiskFileUpload.baseDirectory)
//                        .append(uri);
                    fileNameBuf.append(uri);

                    fileUpload.renameTo(new File(fileNameBuf.toString()));
                }
            } else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {

            }
        }

        private void writeResponse(Channel channel, HttpResponseStatus httpResponseStatus, String returnMsg) {
            String resultStr = "�����";
            if(httpResponseStatus.code() == HttpResponseStatus.OK.code()) {
                resultStr += "��������";
                if("message".equals(type)) {
                    resultStr += "�ַ�����";
                } else if("upload".equals(type)) {
                    resultStr += "�ϴ��ļ���";
                } else if("download".equals(type)) {
                    resultStr += "�����ļ�����";
                }
            } else if(httpResponseStatus.code() == HttpResponseStatus.INTERNAL_SERVER_ERROR.code()) {
                resultStr += "����";
                if("message".equals(type)) {
                    resultStr += "�ַ���";
                } else if("upload".equals(type)) {
                    resultStr += "�ϴ��ļ�";
                } else if("download".equals(type)) {
                    resultStr += "�����ļ���";
                }
                resultStr += "�Ĺ����г����쳣��"+returnMsg;
            }
            //��������Ӧ������ת����ChannelBuffer.
            ByteBuf buf = copiedBuffer(resultStr, CharsetUtil.UTF_8);

            //�ж��Ƿ�ر�������Ӧ����
            boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.headers().get(CONNECTION))
                    || request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
                    && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.headers().get(CONNECTION));

            //����������Ӧ����
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus, buf);
            response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

            if (!close) {
                //����������Ӧ��������Ӧ��������Ӧͷ��û�б�Ҫ���'Content-Length'
                response.headers().set(CONTENT_LENGTH, buf.readableBytes());
            }
            //����������Ӧ
            ChannelFuture future = channel.writeAndFlush(response);
            //����������Ӧ����������ر�����
            if (close) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        FileServer server = new FileServer();
        server.startServer(8888);
    }
}