package avalco.network.vpn.server;

import java.net.SocketAddress;

public class Packet {
    String token;
    byte[]data;
    int length;
    SocketAddress srcAddress;
}
