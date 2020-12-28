# simple

简单的入门示例程序，客户端、服务端都有

# http

## 说明

做一个简单的http服务器，只有服务端，客户端可以使用浏览器或postman

## 重点

1、一次http请求并不是通过一次对话完成的，中间可能有很次的连接。netty的每一次对话都会建立一个channel。如何在一个Channel里面处理一次完整的Http请求？需要用到FullHttpRequest，netty处理channel的时候，只处理消息是FullHttpRequest的Channel，这样我们就能在一个ChannelHandler中处理一个完整的Http请求了。要得到FullHttpRequest，需要aggregator，消息聚合器。HttpObjectAggregator(512 * 1024)的参数含义是消息合并的数据大小，如此代表聚合的消息内容长度不超过512kb。

2、响应中的header描述length很重要，如果没有，你会发现用postman发出请求之后就一直在刷新，因为http请求方不知道返回的数据到底有多长。channel读取完成之后需要输出缓冲流。如果没有，你会发现postman同样会一直在刷新。

# longConnection 

客户端发送心跳：

client一段时间没有读写事件，就会触发userEventTriggered事件，前提是有IdleStateHandler。该方法中发送ping消息（心跳）。本例中心跳是否开启通过ClientInfo.isOpenHeartBeat设置。

Channel绑定自定义属性：

使用AttributeKey与Attribute

服务端检测读空闲：

ReadTimeoutHandler如果超过设置的时间，主动断开，此时触发channelInactive事件，移除缓存连接

客户端断线重连：

服务端主动断开时，客户端也会触发channelInactive事件，再次执行doConnect方法，重新连接

# uploadChunkFile

文件分块传输

# uploadFile

文件传输，由于netty对文件传输都是封装的分块传输，即http chunk，笔者能力有限没找到netty有关multipart/form-data的协议，这里笔者纯手写一个multipart/form-data报文。

服务端用的是之前springboot接收文件上传的接口

```java
package com.web.controller;

import com.commonutils.util.json.JSONObject;
import com.commonutils.util.validate.FileTypeCensor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;

@Controller
@RequestMapping(value = "/file")
public class FileController {
    @ResponseBody
    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    
    public String uploadFile(@RequestParam(value = "uploadFile", required = false) MultipartFile[] multipartFiles,HttpServletRequest request){
        JSONObject result = new JSONObject();
        try {
            for (int i=0; i< multipartFiles.length; i++){
                // 输出源文件名称 就是指上传前的文件名称
                System.out.println("uploadFile:" + multipartFiles[i].getOriginalFilename());
                // 创建文件(MultipartFile转File)
                String saveRoot = "f:\\";
                File file = new File(saveRoot + multipartFiles[i].getOriginalFilename());
                InputStream in  = multipartFiles[i].getInputStream();
                OutputStream os = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                int n;
                while ((n = in.read(buffer,0,4096)) != -1){
                    os.write(buffer,0,n);
                }
                in.close();
                os.close();
            }
            //输出其他字段
            String value = request.getParameter("k1");
            System.out.println("value:"+value);
        } catch (Exception e) {
            result.put("msg", e.getMessage());
            result.put("flag", "fail");
        }
        result.put("flag", "success");
        result.put("msg", "success");

        return result.toString();
    }

   
}


```



# packetProblem

粘包拆包问题

## 问题展示

首先启动服务端，然后再启动客户端，通过控制台可以看到，发送较大字符串，服务接收的数据分成了2次,这就是我们要解决的问题。

## @Sharable

netty的ChannelHandler如果带有@Sharable表示多个channel可以共用一个，否则不行，很多解码器里因为存有Channel的状态变量，所以很多不带@Sharable

## LineBasedFrameDecoder

服务端，入站最前方加上

```java
ch.pipeline().addLast(new LineBasedFrameDecoder(10240));
```

客户端发送末尾加上换行符

```java
channel.writeAndFlush(msg+System.getProperty("line.separator"));
```

运行后，服务端只收到一次消息，客户端还是收到两次回复，因为客户端未做处理

## DelimiterBasedFrameDecoder

和LineBasedFrameDecoder一样，这里可以自定义分隔符

## FixedLengthFrameDecoder

提前知道客户端消息长度，服务端解析固定长度

```java
ch.pipeline().addLast(new FixedLengthFrameDecoder(1300));
```

客户端直接发送即可

## LengthFieldBasedFrameDecoder

数据包长度 = lengthFieldOffset + lengthFieldLength + lengthAdjustment+长度域的值 

ByteOrder：数据存储采用大端模式或小端模式，网络传输默认大端模式

lengthFieldOffset：长度字段偏移量

lengthFieldLength：长度字段所占字节数

lengthAdjustment：长度字段的补偿值

initialBytesToStrip ：从解码帧中第一次去除的字节数。
通俗地说netty拿到一个完整的数据包之后向业务解码器传递之前，应该跳过多少字节。
常用于业务解析器中不需要消息的头部（包含长度或其他信息）的场景

failFast：true: 读取到长度域超过maxFrameLength，就抛出一个 TooLongFrameException。
false: 只有真正读取完长度域的值表示的字节之后，才会抛出 TooLongFrameException。
默认情况下设置为true，建议不要修改，否则可能会造成内存溢出。

tip:关于LengthFieldBasedFrameDecoder用法在myMsgProcotol包中也有体现

# myMsgProcotol

自定义消息与编解码

# longTimeTask

netty处理耗时任务，使用Netty提供的的EventExecutorGroup，注意这里并不是开启异步处理慢业务，以提高响应速度

这里的目的只是尽量避免使用NioEventLoop处理慢业务，因为依旧是同步响应，所以还是慢

如果客户端不需要知道慢业务的处理的最终结果，应该在慢业务处开启多线程异步处理

# concurrencyProblem

模拟并发及延迟问题，服务端对某个请求响应慢，某个客户端响应慢并不会影响其他客户端

但是单个客户端，如果某次请求响应慢，会阻塞对该客户端后续请求的处理，这是NIO的线程模型决定的，传统的BIO则不会