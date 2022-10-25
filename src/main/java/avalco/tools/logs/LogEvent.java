package avalco.tools.logs;

import java.text.SimpleDateFormat;

public class LogEvent {
    long time;
    String msg;
    String tag;
    String threadName;
    long threadID;
    String processID;
    LogLevel level;
    Throwable throwable;
    private static final SimpleDateFormat format=
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    @Override
    public String toString() {
        return getPrefix()+level.name+"-"+tag+" "+msg+(throwable==null?"":" cause by:")+"\n";
    }
    public String getPrefix(){
        return format.format(time)+" "+processID+"-"+threadID+"@"+threadName+"  ";
    }
}
