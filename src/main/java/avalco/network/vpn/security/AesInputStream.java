package avalco.network.vpn.security;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AesInputStream extends InputStream {
    private final InputStream inputStream;
    private final Cipher cipher;
    private final byte []bytes=new byte[1024];
    private byte []buffer;
    private int offset=0;
    private boolean done=false;
    public AesInputStream(InputStream inputStream,byte[] key,byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        this.inputStream = inputStream;
        cipher=Cipher.getInstance("AES/CTR/NoPadding");
        SecretKeySpec keySpec=new SecretKeySpec(key,"AES");
        IvParameterSpec ivParameterSpec=new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE,keySpec,ivParameterSpec);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b,0,b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if(buffer==null||offset>= buffer.length){
            int l=0;
            for (l=0;l==0;l=getMore()){}
            if (l==-1){
                return -1;
            }
        }
        len=Math.min(len,buffer.length-offset);
        System.arraycopy(buffer,offset,b,off,len);
        offset+=len;
        return len;
    }

    private int getMore() {
        if (done){
            return -1;
        }
        try {
            int l=inputStream.read(bytes);
            if (l==-1){
                buffer=cipher.doFinal();
                done=true;
            }
            else {
                buffer=cipher.update(bytes,0,l);
            }
        } catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        if (buffer==null||buffer.length==0){
            return -1;
        }
        offset=0;
        return buffer.length;
    }

    @Override
    public long skip(long n) throws IOException {
        if (buffer==null){
            return 0;
        }
        int i=buffer.length-offset;
         i= (int) Math.min(n,i);
         i=Math.max(0,i);
         offset=offset+i;
         return i;
    }

    @Override
    public int available() throws IOException {
        return buffer!=null?buffer.length-offset:0;
    }

    @Override
    public void close() throws IOException {
        super.close();
    }


    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int read() throws IOException {
        if(buffer==null||offset>=buffer.length){
            int l=0;
            for (l=0;l==0;l=getMore()){}
            if (l==-1){
                return -1;
            }
        }
        return buffer[offset++]&255;
    }
}
