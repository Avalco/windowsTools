package avalco.network.vpn.client;


import avalco.network.vpn.base.ApplicationContext;
import avalco.network.vpn.base.conf.ApplicationConf;
import avalco.network.vpn.base.interfaces.ResourceRecovery;


import java.io.IOException;
import java.net.Socket;

public class VpnClient  implements ResourceRecovery ,Runnable{
    private final ApplicationContext context;

    public VpnClient(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void recoverResource() {

    }

    @Override
    public void run() {
        ApplicationConf applicationConf=context.getApplicationConf();
        try {
            Socket socket=new Socket(applicationConf.getServerHost(),applicationConf.getServerPort());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //connect to server
        //verify identify
        //get ip:netmask:gateway by virtual dhcp or .config
        //create virtual IFace;
        //set ip:netmask:gateway
        //add route net:netmask:----->interface
        //--||//listen fd of virtual interface to get packages
        //send package to server by udp ||
        //--||//get package from server
        //forward package to virtual interface||
    }
}
