package avalco.network.vpn.base;

import avalco.network.vpn.base.conf.ApplicationConf;

import avalco.tools.files.FileUtil;
import avalco.tools.logs.LogPrintStream;
import avalco.tools.logs.LogUtil;


import java.io.*;

import java.nio.file.Files;

import java.nio.file.StandardOpenOption;

public abstract class ApplicationContext {
    protected File errorLog;
    protected File debugLog;
    protected ApplicationConf applicationConf;
    protected LogUtil logUtil;
    private static final String APP_CONF="application";
    private static final String LOG_CONF="log";

    private final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler;

    protected ApplicationContext(){
        Thread shutdownHook = new Thread(new Runnable() {
            @Override
            public void run() {
                onApplicationExit();
            }
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        ConfParse<ApplicationConf> confConfPars=new ConfParse<>(ApplicationConf.class);
        File conf=getConfigFile(APP_CONF);
        try {
            applicationConf=confConfPars.parse(conf);
        } catch (Exception e) {
            LogPrintStream logPrintStream = new LogPrintStream(System.out);
            e.printStackTrace(logPrintStream);
        }
        errorLog=new File(applicationConf.getErrorLogPath());
        debugLog=new File(applicationConf.getLogPath());
        defaultUncaughtExceptionHandler=Thread.getDefaultUncaughtExceptionHandler();
        Thread.UncaughtExceptionHandler myUncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                try {
                    File file=FileUtil.createFileByCalender(errorLog,applicationConf.getType()+".error");
                    LogPrintStream logPrintStream = new LogPrintStream(Files.newOutputStream(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.APPEND));
                    e.printStackTrace(logPrintStream);
                    System.exit(-100);
                } catch (Exception error) {
                    defaultUncaughtExceptionHandler.uncaughtException(t, e);
                }
            }
        };
        Thread.setDefaultUncaughtExceptionHandler(myUncaughtExceptionHandler);
        logUtil=LogUtil.newInstance(debugLog.getAbsolutePath(),getConfigFile(LOG_CONF).getAbsolutePath());
        onCreate();
    }

    protected abstract void onCreate();

    public abstract void start();
    public abstract void shutdown();
    protected abstract void onApplicationExit();
    protected File getConfigFile(String name){
        File conf=FileUtil.getFile("config"+File.separator+name);
        if (!conf.exists()){
            try {
                conf=FileUtil.copyFileFromResources("config/application",name);
            } catch (IOException e) {
                LogPrintStream logPrintStream = new LogPrintStream(System.out);
                e.printStackTrace(logPrintStream);
                conf=null;
            }
        }
        return conf;
    }
}
