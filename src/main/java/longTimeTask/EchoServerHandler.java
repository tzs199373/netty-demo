package longTimeTask;

import io.netty.channel.*;

public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        //ģ���ʱ����
        try {
            Thread.sleep(5000);
            System.out.printf("%s execute time 5s \n", Thread.currentThread().getName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        ctx.writeAndFlush(msg );
    }
}


