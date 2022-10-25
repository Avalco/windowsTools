package avalco.network.vpn.client;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PacketSender implements Runnable{
private final InetAddress inetAddress;
private final int port;
private final Cipher cipher;
private final DatagramSocket datagramSocket;
private final byte[] header;
private final BlockingQueue<DatagramPacket> queue=new LinkedBlockingQueue<>();
public PacketSender(String str,int port,DatagramSocket datagramSocket,String token,byte[]key,byte[]iv) throws UnknownHostException {
    inetAddress=InetAddress.getByName(str);
    this.port=port;
    this.datagramSocket=datagramSocket;
    //todo cipher udp
    try {
        cipher=Cipher.getInstance("AES/CTR/NoPadding");
        SecretKeySpec keySpec=new SecretKeySpec(key,"AES");
        IvParameterSpec ivParameterSpec=new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE,keySpec,ivParameterSpec);
        header=token.getBytes(StandardCharsets.UTF_8);
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
             InvalidAlgorithmParameterException e) {
        throw new RuntimeException(e);
    }
}
    @Override
    public void run() {
        try {
            while (!Thread.interrupted()){
                DatagramPacket datagramPacket=queue.take();
                datagramSocket.send(datagramPacket);
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void send(byte[]bytes){
        try {
            bytes= cipher.doFinal(bytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        byte[] buffer=Arrays.copyOf(header,header.length+bytes.length);
        System.arraycopy(bytes,0,buffer,header.length,bytes.length);
        DatagramPacket datagramPacket=new DatagramPacket(buffer,buffer.length,inetAddress,port);
        queue.offer(datagramPacket);
    }
}
