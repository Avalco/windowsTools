package avalco.network.vpn;

import avalco.network.vpn.base.ApplicationContext;
import avalco.network.vpn.base.interfaces.ResourceRecovery;
import avalco.network.vpn.client.VpnClient;
import avalco.network.vpn.server.VpnServer;

import java.util.ArrayList;
import java.util.List;

public class Application extends ApplicationContext {

    private List<ResourceRecovery> recoveryList;
    private Runnable runnable;
    public static void main(String []args){
        Application application=new Application();
        application.start();
    }

    @Override
    protected void onCreate() {
        recoveryList=new ArrayList<>();
        switch (applicationConf.getType()){
            case "Server":
                VpnServer vpnServer=new VpnServer(this);
                runnable=vpnServer;
                recoveryList.add(vpnServer);
                break;
            case "Client":
                VpnClient vpnClient=new VpnClient(this);
                recoveryList.add(vpnClient);
                runnable=vpnClient;
                break;
        }
    }

    @Override
    public void start() {
        runnable.run();
    }

    @Override
    public void shutdown() {
        if (logUtil!=null){
            logUtil.terminal();
        }
        if (recoveryList!=null){
            for (ResourceRecovery resourceRecovery:recoveryList){
                resourceRecovery.recoverResource();
            }
        }

    }

    @Override
    public void onApplicationExit() {
        shutdown();
    }
}
