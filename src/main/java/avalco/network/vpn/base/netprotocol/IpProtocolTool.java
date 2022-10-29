package avalco.network.vpn.base.netprotocol;

import avalco.network.vpn.base.exception.InternetAddressException;

import java.util.Arrays;

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
    public static byte[] makeIcmp(String dst,String src){
        byte[]bytes=new byte[28];
        Arrays.fill(bytes, (byte) 0);
        bytes[0]=0x45;
        bytes[2]=(20&0xff00)>>>8;
        bytes[3]=(28&0xff);
        bytes[8]= (byte) (255);
        byte[]srcip;
        byte[]dstip;
        try {
            srcip=encodeIp(src);
            dstip=encodeIp(dst);
        } catch (InternetAddressException e) {
            throw new RuntimeException(e);
        }
        bytes[12]=srcip[0];
        bytes[13]=srcip[1];
        bytes[14]=srcip[2];
        bytes[15]=srcip[3];
        bytes[16]=dstip[0];
        bytes[17]=dstip[1];
        bytes[18]=dstip[2];
        bytes[19]=dstip[3];
        bytes[9]=1;
        int cm=IPPacket.IPChecksum(bytes,0,20);
        bytes[10]= (byte) ((cm&0xff00)>>>8);
        bytes[11]= (byte) (cm&0x00ff);
        cm=IPPacket.IPChecksum(bytes,20,8);
        bytes[22]= (byte) ((cm&0xff00)>>>8);
        bytes[23]= (byte) (cm&0x00ff);
        return bytes;
    }
    public static boolean union(int mask,int src,int dst){
        mask=0xFFFFFFFF<<(32-mask);
        return (src&mask)==(dst&mask);
    }
}
