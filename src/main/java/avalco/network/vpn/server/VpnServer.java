package avalco.network.vpn.server;

import avalco.network.vpn.base.ApplicationContext;
import avalco.network.vpn.base.ConfParse;
import avalco.network.vpn.base.conf.ApplicationConf;
import avalco.network.vpn.base.exception.IPPacketException;
import avalco.network.vpn.base.exception.InternetAddressException;
import avalco.network.vpn.base.interfaces.ResourceRecovery;
import avalco.network.vpn.base.netprotocol.IPPacket;
import avalco.network.vpn.base.netprotocol.IPV4Packet;
import avalco.network.vpn.base.netprotocol.IpProtocolTool;
import avalco.network.vpn.base.orders.Order;
import avalco.network.vpn.base.orders.OrderFactory;
import avalco.network.vpn.security.CipherConnection;
import avalco.network.vpn.security.RsaTool;
import avalco.tools.logs.LogUtil;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static avalco.network.vpn.base.netprotocol.IpProtocolTool.countMask;
import static avalco.network.vpn.base.netprotocol.IpProtocolTool.encodeIp;

public class VpnServer implements ResourceRecovery ,Runnable{
    private final ApplicationContext context;
    private static final String TAG="VpnServer";
    private static final String CONFIG_NAME="server";
    private ExecutorService executorService;
    private DatagramSocket datagramSocket;
    private ServerSocket serverSocket;
    private ServerConf serverConf;
    private boolean shutdown;
    private int headLength=generateNewToken().getBytes(StandardCharsets.UTF_8).length;
    private Map<String,String>tokenMap;
    private Map<String,Device>deviceMap;
    private Set<String> ipPool;
    private RouteTables routeTables;
    private DataHandle dataHandle;
    public VpnServer(ApplicationContext context) {
        shutdown=false;
        this.context = context;
        deviceMap=new HashMap<>();
        tokenMap=new HashMap<>();
        ipPool=new HashSet<>();
        routeTables=new RouteTables();
        executorService= Executors.newCachedThreadPool();
    }

    @Override
    public void recoverResource() {
        shutdown=true;
        if (serverSocket!=null&&!serverSocket.isClosed()){
            try {
                serverSocket.close();
            } catch (IOException e) {
                context.getLogUtil().e(TAG," ",e);
            }
        }
        if (datagramSocket!=null&& !datagramSocket.isClosed()){
            datagramSocket.close();
        }
        if (!executorService.isShutdown()){
            executorService.shutdown();
        }
    }

