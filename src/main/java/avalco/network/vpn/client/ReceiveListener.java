package avalco.network.vpn.client;

public interface ReceiveListener {
   void receivePacket(byte[]bytes,int l);
}
