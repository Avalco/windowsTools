package avalco.network.vpn.security;

import avalco.network.vpn.security.exception.CipherHandShakeException;

import javax.crypto.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import java.security.Key;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;


public class CipherConnection {
    private final Key key;
    public CipherConnection(Key key){
        this.key=key;
    }

    public enum SocketType{
        CLIENT,SERVER
    }
    public Socket connect(Socket socket) throws CipherHandShakeException {
        SocketType socketType;
        if (key instanceof PublicKey){
            socketType=SocketType.CLIENT;
        }else if (key instanceof PrivateKey){
            socketType=SocketType.SERVER;
        }
        else {
            throw new CipherHandShakeException("unexpect key type");
        }
        switch (socketType){
            case CLIENT:
              return   handshakeClient(socket);
            case SERVER:
               return handshakeServer(socket);
        }
        return null;
    }

    private Socket handshakeServer(Socket socket) throws CipherHandShakeException {
        try {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            Cipher encode = Cipher.getInstance("RSA");
            encode.init(Cipher.ENCRYPT_MODE, key);
            Cipher decode = Cipher.getInstance("RSA");
            decode.init(Cipher.DECRYPT_MODE, key);
            int seq ;
            int ack ;
            String packet;
            byte[] outs ;
            byte[] in = new byte[1024];
            int l;
            socket.setSoTimeout(20000);
            l = inputStream.read(in);
            socket.setSoTimeout(0);
            String s = new String(decode.doFinal(in, 0, l), StandardCharsets.UTF_8);
            String[] strings=s.split(",");
            seq=Integer.parseInt(strings[0].split("=")[1]);
            ack=Integer.parseInt(strings[1].split("=")[1]);
            String reply;
            if (ack!=0) {
                throw new CipherHandShakeException("unexpect ack ,expect ack is "+0+" but receive is "+ack);
            }
            packet = getPacket(++seq, ++ack);
            outs = encode.doFinal(packet.getBytes(StandardCharsets.UTF_8));
            outputStream.write(outs);
            outputStream.flush();
            socket.setSoTimeout(20000);
            l = inputStream.read(in);
            socket.setSoTimeout(0);
            byte[]decodes=decode.doFinal(in, 0, l);
            s = new String(decodes, StandardCharsets.UTF_8);
            strings=s.split(",",3);
            reply=getPacket(++seq,++ack);
            String sub=s.substring(0,(strings[0].length()+strings[1].length()+1));
            if (!sub.equals(reply)){
                throw new CipherHandShakeException("unexpect reply ,expect reply is "+reply+" but receive is "+sub);
            }
            int offset=sub.getBytes(StandardCharsets.UTF_8).length+1;
            byte[]originKey=new byte[16];
            System.arraycopy(decodes,offset,originKey,0,16);
            byte[]iv=new byte[16];
            System.arraycopy(decodes,offset+16,iv,0,16);
            AesSocketWrapper aesSocketWrapper=new AesSocketWrapper(socket,originKey,iv);
            outputStream=aesSocketWrapper.getOutputStream();
            packet=getPacket(++seq,++ack);
            outputStream.write(packet.getBytes(StandardCharsets.UTF_8));
            return aesSocketWrapper;
        }
        catch (Exception e){
            throw new CipherHandShakeException(e);
        }
    }

    private Socket handshakeClient(Socket socket) throws CipherHandShakeException {
        try {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            Cipher encode = Cipher.getInstance("RSA");
            encode.init(Cipher.ENCRYPT_MODE, key);
            Cipher decode = Cipher.getInstance("RSA");
            decode.init(Cipher.DECRYPT_MODE, key);
            int seq = (int) (Math.random() * 513);
            int ack = 0;
            String packet = getPacket(seq++, ack++);
            byte[] outs = encode.doFinal(packet.getBytes(StandardCharsets.UTF_8));
            outputStream.write(outs);
            outputStream.flush();
            byte[] in = new byte[1024];
            int l;
            socket.setSoTimeout(20000);
            l = inputStream.read(in);
            socket.setSoTimeout(0);
            String s = new String(decode.doFinal(in, 0, l), StandardCharsets.UTF_8);
            String reply = getPacket(seq++, ack++);
            if (!s.equals(reply)) {
                throw new CipherHandShakeException("unexpect reply ,expect reply is "+reply+" but receive is "+s);
            }
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128,new SecureRandom());
            SecretKey originalKey = keyGenerator.generateKey();
            SecretKey iv=keyGenerator.generateKey();
            packet=getPacket(seq++,ack++);
            packet=packet+",";
            int pLength=packet.getBytes(StandardCharsets.UTF_8).length;
            outs=new byte[pLength+32];
            System.arraycopy(packet.getBytes(StandardCharsets.UTF_8),0,outs,0,pLength);
            System.arraycopy(originalKey.getEncoded(),0,outs,pLength,16);
            System.arraycopy(iv.getEncoded(),0,outs,pLength+16,16);
            outs= encode.doFinal(outs);
            outputStream.write(outs);
            outputStream.flush();
            AesSocketWrapper aesSocketWrapper=new AesSocketWrapper(socket,originalKey.getEncoded(),iv.getEncoded());
            inputStream=aesSocketWrapper.getInputStream();
            outputStream=aesSocketWrapper.getOutputStream();
            socket.setSoTimeout(20000);
            l = inputStream.read(in);
            s = new String(in,0,l, StandardCharsets.UTF_8);
            reply=getPacket(seq,ack);
            if (!s.equals(reply)){
                throw new CipherHandShakeException("unexpect reply ,expect reply is "+reply+" but receive is "+s);
            }
            socket.setSoTimeout(0);
            inputStream=null;
            outputStream=null;
            decode=null;
            encode=null;
            outs=null;
            in=null;
            return aesSocketWrapper;
        }
        catch (Exception e){
            try {
                socket.close();
            } catch (IOException ex) {
                //do nothing
            }
            throw new CipherHandShakeException(e);
        }
    }

    private String getPacket(int seq,int ack){
        return "seq="+seq+","+"ack="+ack;
    }
}
