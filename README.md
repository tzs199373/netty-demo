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
