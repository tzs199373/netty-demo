# simple

简单的入门示例程序，客户端、服务端都有

# httpServer

## 说明

做一个简单的http服务器，只有服务端，客户端可以使用浏览器或postman

## 重点

1、一次http请求并不是通过一次对话完成的，中间可能有很次的连接。netty的每一次对话都会建立一个channel。如何在一个Channel里面处理一次完整的Http请求？需要用到FullHttpRequest，netty处理channel的时候，只处理消息是FullHttpRequest的Channel，这样我们就能在一个ChannelHandler中处理一个完整的Http请求了。要得到FullHttpRequest，需要aggregator，消息聚合器。HttpObjectAggregator(512 * 1024)的参数含义是消息合并的数据大小，如此代表聚合的消息内容长度不超过512kb。

2、响应中的header描述length很重要，如果没有，你会发现用postman发出请求之后就一直在刷新，因为http请求方不知道返回的数据到底有多长。channel读取完成之后需要输出缓冲流。如果没有，你会发现postman同样会一直在刷新。

# longConnection 

自定义消息协议通讯及心跳检测例子

LongClient类中main方法会while循环发业务消息，如果屏蔽while循环，client一段时间没有读写事件，就会触发userEventTriggered事件，前提是有IdleStateHandler。该方法中发送ping消息（心跳）。

# file

文件传输

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

netty处理耗时任务，使用Netty提供的的EventExecutorGroup

# concurrencyProblem

模拟并发及延迟问题，服务端对某个请求响应慢