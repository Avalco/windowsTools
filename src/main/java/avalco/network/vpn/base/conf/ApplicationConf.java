package avalco.network.vpn.base.conf;

public class ApplicationConf {
    String logPath;
    String errorLogPath;
    String type;
    String serverHost;
    int serverPort;
    String userName;
    String password;
    String serverKeyPath;

    public String getServerKeyPath() {
        return serverKeyPath;
    }

    public String getType() {
        return type;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getErrorLogPath() {
        return errorLogPath;
    }

    public void setErrorLogPath(String errorLogPath) {
        this.errorLogPath = errorLogPath;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getServerHost() {
        return serverHost;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
