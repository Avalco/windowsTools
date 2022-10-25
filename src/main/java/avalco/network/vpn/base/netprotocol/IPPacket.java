package avalco.network.vpn.base.netprotocol;

import avalco.network.vpn.base.exception.IPPacketException;

public abstract class IPPacket {

    protected String src;
    protected String dst;
    protected int length;
    protected int headerLength;
    protected int version;
    public static IPPacket handlePacket(byte[]bytes) throws IPPacketException {
        if (bytes==null||bytes.length==0){
            throw new IPPacketException("ip packet cant not be null");
        }
        int version=((bytes[0]&0xf0)>>4);
        if (version!=4&&version!=6){
            throw new IPPacketException("ip version error"+bytes.length);
        }
        IPPacket ipPacket;
        if (version==4){
            return new IPV4Packet().handle(bytes);
        }
        return new IPv6Packet().handle(bytes);
    }
   protected abstract IPPacket handle(byte[] bytes) throws IPPacketException;
    @Override
    public String toString() {
        return "version=ipv"+version+",headLength="+headerLength+",packet length="+length+",from "+src+",to "+dst;
    }

    public String getSrc() {
        return src;
    }

    public String getDst() {
        return dst;
    }

    public int getLength() {
        return length;
    }

    public int getHeaderLength() {
        return headerLength;
    }

    public int getVersion() {
        return version;
    }

public static int IPChecksum( byte[]bytes,int offset,int l)
{
        int Sum = 0;
        int i;
        for (i=offset; i < l&&i+1<l; i+= 2){
            Sum += ((bytes[i]&0xff)<<8)+(bytes[i+1]&0xff);
        }
        if (i<l){
            Sum +=(bytes[i]&0xff);
        }
        while (Sum>>16!=0){
            Sum = (Sum & 0xffff) + (Sum >> 16);
        }
        Sum = (Sum >> 16) + (Sum & 0xffff);
        Sum += (Sum >> 16);
        return (~Sum);
 }
}