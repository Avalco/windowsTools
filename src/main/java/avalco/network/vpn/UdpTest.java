package avalco.network.vpn;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.nio.charset.StandardCharsets;

public class UdpTest {
    public static void main(String []args){
        try {
            DatagramSocket datagramSocket=new DatagramSocket(12354);
            byte[] bys=new byte[1024];
            while (true){
                DatagramPacket datagramPacket=new DatagramPacket(bys, bys.length);
                datagramSocket.receive(datagramPacket);
                System.out.println("输入数据为："+new String(datagramPacket.getData(),0,datagramPacket.getLength()));
                String replyMsg="received:"+datagramPacket.getLength();
                DatagramPacket reply=new DatagramPacket(replyMsg.getBytes(StandardCharsets.UTF_8),replyMsg.getBytes(StandardCharsets.UTF_8).length,datagramPacket.getSocketAddress());
                datagramSocket.send(reply);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
