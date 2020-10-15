package simple;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.atomic.AtomicInteger;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private AtomicInteger channelCount = new AtomicInteger(0); //ͨ������

    /**
     * ������
     * @param ctx
     * @param msg
     * @throws Exception
     */
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("read channel=" + ctx.channel() + ", total channel=" + channelCount);
        try {

            byte[] bytes = (byte[])msg;
            System.out.println(new String(bytes));

            String returnMsg = "hi , I am is server...";
            Channel channel = ctx.channel();
            channel.writeAndFlush(returnMsg.getBytes());
        } finally {
            // �����յ�������
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * �������ĳ�ʱʱ�ᴥ��
     * @param ctx
     * @param evt
     * @throws Exception
     */
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                System.out.println("trigger channel =" + ctx.channel());
                ctx.close();  //�����ʱ���ر����ͨ��
            }
        } else if (evt instanceof SslHandshakeCompletionEvent) {
            System.out.println("ssl handshake done");
            //super.userEventTriggered(ctx,evt);
        }
    }

    /**
     * ��ͨ���ʱ
     * @param ctx
     * @throws Exception
     */
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channelCount.incrementAndGet();//������ͨ�����ӽ���ʱ����ͨ����+1
        System.out.println("active channel=" + ctx.channel() + ", total channel=" + channelCount + ", id=" + ctx.channel().id().asShortText());

    }

    /**
     * ��ͨ�����ʱ
     * @param ctx
     * @throws Exception
     */
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //��ͨ���ر�ʱ����ͨ����-1
        ctx.close();
        channelCount.decrementAndGet();
        System.out.println("inactive channel,channel=" + ctx.channel() +", id=" + ctx.channel().id().asShortText());
    }

    /**
     * �쳣��ȡ
     * @param ctx
     * @param cause
     * @throws Exception
     */
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("exception channel=" + ctx.channel() + " cause=" + cause); //�������Ծ�رմ�ͨ��
        ctx.close();
    }

}
