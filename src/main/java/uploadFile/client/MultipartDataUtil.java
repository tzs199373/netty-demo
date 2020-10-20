package uploadFile.client;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MultipartDataUtil {

    public static void postMultipartData(Channel channel) throws IOException {
//        String uri = "/LAPI/V1.0/PACS/GUI/PicFile?Type=2";
        String uri = "/file/upload";

        Map<String,String> fliedMap  = new HashMap<>();
        fliedMap.put("k1","v1");
        fliedMap.put("k2","汉字");

        FilePart filePart = new FilePart("uploadFile","1.zip",
                "application/x-gzip-compressed",new File("C:\\Users\\asus\\Desktop\\1.zip"));
        ArrayList<FilePart> fileParts = new ArrayList<FilePart>(){{
            add(filePart);
        }};

        postMultipartData(channel,uri,fliedMap,fileParts);
    }


    public static void postMultipartData(Channel channel, String uri,Map<String,String> fliedMap,ArrayList<FilePart> fileParts) throws IOException {
        // 分隔符
        String BOUNDARY = "--------------------------328804715201393196989403";
        // 远程ip与port
        InetSocketAddress ipSocket = ((SocketChannel)channel).remoteAddress();
        String  remoteAddress = ipSocket.getAddress().getHostAddress();
        int remotePort = ipSocket.getPort();
        /*******************************报文主体****************************************/
        //多字段
        StringBuilder paramterPart = new StringBuilder();
        for (Map.Entry<String, String> entry : fliedMap.entrySet()) {
            paramterPart.append("--")
                    .append(BOUNDARY)
                    .append("\r\n")
                    .append("Content-Disposition: form-data; name=\""+ entry.getKey() + "\"\r\n\r\n")
                    .append(URLEncoder.encode(entry.getValue()))
                    .append("\r\n");
        }
        //多文件
        long fileTotalSize = 0;
        for (FilePart filePart:fileParts) {
            StringBuilder prev = new StringBuilder()
                    .append("--")
                    .append(BOUNDARY)
                    .append("\r\n")
                    .append("Content-Disposition: form-data; name=\""+filePart.getName()+"\"; filename=\""+filePart.getFileName()+"\"\r\n")
                    .append("Content-Type: " + filePart.getContentType() +"\r\n\r\n");
            String end = "\r\n";
            filePart.setPrev(prev);
            filePart.setEnd(end);
            fileTotalSize += prev.toString().getBytes().length + filePart.getFileSize() + end.getBytes().length;
        }
        //结束标志
        StringBuilder bodyEnd = new StringBuilder("--").append(BOUNDARY).append("--\r\n");

        //报文主体长度
        long contentLength = paramterPart.toString().getBytes().length
                + fileTotalSize + bodyEnd.toString().getBytes().length;

        /*******************************报文首部****************************************/
        //请求行
        StringBuilder head = new StringBuilder("POST ").append(uri).append(" HTTP/1.1").append("\r\n");
        //首部字段
//        head.append("User-Agent: PostmanRuntime/7.26.5").append("\r\n");
//        head.append("Accept: */*").append("\r\n");
//        head.append("Cache-Control: no-cache").append("\r\n");
//        head.append("Postman-Token: 7be7f8eb-a9f5-49b4-85f2-3a228dffbec7").append("\r\n");
        head.append("Host: ").append(remoteAddress).append(":").append(remotePort).append("\r\n");
//        head.append("Accept-Encoding: gzip, deflate, br").append("\r\n");
//        head.append("Connection: keep-alive").append("\r\n");
        head.append("Content-Type: multipart/form-data; boundary=").append(BOUNDARY).append("\r\n");
        head.append("Content-Length: ").append(String.valueOf(contentLength)).append("\r\n");
        head.append("\r\n");

        /*******************************发送与打印****************************************/
        byte[] headByte = head.toString().getBytes();
        channel.write(headByte);
        String headStr = new String(headByte);

        byte[] paramterPartByte = paramterPart.toString().getBytes();
        channel.write(paramterPartByte);
        String paramterPartStr = new String(paramterPartByte);

        String fileString = "";
        for (FilePart filePart:fileParts) {
            byte[] prevByte = filePart.getPrev().toString().getBytes();
            channel.write(prevByte);
            String prevStr = new String(prevByte);

            byte[] fileByte = Files.readAllBytes(filePart.getFile().toPath());
            channel.write(fileByte);
            String fileStr = "文件字节省略";

            byte[] endByte = filePart.getEnd().getBytes();
            channel.write(endByte);
            String endStr = new String(endByte);

            fileString += prevStr+fileStr+endStr;
        }

        byte[] bodyEndByte = bodyEnd.toString().getBytes();
        channel.write(bodyEndByte);
        String bodyEndStr = new String(bodyEndByte);

        System.out.println(headStr+paramterPartStr+fileString+bodyEndStr);
        channel.flush();
    }


}
