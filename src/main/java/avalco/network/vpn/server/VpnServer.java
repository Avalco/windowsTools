package avalco.network.vpn.server;

import avalco.network.vpn.base.interfaces.ResourceRecovery;

public class VpnServer implements ResourceRecovery ,Runnable{
    @Override
    public void recoverResource() {

    }

    @Override
    public void run() {
        //create Server to  verify identify ,virtual dhcp ,hand key
        //create a route tables;
        //get ip:netmask:gateway by .config
        //create virtual IFace;
        //set ip:netmask:gateway
        //--||//get package from client
        //forward package by route tables||
    }
}
