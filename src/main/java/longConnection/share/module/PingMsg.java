package longConnection.share.module;

/**
 * ����������Ϣ����
 */
public class PingMsg extends BaseMsg {
    public PingMsg() {
        super();
        setType(MsgType.PING);
    }
}
