package avalco.network.vpn;


import avalco.network.vpn.base.exception.InternetAddressException;
import avalco.network.vpn.base.netprotocol.IPPacket;
import avalco.network.vpn.base.VirtualInternetFace;
import avalco.network.vpn.base.exception.IPPacketException;
import avalco.network.vpn.base.netprotocol.IpProtocolTool;

import java.io.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class Test {
    private static byte[] buffer=null;
    public static void main(String [] args){
        try {
            System.out.println(IpProtocolTool.countMask("255.255.255.0"));
        } catch (InternetAddressException e) {
            throw new RuntimeException(e);
        }
      /*  VirtualInternetFace virtualInternetFace=new VirtualInternetFace();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                virtualInternetFace.close();
            }
        }));
        virtualInternetFace.createIFace("testTun");
        byte[]ip=new byte[]{(byte) 192, (byte) 168,23,1};
        virtualInternetFace.setIFaceConf(ip,24);
        virtualInternetFace.addReceiveListener(new VirtualInternetFace.ReceiveListener() {
            @Override
            public void receivePacket(byte[] bytes) {
                if (bytes!=null){
                    if (buffer==null){
                        buffer=bytes;
                    }
                    System.out.println(bytes.length+" "+Arrays.toString(bytes));
                    try {
                        IPPacket ipPacket=IPPacket.handlePacket(bytes);
                        System.out.println(ipPacket.toString()+"\n"+"data:["+new String(bytes,ipPacket.getHeaderLength(),bytes.length- ipPacket.getHeaderLength())+"]");
                    } catch (IPPacketException e) {
                        e.printStackTrace(new PrintStream(System.out));
                    }
                }
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(30000);
                    IPPacket ipPacket=IPPacket.handlePacket(buffer);
                    System.out.println("buffer handle:"+ipPacket.toString()+"\n");

                    System.out.println("send");
                    byte[]bytes=new byte[28];
                    Arrays.fill(bytes, (byte) 0);
                    bytes[0]=0x45;
                    bytes[2]=(20&0xff00)>>>8;
                    bytes[3]=(28&0xff);
                    bytes[8]= (byte) (255);
                    bytes[12]=(byte)(192);
                    bytes[13]=(byte)(168);
                    bytes[14]=(byte)(23);
                    bytes[15]=(byte)(3);
                    bytes[16]=(byte)(192);
                    bytes[17]=(byte)(168);
                    bytes[18]=(byte)(23);
                    bytes[19]=(byte)(5);
                    bytes[9]=1;
                    int cm=IPPacket.IPChecksum(bytes,0,20);
                    bytes[10]= (byte) ((cm&0xff00)>>>8);
                    bytes[11]= (byte) (cm&0x00ff);
                    cm=IPPacket.IPChecksum(bytes,20,8);
                    bytes[22]= (byte) ((cm&0xff00)>>>8);
                    bytes[23]= (byte) (cm&0x00ff);
                    System.out.println(Arrays.toString(bytes));
                    virtualInternetFace.sendPackets(bytes,28);
                    System.out.println("send success");
                } catch (InterruptedException | IPPacketException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(600000);
                    System.out.println("1-thread-close");
                    virtualInternetFace.close();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        System.out.println("main-thread-close");*/
       /* try {
            Thread.sleep(5000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
       /* try {
            PrivateKey privateKey= RsaTool.getPrivateKeyFromFile("server.psk");
            PublicKey publicKey= RsaTool.getPubKeyFromFile("server.pub");
            ServerSocket serverSocket=new ServerSocket(9000);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket=serverSocket.accept();
                        CipherConnection cipherConnection =new CipherConnection(privateKey);
                        socket= cipherConnection.connect(socket);
                        InputStream inputStream=socket.getInputStream();
                        OutputStream outputStream= socket.getOutputStream();
                        BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(outputStream));
                        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
                        String s;
                        while ((s= bufferedReader.readLine())!=null){
                            System.out.println("receive:"+s);
                            bufferedWriter.write(("reply to:"+s));
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                        }
                        inputStream.close();
                        outputStream.close();
                        socket.close();
                    }
                    catch (CipherHandShakeException | IOException e){
                        e.printStackTrace();
                    }

                }
            }).start();
           new Thread(new Runnable() {
               @Override
               public void run() {
                   try {
                       Socket socket=new Socket("127.0.0.1",9000);
                       CipherConnection cipherConnection =new CipherConnection(publicKey);
                       socket= cipherConnection.connect(socket);
                       InputStream inputStream=socket.getInputStream();
                       OutputStream outputStream= socket.getOutputStream();
                       Scanner scanner=new Scanner(System.in);
                       String s;
                       BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(outputStream));
                       BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
                       while (!(s=scanner.nextLine()).equals("end")){
                           bufferedWriter.write(("send to server:"+s));
                           bufferedWriter.newLine();
                           bufferedWriter.flush();
                           String s1= bufferedReader.readLine();
                           System.out.println("reply from server:"+s1);
                       }
                       inputStream.close();
                       outputStream.close();
                       socket.close();
                   } catch (IOException | CipherHandShakeException e) {
                      e.printStackTrace();
                   }

               } 96  4  45  228  2  152  17  1  254  128
               96, 4, 45, -28, 2, -104, 17, 1, -2, -128
           }).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
    }

}
