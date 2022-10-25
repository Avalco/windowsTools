package avalco.network.vpn.security;

import avalco.network.vpn.base.exception.NoExitException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RsaTool {
    private static final int KEY_SIZE=2048;
    public static void createKeyPair(String pub,String pri) throws IOException {
        KeyPairGenerator keyPairGenerator= null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new NoExitException(e);
        }
        keyPairGenerator.initialize(KEY_SIZE,new SecureRandom());
        KeyPair keyPair=keyPairGenerator.generateKeyPair();
        PublicKey publicKey= keyPair.getPublic();
        PrivateKey privateKey= keyPair.getPrivate();
        OutputStream outputStream= Files.newOutputStream(new File(pub).toPath());
        BASE64Encoder base64Encoder=new BASE64Encoder();
        outputStream.write(base64Encoder.encode(publicKey.getEncoded()).getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();
        outputStream=Files.newOutputStream(new File(pri).toPath());
        outputStream.write(base64Encoder.encode(privateKey.getEncoded()).getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();
    }
    public static PublicKey getPubKeyFromFile(String file) throws IOException {
        BASE64Decoder base64Decoder=new BASE64Decoder();
        InputStream inputStream=Files.newInputStream(new File(file).toPath());
        byte[]bytes= base64Decoder.decodeBuffer(inputStream);
        X509EncodedKeySpec x509EncodedKeySpec=new X509EncodedKeySpec(bytes);
        try {
            return KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e ) {
            throw new NoExitException(e);
        }
    }
    public static PrivateKey getPrivateKeyFromFile(String file) throws IOException {
        BASE64Decoder base64Decoder=new BASE64Decoder();
        InputStream inputStream=Files.newInputStream(new File(file).toPath());
        byte[]bytes= base64Decoder.decodeBuffer(inputStream);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec=new PKCS8EncodedKeySpec(bytes);
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(pkcs8EncodedKeySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e ) {
            throw new NoExitException(e);
        }
    }
}
