package myMsgProcotol;

public class MyHead {

    //���ݳ���
    private int length;

    //���ݰ汾
    private int version;


    public MyHead(int length, int version) {
        this.length = length;
        this.version = version;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }


}
