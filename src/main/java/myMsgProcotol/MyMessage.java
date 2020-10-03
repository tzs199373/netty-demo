package myMsgProcotol;

public class MyMessage {
    //消息head
    private MyHead head;
    //消息body
    private String content;

    public MyMessage(MyHead head, String content) {
        this.head = head;
        this.content = content;
    }

    public MyHead getHead() {
        return head;
    }

    public void setHead(MyHead head) {
        this.head = head;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return String.format("[length=%d,version=%d,content=%s]",head.getLength(),head.getVersion(),content);
    }
}
