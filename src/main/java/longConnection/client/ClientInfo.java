package longConnection.client;

import java.util.Date;

public class ClientInfo {
    private String clientId;
    private Date createDate;

    public ClientInfo(String clientId, Date createDate) {
        this.clientId = clientId;
        this.createDate = createDate;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
