package avalco.network.vpn.base.orders;

public class OrderFactory {
    public static Order Auth(String name,String psk){
       return new Order.OrderBuilder()
               .setOrder("AUTH")
               .addParams(name)
               .addParams(psk)
               .build();
    }
    public static Order DHCP(String token){
        return new Order.OrderBuilder()
                .setOrder("DHCP")
                .addParams(token)
                .build();
    }
    public static Order Quit(String token){
        return new Order.OrderBuilder()
                .setOrder("QUIT")
                .addParams(token)
                .build();
    }
    public static Order Conf(String token,String ip,String mask,String gateway){
        return new Order.OrderBuilder()
                .setOrder("CONF")
                .addParams(token)
                .addParams(ip)
                .addParams(mask)
                .addParams(gateway)
                .build();
    }
    public static Order getPort(String token){
        return new Order.OrderBuilder()
                .setOrder("DATA-PORT")
                .addParams(token)
                .build();
    }
    public static Order Token(String token){
        return new Order.OrderBuilder()
                .setOrder("TOKEN")
                .addParams(token)
                .build();
    }
}
