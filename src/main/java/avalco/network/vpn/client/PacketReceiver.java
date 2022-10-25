package avalco.network.vpn.client;



import avalco.network.vpn.base.exception.NoExitException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


public class PacketReceiver implements Runnable{
ReceiveListener receiveListener;
    private final String header;
    private final Cipher cipher;
byte[]buffer=new byte[65535];
    private DatagramSocket datagramSocket;
    public PacketReceiver(DatagramSocket datagramSocket,String token,byte[]key,byte[]iv, ReceiveListener receiveListener) {
        this.receiveListener = receiveListener;
        this.datagramSocket=datagramSocket;
        try {
            cipher=Cipher.getInstance("AES/CTR/NoPadding");
            SecretKeySpec keySpec=new SecretKeySpec(key,"AES");
            IvParameterSpec ivParameterSpec=new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE,keySpec,ivParameterSpec);
            header=token;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        DatagramPacket datagramPacket=new DatagramPacket(buffer,buffer.length);
        while (!Thread.interrupted()){
            try {
                datagramSocket.receive(datagramPacket);
                String token=new String(buffer,0,header.getBytes(StandardCharsets.UTF_8).length,StandardCharsets.UTF_8);
                if (token.equals(header)){
                    try {
                        byte[]bytes;
                        bytes = cipher.doFinal(datagramPacket.getData(),token.getBytes(StandardCharsets.UTF_8).length,datagramPacket.getLength());
                        receiveListener.receivePacket(bytes,bytes.length);
                    } catch (IllegalBlockSizeException | BadPaddingException e) {
                        throw new NoExitException(e);
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
