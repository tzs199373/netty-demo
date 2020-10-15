package uploadFile2;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tzs
 * @version 1.0
 * @Description
 * @since 2020/10/15 9:00
 */
public class Example2 {

    public static void getMultipartData(Channel channel) throws IOException {
//        String uri = "/LAPI/V1.0/PACS/GUI/PicFile?Type=2";
        String uri = "/file/upload";
        File file = new File("C:\\Users\\asus\\Desktop\\1.zip");
        buildMultipartData(channel,uri,file);
    }


    public static void buildMultipartData(Channel channel, String uri, File file) throws IOException {

        /*******************************报文主体****************************************/

        StringBuilder body = new StringBuilder();
        String BOUNDARY = "--------------------------328804715201393196989403"; // 分隔符
        //文件
        body.append("--")
                .append(BOUNDARY)
                .append("\r\n")
                .append("Content-Disposition: form-data; name=\"uploadFile\"; filename=\"1.zip\"\r\n")
                .append("Content-Type: application/zip\r\n\r\n");

        //文件内容：二进制

        //结束标志
        StringBuilder bodyEnd = new StringBuilder("\r\n--").append(BOUNDARY).append("--\r\n");

        //报文主体长度
        long contentLength = body.toString().getBytes().length
                + file.length() + bodyEnd.toString().getBytes().length;

        /*******************************报文首部****************************************/
        //请求行
        StringBuilder head = new StringBuilder(HttpMethod.POST.toString())
                .append(" ").append(uri).append(" ").append(HttpVersion.HTTP_1_1.toString()).append("\r\n");
        //首部字段
        head.append("User-Agent: PostmanRuntime/7.26.5").append("\r\n");
        head.append("Accept: */*").append("\r\n");
        head.append("Cache-Control: no-cache").append("\r\n");
        head.append("Postman-Token: 7be7f8eb-a9f5-49b4-85f2-3a228dffbec7").append("\r\n");
        head.append("Host: 192.168.0.105:8080").append("\r\n");
        head.append("Accept-Encoding: gzip, deflate, br").append("\r\n");
        head.append("Connection: keep-alive").append("\r\n");
        head.append("Content-Type: multipart/form-data; boundary=").append(BOUNDARY).append("\r\n");
        head.append("Content-Length: ").append(String.valueOf(contentLength)).append("\r\n");
        head.append("\r\n");

        /*******************************合并****************************************/
        channel.write(head.toString().getBytes());
        channel.write(body.toString().getBytes());
        channel.write(Files.readAllBytes(file.toPath()));
        channel.write(bodyEnd.toString().getBytes());
        System.out.println(head.append(body).append(bodyEnd).toString());
        channel.flush();
    }


}
