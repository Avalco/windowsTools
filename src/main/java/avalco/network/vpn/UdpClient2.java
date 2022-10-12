package avalco.network.vpn;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;


public class UdpClient2 {
    public static void main(String []args){
        try {
            DatagramSocket datagramSocket=new DatagramSocket();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int i=0;
                    String s="client2 send message "+i;
                    while (i++<100){
                        try {
                            Thread.sleep(100);
                            DatagramPacket datagramPacket=new DatagramPacket(s.getBytes(StandardCharsets.UTF_8)
                                    , s.length(), InetAddress.getByName("127.0.0.1"),12354);
                            datagramSocket.send(datagramPacket);
                        } catch (IOException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    datagramSocket.close();
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] bys=new byte[1024];
                        while (true){
                            DatagramPacket datagramPacket=new DatagramPacket(bys, bys.length);
                            datagramSocket.receive(datagramPacket);
                            System.out.println("收到回复："+new String(datagramPacket.getData(),0,datagramPacket.getLength()));
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
