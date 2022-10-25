package avalco.network.vpn.server;

import java.util.HashMap;
import java.util.Map;

public class RouteTables {
    Map<String,Route> routeTables;
    public RouteTables() {
        routeTables=new HashMap<>();
    }
    public void add(String ip,Route route){
        routeTables.put(ip,route);
    }
    public Route getRoute(String ip){
        return routeTables.get(ip);
    }
}
