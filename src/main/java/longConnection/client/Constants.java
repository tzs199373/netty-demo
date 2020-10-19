package longConnection.client;

import io.netty.util.AttributeKey;

public class Constants {
    public static final AttributeKey<ClientInfo> CLIENT_INFO_ATTRIBUTE_KEY = AttributeKey.valueOf("client.info");
}
