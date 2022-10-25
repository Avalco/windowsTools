package avalco.network.vpn.security;


import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


public class AesOutputStream extends OutputStream {
    private final OutputStream outputStream;
    private final byte[] iBytes;
    private byte [] bytes;
    private final Cipher cipher;
    public AesOutputStream(OutputStream outputStream,byte[] key,byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        this.outputStream = outputStream;
        cipher=Cipher.getInstance("AES/CTR/NoPadding");
        SecretKeySpec keySpec=new SecretKeySpec(key,"AES");
        IvParameterSpec ivParameterSpec=new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE,keySpec,ivParameterSpec);
        iBytes=new byte[1];
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b,0,b.length);

    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        bytes=cipher.update(b,0,len);
        if (bytes!=null){
            outputStream.write(bytes);
            bytes=null;
        }
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        try {
            bytes=cipher.doFinal();
            if (bytes!=null){
                outputStream.write(bytes);
                bytes=null;
            }
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        outputStream.close();
    }

    @Override
    public void write(int b) throws IOException {
        iBytes[0]= (byte) b;
       bytes=cipher.update(iBytes,0,1);
        if (bytes!=null){
            outputStream.write(bytes);
            bytes=null;
        }
    }
}
