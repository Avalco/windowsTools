package avalco.network.vpn.security.exception;

public class CipherHandShakeException extends Exception{
    public CipherHandShakeException(String message) {
        super(message);
    }

    public CipherHandShakeException(Throwable cause) {
        super(cause);
    }

    public CipherHandShakeException(String message, Throwable cause) {
        super(message, cause);
    }
}
