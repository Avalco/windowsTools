package avalco.network.vpn.base.conf;

public class ApplicationConf {
    String logPath;
    String errorLogPath;
    String type;
    String serverHost;
    int serverPort;
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
}
