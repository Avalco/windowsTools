package avalco.network.vpn.base;

public class VirtualInternetFace {
    static {

        System.load("E:\\xzf\\source\\vpn\\virtualInternetFace\\x64\\Debug\\virtualInternetFace.dll");
        System.load("E:\\xzf\\source\\vpn\\virtualInternetFace\\x64\\Debug\\wintun.dll");
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
