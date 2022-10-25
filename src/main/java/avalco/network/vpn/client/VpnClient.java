package avalco.network.vpn.client;


import avalco.network.vpn.base.ApplicationContext;
import avalco.network.vpn.base.ConfParse;
import avalco.network.vpn.base.VirtualInternetFace;
import avalco.network.vpn.base.conf.ApplicationConf;
import avalco.network.vpn.base.exception.IPPacketException;
import avalco.network.vpn.base.interfaces.ResourceRecovery;
import avalco.network.vpn.base.netprotocol.IPPacket;
import avalco.network.vpn.base.netprotocol.IpProtocolTool;
import avalco.network.vpn.base.orders.Order;
import avalco.network.vpn.base.orders.OrderFactory;
import avalco.network.vpn.security.AesSocketWrapper;
import avalco.network.vpn.security.CipherConnection;
import avalco.network.vpn.security.RsaTool;


import java.io.*;

import java.net.DatagramSocket;
import java.net.Socket;

import java.security.PublicKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VpnClient  implements ResourceRecovery ,Runnable{
    private static final String TAG="VpnClient";
    private static final String CONFIG_NAME="client";
    private final ApplicationContext context;
    private Socket client;
    private String token;
    private ExecutorService executorService;
    private  DatagramSocket datagramSocket;
    public VpnClient(ApplicationContext context) {
        this.context = context;
        executorService= Executors.newCachedThreadPool();
    }

    @Override
    public void recoverResource() {
        if (client!=null){
            try {
                client.close();
            } catch (IOException e) {
                context.getLogUtil().e(TAG,"recoverResource",e);
            }
        }
        if (datagramSocket!=null){
                datagramSocket.close();
        }
        if (executorService!=null){
            executorService.shutdown();
        }
    }

    @Override
    public void run() {
        ApplicationConf applicationConf=context.getApplicationConf();
        try {
            PublicKey publicKey= RsaTool.getPubKeyFromFile(applicationConf.getServerKeyPath());
            CipherConnection cipherConnection=new CipherConnection(publicKey);
            client=cipherConnection.connect(new Socket(applicationConf.getServerHost(),applicationConf.getServerPort()));
            byte[]key=((AesSocketWrapper)client).getKey();
            byte[]iv=((AesSocketWrapper)client).getIv();
            OrderSender orderSender=new OrderSender(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())));
            executorService.execute(orderSender);
            orderSender.send(OrderFactory.Auth(applicationConf.getUserName(),applicationConf.getPassword()));
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(client.getInputStream()));
            String s= bufferedReader.readLine();
            System.out.println(s);
            Order order=Order.format(s);
            if (!order.getCommand().equals("TOKEN")){
                throw new RuntimeException("Auth error");
            }
            token=order.getParams()[0];
            File configFile=context.getConfigFile(CONFIG_NAME);
            ConfParse<ClientConf> confParse=new ConfParse<>(ClientConf.class);
            ClientConf clientConf= confParse.parse(configFile);
            if (clientConf.type==0){
                orderSender.send(OrderFactory.DHCP(token));
                s=bufferedReader.readLine();
                Order conf=Order.format(s);
                clientConf.ip=conf.getParams()[1];
                clientConf.gateway=conf.getParams()[3];
                clientConf.mask=conf.getParams()[2];
            }else {
                orderSender.send(OrderFactory.Conf(token, clientConf.ip, clientConf.mask, clientConf.gateway));
                s=bufferedReader.readLine();
                if (!s.equals("OK")){
                    throw new RuntimeException("static ip error "+s);
                }
            }
            orderSender.send(OrderFactory.getPort(token));
            s=bufferedReader.readLine();
            int port=Integer.parseInt(s);
            datagramSocket=new DatagramSocket();
            PacketSender packetSender=new PacketSender(applicationConf.getServerHost(),port,datagramSocket,token,key,iv);
            executorService.execute(packetSender);
            VirtualInternetFace virtualInternetFace=new VirtualInternetFace();
            virtualInternetFace.createIFace("Vpn-tun");
            byte[]ip= IpProtocolTool.encodeIp(clientConf.ip);
            int mask=IpProtocolTool.countMask(clientConf.mask);
            virtualInternetFace.setIFaceConf(ip,mask);
            PacketReceiver packetReceiver=new PacketReceiver(datagramSocket,token,key,iv, new ReceiveListener() {
                @Override
                public void receivePacket(byte[] bytes, int l) {
                    virtualInternetFace.sendPackets(bytes,l);
                }
            });
            executorService.execute(packetReceiver);
            virtualInternetFace.addReceiveListener(new VirtualInternetFace.ReceiveListener() {
                @Override
                public void receivePacket(byte[] bytes) {
                    try {
                        IPPacket ipPacket=IPPacket.handlePacket(bytes);
                        if (ipPacket.getDst().substring(0,mask).equals(clientConf.gateway.substring(0,mask))){
                            packetSender.send(bytes);
                        }
                    } catch (IPPacketException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (Exception e) {
            context.getLogUtil().e(TAG,"connect server",e);
            context.shutdown();
        }
    }



}
