package avalco.network.vpn.server;

import java.net.SocketAddress;

public class Route {
    String token;
    byte[] ip;
    int mask;
    SocketAddress socketAddress;
}
