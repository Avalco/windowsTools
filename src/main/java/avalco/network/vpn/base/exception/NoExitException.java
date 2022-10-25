package avalco.network.vpn.base.exception;

public class NoExitException extends RuntimeException{
    public NoExitException(Throwable cause) {
        super(cause);
    }

    public NoExitException() {
        super();
    }

    public NoExitException(String message) {
        super(message);
    }

    public NoExitException(String message, Throwable cause) {
        super(message, cause);
    }

    protected NoExitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
