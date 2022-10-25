package avalco.tools.logs;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;

public class LogPrintStream extends PrintStream{
        String prefix;
        public LogPrintStream(OutputStream out) {
            super(out);
            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            String id = runtime.getName();
            id=id.substring(0,id.indexOf("@"));
            prefix=dateFormat.format(System.currentTimeMillis())+" "+id+"-"+Thread.currentThread().getId()+"@"+Thread.currentThread().getName()+"  ";
        }

        @Override
        public void print(String s) {
            s=s==null?prefix+"null":prefix+s;
            super.print(s);
        }
        public LogPrintStream setPrefix(String prefix){
            this.prefix=prefix;
            return this;
        }
}
