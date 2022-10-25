package avalco.tools.files;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;

public class FileUtil {
    public static String resourceDirectory="resources";
    public static String libDirectory="lib";
    private static final SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
    public static InputStream getResourceFileStream(String fileName){
        return getStreamFormJar(resourceDirectory+"/"+fileName);
    }
    public static InputStream getLibFileStream(String fileName){
        return getStreamFormJar(libDirectory+File.separator+fileName);
    }
    public static InputStream getStreamFormJar(String path){
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null)
            loader = ClassLoader.getSystemClassLoader();
        return loader.getResourceAsStream(path);
    }
    public static File createDirectory(String name){
        File file =new File(name);
        if (!file.exists()&&file.mkdirs()){
            return file;
        }
        return null;
    }
    public static File getFile(String path){
        return new File(path);
    }
    public static File copyFileFromResources(String des,String sour) throws IOException {
        File dFile=new File(des);
        if (!dFile.getParentFile().exists()){
            dFile.getParentFile().mkdirs();
        }
        InputStream inputStream=getResourceFileStream(sour);
        OutputStream outputStream= Files.newOutputStream(dFile.toPath());
        byte[]bytes=new byte[1024];
        int l=0;
        while ((l=inputStream.read(bytes))!=-1){
            outputStream.write(bytes,0,l);
            outputStream.flush();
        }
        outputStream.close();
        inputStream.close();
        return dFile;
    }
    public static File createFileByCalender(File directory,String name){
        if (!directory.exists()){
            directory.mkdirs();
        }
       String date= simpleDateFormat.format(System.currentTimeMillis());
       int l=name.lastIndexOf(".");
       return new File(directory,name.substring(0,l)+"-"+date+name.substring(l));
    }
}
