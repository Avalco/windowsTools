package avalco.network.vpn.base.netprotocol;

import avalco.network.vpn.base.exception.IPPacketException;

public class IPV4Packet extends IPPacket{
    int srcIP;
    int dstIP;
    //头部
        //0-3 IP协议版本: IPv4 值为0100，IPv6 值为0110
        //4-7 首部的长度: 占4位，最大表示的十进制数为15，该字段的值*4才代表首部长度字节数。首部长度不是4的整数倍时,用最后的填充字段填充。
        //8-15 服务类型: 前3位为优先级(Precedence)，后4位标志位，最后1位保留未用。
        //优先级主要用于 QoS，表示从0（普通级别）到7(网络控制分组)的优先级。
        //标志位可分别表示D(Delay更低的时延)、T(Throughput 更高的吞吐量)、R(Reliability更高的可靠性)、C(Cost 更低费用的路由)。
        //16-31 总长: 首部和数据之和的长度，单位为字节，数据报的最大长度为65535
        //32-47 标识: 用于数据包在分段重组时标识其序列号。
        //48-50 标志: 三位从左到右分别是MF、DF、未用。MF=1 表示后面还有分段的数据包，MF=0 表示没有更多分片(即最后一个分片)。DF=1 表示路由器不能对该数据包分段，DF=0 表示数据包可以被分段。
        //51-63 片偏移: 用于标识该数据段在上层初始数据报文中的偏移量。如果某个包含分段的上层报文的IP数据包在传送时丢失，则整个一系列包含分段的上层数据包的IP包都会要求重传
        //64-71 生存时间: 生存时间常用的字段是TTL，表示数据报在网络中的寿命，由发出的源站点设置，TTL字段是以跳数限制的，每经过一个路由器，在转发之前就把跳数减为1，当TTL减为0时就会丢弃这个数据报，因为数据报在因特网中最大经过的路由器是255。
        //72-79 协议: 数据报携带的数据是使用何种协议
        //80-95 首部校验和:
        //96-127 源地址
        //128-159 目的地址
        //[1-40BYTE] 可选字段
        //[填充段]：上述所有长度不为4BYTE整数倍时，用全0填充到4BYTE整数倍
    //数据

    @Override
    protected IPPacket handle(byte[] bytes) throws IPPacketException {
        if (bytes.length<20){
            throw new IPPacketException("ipv4 min length is 20 bytes but now is"+bytes.length);
        }
        headerLength=(bytes[0]&0x0f)*4;
        version=4;
        length=((bytes[2]&0xff)<<8)+(bytes[3]&0xff);
        int p1,p2,p3,p4;
        p1=bytes[12]&0xff;
        p2=bytes[13]&0xff;
        p3=bytes[14]&0xff;
        p4=bytes[15]&0xff;
        srcIP=((bytes[12]&0xff)<<24)+((bytes[13]&0xff)<<16)+((bytes[14]&0xff)<<8)+((bytes[15]&0xff));
        src=p1+"."+p2+"."+p3+"."+p4;
        p1=bytes[16]&0xff;
        p2=bytes[17]&0xff;
        p3=bytes[18]&0xff;
        p4=bytes[19]&0xff;
        dstIP=((bytes[16]&0xff)<<24)+((bytes[17]&0xff)<<16)+((bytes[18]&0xff)<<8)+((bytes[19]&0xff));
        dst=p1+"."+p2+"."+p3+"."+p4;
        return this;
    }

    public int getSrcIP() {
        return srcIP;
    }

    public int getDstIP() {
        return dstIP;
    }
}
