package avalco.network.vpn.client;


import avalco.network.vpn.base.interfaces.ResourceRecovery;

public class VpnClient  implements ResourceRecovery ,Runnable{

    @Override
    public void recoverResource() {

    }

    @Override
    public void run() {
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
