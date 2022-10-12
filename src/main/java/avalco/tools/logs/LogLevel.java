package avalco.tools.logs;

public enum LogLevel {
    INFO("INFO"),DEBUG("DEBUG"),ERROR("ERROR");
    String name;
    LogLevel(String s){
        this.name=s;
    }

}
