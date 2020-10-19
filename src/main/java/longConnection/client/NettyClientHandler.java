package longConnection.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.ReferenceCountUtil;
import longConnection.share.module.*;

import static longConnection.client.Constants.CLIENT_INFO_ATTRIBUTE_KEY;

public class NettyClientHandler extends SimpleChannelInboundHandler<BaseMsg> {
    private LongClient client;
    public NettyClientHandler(LongClient client) {
        this.client = client;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Attribute<ClientInfo> attr = ctx.channel().attr(CLIENT_INFO_ATTRIBUTE_KEY);
        System.out.println("client["+attr.get().getClientId()+"] 准备重连");
        client.doConnect(attr.get().getClientId(),attr.get().isOpenHeartBeat());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case WRITER_IDLE:
                    Attribute<ClientInfo> attr = ctx.channel().attr(CLIENT_INFO_ATTRIBUTE_KEY);
                    if(attr.get().isOpenHeartBeat()){
                        PingMsg pingMsg=new PingMsg();
                        pingMsg.setClientId(attr.get().getClientId());
                        ctx.writeAndFlush(pingMsg);
                        System.out.println("send ping to server----------");
                    }else{
                        System.out.println("client["+attr.get().getClientId()+"] beatHeart is not open");
                        ctx.fireUserEventTriggered(evt);
                    }
                    break;
                case READER_IDLE:
                    ctx.fireUserEventTriggered(evt);
                    break;
                case ALL_IDLE:
                    ctx.fireUserEventTriggered(evt);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BaseMsg baseMsg) throws Exception {
        MsgType msgType=baseMsg.getType();
        switch (msgType){
            case LOGIN:{
                Attribute<ClientInfo> attr = ctx.channel().attr(CLIENT_INFO_ATTRIBUTE_KEY);
                //向服务器发起登录
                LoginMsg loginMsg=new LoginMsg();
                loginMsg.setClientId(attr.get().getClientId());
                loginMsg.setPassword("yao");
                loginMsg.setUserName("robin");
                ctx.writeAndFlush(loginMsg);
            }break;
            case PING:{
                System.out.println("receive ping from server----------");
            }break;
            case ASK:{
                Attribute<ClientInfo> attr = ctx.channel().attr(CLIENT_INFO_ATTRIBUTE_KEY);
                
                ReplyClientBody replyClientBody=new ReplyClientBody("client info **** !!!");
                ReplyMsg replyMsg=new ReplyMsg();
                replyMsg.setClientId(attr.get().getClientId());
                replyMsg.setBody(replyClientBody);
                ctx.writeAndFlush(replyMsg);
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
