package avalco.network.vpn.base.orders;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Order {
    private String command;

    private static final char SPECIAL_CHAR='#';
    private static final String SPECIAL_SPACE=SPECIAL_CHAR+"S";
    private static final char SPACE=' ';
    private static final String DOUBLE_SPECIAL_CHAR="##";
    String []params;
    private static final String[] string0=new String[0];
    private final StringBuilder stringBuilder;
    private static final Map<String,Character> map=new HashMap<>();
    static {
        map.put(SPECIAL_SPACE,SPACE);
        map.put(DOUBLE_SPECIAL_CHAR,SPECIAL_CHAR);
    }
    private Order(){
        stringBuilder=new StringBuilder();
    }
    public static class OrderBuilder{
        private final Order order;
        private final List<String> params;
        public OrderBuilder(){
            order=new Order();
            params=new ArrayList<>();
        }
        public OrderBuilder setOrder(String command){
            order.command=command;
            return this;
        }
        public OrderBuilder addParams(String params){
            this.params.add(params);
            order.stringBuilder.append(" ");
            params=params.replace(""+SPECIAL_CHAR,DOUBLE_SPECIAL_CHAR);
            params=params.replace(""+SPACE,SPECIAL_SPACE);
            order.stringBuilder.append(params);
            return this;
        }
        public Order build(){
            order.params= this.params.toArray(string0);
            return order;
        }
    }

    @Override
    public String toString() {
        return command+stringBuilder.toString();
    }
    public static Order format(String s){
        Order order=new Order();
        String[] strings=s.split(" ");
        order.command=strings[0];
        if (strings.length>1){
            order.params=new String[strings.length-1];
            for (int i=1;i<strings.length;i++){
                order.params[i-1]= order.unSpecial(strings[i]);
                order.stringBuilder.append(" ");
                order.stringBuilder.append(strings[i]);
            }
        }
       return order;
    }

    public String getCommand() {
        return command;
    }

    public String[] getParams() {
        return params;
    }
    private String unSpecial(String s){
        StringBuilder stringBuilder=new StringBuilder();
        boolean flag=false;
        for (int i=0;i<s.length();i++){
            if (flag){
                stringBuilder.append(map.get("#"+s.charAt(i)));
                flag=false;
            }else {
                if (s.charAt(i)=='#'){
                    flag=true;
                }  else {
                    stringBuilder.append(s.charAt(i));
                }
            }
        }
        return stringBuilder.toString();
    }
}
