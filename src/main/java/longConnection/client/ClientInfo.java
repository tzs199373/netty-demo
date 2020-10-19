package longConnection.client;

import java.util.Date;

public class ClientInfo {
    private String clientId;
    private  boolean isOpenHeartBeat;
    private Date createDate;

    public ClientInfo(String clientId, boolean isOpenHeartBeat, Date createDate) {
        this.clientId = clientId;
        this.isOpenHeartBeat = isOpenHeartBeat;
        this.createDate = createDate;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isOpenHeartBeat() {
        return isOpenHeartBeat;
    }

    public void setOpenHeartBeat(boolean openHeartBeat) {
        isOpenHeartBeat = openHeartBeat;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

}
