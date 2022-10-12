package avalco.tools.logs;

import avalco.network.vpn.base.interfaces.ResourceRecovery;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;


public class Logger implements ResourceRecovery {
    OutputStream outputStream;
    LinkedBlockingDeque<LogEvent> linkedBlockingDeque;
    CacheList eventCachePool;
    boolean running;
    CountDownLatch countDownLatch;
    private LogPrintStream logPrintStream;
    private static final int cachePoolCapacity=100;
    public Logger(OutputStream outputStream) {
        this.outputStream = outputStream;
        eventCachePool= new CacheList(cachePoolCapacity);
        running=true;
        countDownLatch=new CountDownLatch(1);
        logPrintStream=new LogPrintStream(outputStream);
        new OutThread().start();
    }

    public void append(LogLevel level,String tag,String msg,Throwable throwable){
        LogEvent logEvent=eventCachePool.get();
        logEvent.time=System.currentTimeMillis();
        logEvent.level=level;
        logEvent.tag=tag;
        logEvent.msg=msg;
        logEvent.threadName=Thread.currentThread().getName();
        logEvent.threadID=Thread.currentThread().getId();
        String s=ManagementFactory.getRuntimeMXBean().getName();
        logEvent.processID= s.substring(0,s.indexOf("@"));
        logEvent.throwable=throwable;
        linkedBlockingDeque.offer(logEvent);
    }

    @Override
    public void recoverResource() {
        running=false;
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    class OutThread extends Thread{
        @Override
        public void run() {
            try {
                while (running||!linkedBlockingDeque.isEmpty()){
                    LogEvent logEvent=linkedBlockingDeque.take();
                    if (logEvent.msg!=null){
                        outputStream.write(logEvent.toString().getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                    }
                    if (logEvent.throwable!=null){
                        logPrintStream.setPrefix(logEvent.getPrefix());
                        logEvent.throwable.printStackTrace(logPrintStream);
                    }
                }
                outputStream.close();
                countDownLatch.countDown();
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class CacheNode{
        LogEvent logEvent;
        CacheNode next;
    }
    static class CacheList{
        public CacheList(int cachePoolCapacity) {
            CacheNode cacheNode= new CacheNode();
            first=cacheNode;
            last=cacheNode;
            for (int i=0;i<cachePoolCapacity-1;i++){
                add(new LogEvent());
            }
        }

        public synchronized void add(LogEvent logEvent) {
            CacheNode cacheNode= new CacheNode();
            cacheNode.logEvent=logEvent;
            last.next=cacheNode;
        }
        public LogEvent get(){
           LogEvent logEvent=first.logEvent;
           first=first.next;
           return logEvent;
        }
        CacheNode first;
        CacheNode last;
    }
}
