package simple;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class ClientHandler  extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("active channel:" + ctx.channel());
        // Ҫ���͵���Ϣ
        String data = "I am client ...";
        // ���Ҫ������Ϣ���ֽ�����
        byte[] content = data.getBytes();

        Channel channel = ctx.channel();
        channel.writeAndFlush(content);
    }

    // ֻ�Ƕ����ݣ�û��д���ݵĻ�
    // ��Ҫ�Լ��ֶ����ͷŵ���Ϣ
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        System.out.println("read channel:" + ctx.channel());
        try {
            // ���ڻ�ȡ�ͻ��˷�����������Ϣ
            System.out.println("Client receive message:" + new String((byte[])msg));
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.close();
    }
}