    @Override
    public void run() {
        ApplicationConf applicationConf=context.getApplicationConf();
        try {
            serverSocket=new ServerSocket(applicationConf.getServerPort());
            datagramSocket=new DatagramSocket();
            File configFile=context.getConfigFile(CONFIG_NAME);
            ConfParse<ServerConf> confParse=new ConfParse<>(ServerConf.class);
            serverConf= confParse.parse(configFile);
            PrivateKey privateKey= RsaTool.getPrivateKeyFromFile(serverConf.getPrivateKeyPath());
            CipherConnection cipherConnection=new CipherConnection(privateKey);
            dataHandle=new DataHandle();
            executorService.execute(new DataListener());
            executorService.execute(dataHandle);
            executorService.execute(new DataSender());
            executorService.execute(new DataSender());
            executorService.execute(new DataSender());
            while (!shutdown){
                Socket socket=serverSocket.accept();
                executorService.execute(new Device(socket,cipherConnection,datagramSocket.getPort(),this));
            }
        } catch (IOException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    private class DataListener implements Runnable{
        byte[]buffer=new byte[65535];
        @Override
        public void run() {
            while (!Thread.interrupted()&&!shutdown){
                try {
                    DatagramPacket datagramPacket=new DatagramPacket(buffer, buffer.length);
                    datagramSocket.receive(datagramPacket);
                    String token=new String(datagramPacket.getData(),0,headLength,StandardCharsets.UTF_8);
                    if (deviceMap.get(token)!=null){
                        Packet packet=new Packet();
                        packet.srcAddress=datagramPacket.getSocketAddress();
                        packet.token=token;
                        packet.data=Arrays.copyOfRange(datagramPacket.getData(),headLength,datagramPacket.getLength());
                        dataHandle.hand(packet);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    LinkedBlockingQueue<Packet> packets=new LinkedBlockingQueue<>();
    private class DataHandle implements Runnable{
        LinkedBlockingQueue<Packet> packets=new LinkedBlockingQueue<>();

        @Override
        public void run() {
            while (!Thread.interrupted()&&!shutdown){
                try {
                    Packet packet=packets.take();
                    try {
                        Device device=deviceMap.get(packet.token);
                        Cipher cipher=Cipher.getInstance("AES/CTR/NoPadding");
                        SecretKeySpec keySpec=new SecretKeySpec(device.key,"AES");
                        IvParameterSpec ivParameterSpec=new IvParameterSpec(device.iv);
                        cipher.init(Cipher.DECRYPT_MODE,keySpec,ivParameterSpec);
                        packet.data= cipher.doFinal(packet.data);
                        VpnServer.this.packets.add(packet);
                    }catch (Exception e){
                        context.getLogUtil().e(TAG,"dataHandle",e);
                    }
                } catch (InterruptedException e) {
                    context.getLogUtil().e(TAG,"dataHandle",e);
                }
            }
        }
        public void hand(Packet packet){
            packets.add(packet);
        }
    }
    private class DataSender implements Runnable{
        @Override
        public void run() {
            try {
                while (!Thread.interrupted()&&!shutdown){
                    Packet packet=packets.take();
                    try {
                        IPPacket ipv4Packet= IPPacket.handlePacket(packet.data);
                        String dst= ipv4Packet.getDst();
                        String src=ipv4Packet.getSrc();
                        Route dstRoute= routeTables.getRoute(dst);
                        Route srcRout=routeTables.getRoute(src);
                        srcRout.socketAddress= packet.srcAddress;
                        if (dstRoute!=null){
                            Device device=deviceMap.get(dstRoute.token);
                            if (device!=null&&dstRoute.socketAddress!=null){
                                Cipher cipher=Cipher.getInstance("AES/CTR/NoPadding");
                                SecretKeySpec keySpec=new SecretKeySpec(device.key,"AES");
                                IvParameterSpec ivParameterSpec=new IvParameterSpec(device.iv);
                                cipher.init(Cipher.ENCRYPT_MODE,keySpec,ivParameterSpec);
                                packet.data= cipher.doFinal(packet.data);
                                byte[] buffer= Arrays.copyOf(dstRoute.token.getBytes(StandardCharsets.UTF_8)
                                        ,headLength+packet.data.length);
                                System.arraycopy(packet.data,0,buffer,headLength,packet.data.length);
                                DatagramPacket datagramPacket=new DatagramPacket(buffer,buffer.length,dstRoute.socketAddress);
                                datagramSocket.send(datagramPacket);
                            }
                        }
                    }catch (Exception e){
                        context.getLogUtil().e(TAG,"dataSender",e);
                    }
                }
            } catch (InterruptedException e) {
                context.getLogUtil().e(TAG,"dataSender",e);
            }
        }
    }
    public String registerDevice(String param,Device device) {
        String d=tokenMap.getOrDefault(param,null);
        System.out.println("registerDevice "+d);
        if (d!=null){
            tokenMap.remove(param);
            Device device1=deviceMap.remove(d);
            ipPool.remove(device1.getIp());
            device1.close("user login in new device");
        }
        String token= generateNewToken();
        tokenMap.put(param,token);
        deviceMap.put(token, device);
        device.setToken(token);
        device.setName(param);
        return OrderFactory.Token(token).toString();
    }
    public String dhcp(String param) {
        Device device=deviceMap.get(param);
        if (device==null){
            return "device not auth";
        }
        String ip=generateNewIpv4();
        String mask=serverConf.mask;
        String gateway=serverConf.gateway;
        Order con=OrderFactory.Conf(param,ip,mask,gateway);
        device.setIp(ip);
        try {
            byte[] ips=encodeIp(ip);
            Route route=new Route();
            route.ip=ips;
            route.mask=countMask(mask);
            route.token=param;
            routeTables.add(ip,route);
        } catch (InternetAddressException e) {
            throw new RuntimeException(e);
        }
        return con.toString();
    }
private static final String TokenCharSet="0123456789zxcvbnmasdfghjklqwertyuiop=+AZXCSVDBFGNHMJKLQWERTYUIOP";
    private String generateNewToken() {
        StringBuilder stringBuilder=new StringBuilder();
        for (int i=0;i<16;i++){
            int index= (int) (Math.random()*TokenCharSet.length());
            stringBuilder.append(TokenCharSet.charAt(index));
        }
        return stringBuilder.toString();
    }

    private String generateNewIpv4() {
        try {
            byte[]ip=encodeIp(serverConf.gateway);
            System.out.println("mask:"+serverConf.mask);
            int mask=countMask(serverConf.mask);
            System.out.println("masknum:"+mask);
            int hostNum=32-mask;
            System.out.println("hostnum:"+hostNum);
            int maxHostNum= (int) (Math.pow(2,hostNum)-2);
            int host= (int) (Math.random()*maxHostNum+1);
            System.out.println("host:"+host);
            //1-2^hostNum-2;
            int index=4-hostNum/8;//2
            System.out.println("index:"+index);
            int remain=hostNum%8;//4
            System.out.println("remain:"+remain);
            if (remain!=0){
                ip[index-1]= (byte) (ip[index-1] |((host>>>(hostNum-remain))));
            }
            for (int i=index;i<4;i++){
                ip[i]= (byte) (host>>>(4-i-1)*8);
            }
            int p1,p2,p3,p4;
            p1=ip[0]&0xff;
            p2=ip[1]&0xff;
            p3=ip[2]&0xff;
            p4=ip[3]&0xff;
            String nIp= p1+"."+p2+"."+p3+"."+p4;
            System.out.println("nip:"+nIp);
            if (ipPool.add(nIp)){
                return nIp;
            }
            else return generateNewIpv4();
        } catch (InternetAddressException e) {
            throw new RuntimeException(e);
        }
    }

    public String deleteDevice(String name, String token) {
        if (name!=null){
            tokenMap.remove(name);
        }
        if (token!=null){
            Device device1=deviceMap.remove(token);
            ipPool.remove(device1.getIp());
            device1.close("client quit");
        }
        return "OK";
    }

    public String configIp(String param, String param1, String param2, String param3) {
        Device device=deviceMap.get(param);
        if (device==null){
            return "device not auth";
        }
        if (ipPool.add(param1)){
            device.setIp(param1);
            device.setMask(param2);
            device.setGateway(param3);
            try {
                byte[] ips=encodeIp(param1);
                Route route=new Route();
                route.ip=ips;
                route.mask=countMask(param2);
                route.token=param;
                routeTables.add(param1,route);
            } catch (InternetAddressException e) {
                throw new RuntimeException(e);
            }
            return "OK";
        }
        return "ip address conflict";
    }
public LogUtil getLogUtil(){
        return context.getLogUtil();
}
}
