package uploadFile;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tzs
 * @version 1.0
 * @Description
 * @since 2020/10/15 9:00
 */
public class Example {
    public static void main(String[] args) throws IOException {
        getMultipartData();
    }


    public static String getMultipartData() throws IOException {
//        String uri = "/LAPI/V1.0/PACS/GUI/PicFile?Type=2";
        String uri = "/file/upload";
        String host = "192.168.2.215";

        File[] files = new File[1];
        File file = new File("C:\\Users\\asus\\Desktop\\1.zip");
        files[0] = file;
        return buildMultipartData(host,uri,files);
    }


    public static String buildMultipartData(String host,String uri,File[] files) throws IOException {
        File file = files[0];
        byte[] fileByte = Files.readAllBytes(file.toPath());

        /*******************************��������****************************************/
        StringBuilder body = new StringBuilder();
        String BOUNDARY = "---------------------------azhHvtQvhg8ujJ"; // �ָ���
        Map<String,String> fliedMap  = new HashMap<>();
//        fliedMap.put("k1","v1");
//        fliedMap.put("k2","v2");

        //ÿ���ֶ�
        for (Map.Entry<String, String> entry : fliedMap.entrySet()) {
            body.append("--")
                    .append(BOUNDARY)
                    .append("\r\n")
                    .append("Content-Disposition: form-data; name=\""+ entry.getKey() + "\"\r\n\r\n")
                    .append(URLEncoder.encode(entry.getValue(),"UTF-8"))
                    .append("\r\n");
        }

        //�ļ�
        for (int i = 0; i < files.length; i++) {
            body.append("--")
                    .append(BOUNDARY)
                    .append("\r\n")
                    .append("Content-Disposition: form-data; name=\"uploadFile\"; filename=\"1_1.zip\"\r\n")
                    .append("Content-Type: application/x-gzip-compressed\r\n\r\n")
                    .append(new String(fileByte));
        }
        //������־
        body.append("\r\n--" + BOUNDARY + "--\r\n");


        /*******************************�����ײ�****************************************/
        //������
        StringBuilder head = new StringBuilder(HttpMethod.POST.toString())
                .append(" ").append(uri).append(" ").append(HttpVersion.HTTP_1_1.toString()).append("\r\n");
        //�ײ��ֶ�
        head.append("Host: ").append(host).append("\r\n");//�����IP
        head.append("Content-Length: ").append(String.valueOf(body.toString().getBytes().length)).append("\r\n");
        head.append("Content-Type: multipart/form-data; boundary=").append(BOUNDARY).append("\r\n");
        head.append("\r\n");

        /*******************************�ϲ�****************************************/
        String rst = head.append(body).toString();
        System.out.println(rst);
        return rst;
    }


}
