package avalco.network.vpn.server;

public class ServerConf {
    String privateKeyPath;
    String mask;
    String gateway;
    String ip;
    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public String getMask() {
        return mask;
    }

    public String getGateway() {
        return gateway;
    }

    public String getIp() {
        return ip;
    }
}
