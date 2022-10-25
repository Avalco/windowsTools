package avalco.network.vpn.base.netprotocol;

import avalco.network.vpn.base.exception.InternetAddressException;

public class IpProtocolTool {
    public static byte[] encodeIp(String ip) throws InternetAddressException {
        byte[]bytes=new byte[4];
        String []strings=ip.split("\\.");
        if (strings.length!=4){
            throw new InternetAddressException("cant encode ip:"+ip);
        }
        for (int i=0;i<4;i++){
            int n=Integer.parseInt(strings[i]);
            if (n<0||n>255){
                throw new InternetAddressException("cant encode ip:"+ip);
            }
            bytes[i]= (byte) (n&0xff);
        }
        return bytes;
    }
    public static int countMask(String mask) throws InternetAddressException {
        String []strings=mask.split("\\.");
        int num=0;
        if (strings.length!=4){
            throw new InternetAddressException("cant encode mask:"+mask);
        }
        for (int i=0;i<4;i++){
            int n=Integer.parseInt(strings[i]);
            if (n<0||n>255){
                throw new InternetAddressException("cant encode mask:"+mask);
            }
            num+=Integer.bitCount(n);
        }
        return num;
    }
}
