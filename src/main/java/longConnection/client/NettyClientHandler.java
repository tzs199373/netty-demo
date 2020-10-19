package longConnection.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import longConnection.share.module.*;

public class NettyClientHandler extends SimpleChannelInboundHandler<BaseMsg> {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case WRITER_IDLE:
                    PingMsg pingMsg=new PingMsg();
                    pingMsg.setClientId("clientId001");
                    ctx.writeAndFlush(pingMsg);
                    System.out.println("send ping to server----------");
                    break;
                case READER_IDLE:
                    break;
                case ALL_IDLE:
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BaseMsg baseMsg) throws Exception {
        MsgType msgType=baseMsg.getType();
        switch (msgType){
            case LOGIN:{
                //向服务器发起登录
                LoginMsg loginMsg=new LoginMsg();
                loginMsg.setClientId("clientId001");
                loginMsg.setPassword("yao");
                loginMsg.setUserName("robin");
                channelHandlerContext.writeAndFlush(loginMsg);
            }break;
            case PING:{
                System.out.println("receive ping from server----------");
            }break;
            case ASK:{
                ReplyClientBody replyClientBody=new ReplyClientBody("client info **** !!!");
                ReplyMsg replyMsg=new ReplyMsg();
                replyMsg.setClientId("clientId001");
                replyMsg.setBody(replyClientBody);
                channelHandlerContext.writeAndFlush(replyMsg);
            }break;
            case REPLY:{
                ReplyMsg replyMsg=(ReplyMsg)baseMsg;
                ReplyServerBody replyServerBody=(ReplyServerBody)replyMsg.getBody();
                System.out.println("receive client msg: "+replyServerBody.getServerInfo());
            }
            default:break;
        }
        ReferenceCountUtil.release(msgType);
    }
}
