package avalco.network.vpn.base.exception;

import java.io.IOException;

public class ConfigFormatError extends IOException {
    public ConfigFormatError(String message) {
        super(message);
    }
}
