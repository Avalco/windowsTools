package avalco.network.vpn.base;

import java.io.File;


public class VirtualInternetFace {
    static {
        File file=new File("");
        //logUtils.d(file.getAbsolutePath());
        if (System.getProperty("os.name").toLowerCase().contains("windows")){
            System.load(file.getAbsolutePath()+File.separator+"wintun.dll");
            System.load(file.getAbsolutePath()+File.separator+"virtualInternetFace.dll");
        }else {
            System.load(file.getAbsolutePath()+File.separator+"virtualInternetFace.so");
        }
        }
    public native void createIFace(String name);
    public native void sendPackets(byte[] bytes,int length);
    public native void setIFaceConf(byte[]ip,int mask);
    public native void addReceiveListener(ReceiveListener receiveListener);
    public native void close();
    public native String test();
    public interface ReceiveListener{
        void receivePacket(byte[]bytes);
    }

}
