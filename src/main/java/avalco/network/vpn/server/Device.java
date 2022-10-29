package avalco.network.vpn.server;

import avalco.network.vpn.base.exception.NoExitException;
import avalco.network.vpn.base.orders.Order;
import avalco.network.vpn.security.AesSocketWrapper;
import avalco.network.vpn.security.CipherConnection;



import java.io.*;
import java.net.Socket;


public class Device implements Runnable{
    private static final String TAG="Device";
    private String token;
    private Socket socket;
    private int dataPort;
    private boolean quit;
    private VpnServer server;
    private String ip;
    private String mask;
    private String gateway;
    private String name;
    public CipherConnection cipherConnection;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    byte[]key;
    byte[]iv;
    public Device(Socket socket,CipherConnection cipherConnection,int dataPort,VpnServer server){
        this.socket=socket;
        this.cipherConnection=cipherConnection;
        this.dataPort=dataPort;
        this.server=server;
    }
    @Override
    public void run() {
        try {
            socket=cipherConnection.connect(socket);
            key=((AesSocketWrapper)socket).getKey();
            iv=((AesSocketWrapper)socket).getIv();
            bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            socket.setSoTimeout(20000);
            String s=bufferedReader.readLine();
            server.getLogUtil().e(TAG,s,null);
            socket.setSoTimeout(0);
            Order order=Order.format(s);
            if (!order.getCommand().equals("AUTH")){
                bufferedWriter.write("must auth before other operation");
                bufferedWriter.newLine();
                bufferedWriter.flush();
                socket.shutdownInput();
                return;
            }
            String reply=handle(s);
            server.getLogUtil().d(TAG,reply);
            bufferedWriter.write(reply);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            quit=false;
            while (!Thread.interrupted()&&!quit){
               String s1= bufferedReader.readLine();
               String reply1=handle(s1);
                bufferedWriter.write(reply1);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (Exception e) {
            try {
                if (!socket.isClosed())
                {
                    socket.close();
                }
            } catch (IOException ex) {
                //do nothing
            }
            throw new NoExitException(e);
        }
        server.deleteDevice(name,token);
    }

    private String handle(String s) {
       server.getLogUtil().d(TAG,"handel " +s);
        Order order=Order.format(s);
        if (!order.getCommand().equals("AUTH")&&!order.getParams()[0].equals(token)){
            return "error token";
        }
        String reply="";
        switch (order.getCommand()){
            case "AUTH":
                reply=server.registerDevice(order.getParams()[0],this);
                break;
            case "DHCP":
                reply=server.dhcp(order.getParams()[0]);
                break;
            case "QUIT":
                reply=server.deleteDevice(name,token);
                quit=true;
                break;
            case "CONF":
                reply=server.configIp(order.getParams()[0],order.getParams()[1],order.getParams()[2],order.getParams()[3]);
                break;
            case "DATA-PORT":
                reply=dataPort+"";
                break;
        }
        server.getLogUtil().d(TAG,"reply:"+reply);
        return reply;
    }
    public void  close(String s){
        quit=true;
        try {
            bufferedWriter.write(s);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (!socket.isClosed())
                    {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }).start();

        } catch (IOException e) {
            //
        }
    }

    public void setToken(String token) {
        this.token=token;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }
}
