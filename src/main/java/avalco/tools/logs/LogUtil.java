package avalco.tools.logs;

import avalco.network.vpn.base.ConfParse;
import avalco.tools.files.FileUtil;

import java.io.*;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LogUtil {
    private final File logDirector;
    private final LogConf logConf;

    private Logger logger;
    private final Timer timer;
    private static final long DAY=24*60*60*1000;
    private static final String TAG="LogUtils";
    private LogUtil(String logPath, String logConfig) {
        logDirector = new File(logPath);
        try {
            logConf = new ConfParse<LogConf>(LogConf.class).parse(new File(logConfig));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
         timer=new Timer();
        int hour=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute=Calendar.getInstance().get(Calendar.MINUTE);
        int second=Calendar.getInstance().get(Calendar.SECOND);
        int millisecond=Calendar.getInstance().get(Calendar.MILLISECOND);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Logger nLogger= null;
                try {
                    nLogger = createLogger();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Logger old=logger;
                logger=nLogger;
                old.recoverResource();
                cleanOutLog();
            }
        },DAY-(((hour*60+minute)*60+second)*1000+millisecond), DAY);
        try {
            logger=createLogger();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                cleanOutLog();
            }
        }).start();
    }

    private void cleanOutLog() {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        Date current=new Date();
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(current);
        calendar.add(Calendar.DAY_OF_YEAR,-logConf.duration);
        File []files=logDirector.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String regx="[0-9]{4}-[0-9]2-[0-9]2";
                Matcher matcher= Pattern.compile(regx).matcher(name);
               if (matcher.find()){
                   String s=matcher.group();
                   try {
                       Date d=simpleDateFormat.parse(s);
                       if (d.before(calendar.getTime())){
                           return true;
                       }
                   } catch (ParseException e) {
                       e(TAG,"parse fileName "+name+"failed:"+e.getMessage(),e);
                   }
               }
               return false;
            }
        });
        if (files!=null){
            for (File file:files){
               boolean b= file.delete();
            }
        }
    }

    private Logger createLogger() throws IOException {
        File file= FileUtil.createFileByCalender(logDirector, logConf.logName);
        return new Logger(Files.newOutputStream(file.toPath()));
    }

    public static LogUtil newInstance(String logPath, String logConfig) {
        return new LogUtil(logPath, logConfig);
    }

    public void terminal() {
        logger.recoverResource();
        timer.cancel();
    }

    public void i(String tag, String msg) {
        logger.append(LogLevel.INFO, tag, msg, null);
    }

    public void d(String tag, String msg) {
        logger.append(LogLevel.DEBUG, tag, msg, null);
    }

    public void e(String tag, String msg) {
        logger.append(LogLevel.ERROR, tag, msg, null);
    }

    public void e(String tag, String msg, Throwable throwable) {
        logger.append(LogLevel.ERROR, tag, msg, throwable);
    }

}
