package avalco.network.vpn.base.netprotocol;

import avalco.network.vpn.base.exception.IPPacketException;

public class IPv6Packet extends IPPacket{
    //固定头部
        //0-3 IP协议版本: IPv4 值为0100，IPv6 值为0110
        //4-11 通信分类:
        //12-31 流标签
        //32-47 有效载荷长度
        //48-55 下一个头部
        //56-63 跳数限制
        //64-191 源地址
        //192-319 目的地址
    //附加头部 ......
    //数据
    @Override
    protected IPPacket handle(byte[] bytes) throws IPPacketException {
        if (bytes.length<40){
            throw new IPPacketException("ipv6 min length is 40 but now is"+bytes.length);
        }
        version=6;
        length=((bytes[4]&0xff)<<8)+(bytes[5]&0xff)+40;
        headerLength=40;
        src=format(bytes,8);
        dst=format(bytes,24);
        return this;
    }
    private String format(byte[]bytes,int offset){
        StringBuilder stringBuilder=new StringBuilder();
        for (int i=offset;i<offset+16;i=i+2){
            String s=Integer.toHexString(bytes[i]&0xff);
            s=s.length()<2?"0"+s:s;
            stringBuilder.append(s);
            s=Integer.toHexString(bytes[i+1]&0xff);
            s=s.length()<2?"0"+s:s;
            stringBuilder.append(s);
            stringBuilder.append(":");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }
}
