package avalco.network.vpn.base;

import avalco.network.vpn.base.exception.ConfigFormatError;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfParse<T> {
    private final Class<T> tClass;

    public ConfParse(Class<T> tClass) {
        this.tClass = tClass;
    }
    public T parse(File cfg) throws IOException, ConfigFormatError, InstantiationException, IllegalAccessException{
        return parse(new BufferedReader(new FileReader(cfg)));
    }
    public T parse(BufferedReader reader) throws IOException, ConfigFormatError, InstantiationException, IllegalAccessException {
        String s;
        int line=0;
        T t= tClass.newInstance();
        while ((s=reader.readLine())!=null){
            line++;
            s=s.trim();
            if (!s.equals("")&&s.charAt(0)!='#'){
                int index=-1;
                String key="";
                String value="";
                String typeName="";
                try {
                     index=s.indexOf(":");
                    while (s.charAt(index-1)=='\\'){
                        s=s.substring(0,index-1)+s.substring(index);
                        index=s.indexOf(":",index);
                    }
                        key=s.substring(0,index);
                        key=key.trim();
                        key=forMateKey(key);
                        value=s.substring(index+1);
                        value=value.trim();
                        Field field=tClass.getDeclaredField(key);
                        field.setAccessible(true);
                        Type type=field.getAnnotatedType().getType();
                        typeName=type.getTypeName();
                        switch (typeName){
                            case "int":
                                field.setInt(t,Integer.parseInt(value));
                                break;
                            case "double":
                                field.setDouble(t,Double.parseDouble(value));
                                break;
                            case "boolean":
                                field.setBoolean(t,Boolean.parseBoolean(value));
                                break;
                            case "short":
                                field.setShort(t,Short.parseShort(value));
                                break;
                            case "byte":
                                field.setByte(t,Byte.parseByte(value));
                                break;
                            case "long":
                                field.setLong(t,Long.parseLong(value));
                                break;
                            case "float":
                                field.setFloat(t,Float.parseFloat(value));
                                break;
                            case "java.lang.String":
                                field.set(t,value);
                                break;
                        }
                }catch (StringIndexOutOfBoundsException e){
                    throw new ConfigFormatError("parse config error in line of "+line+",char at "+index);
                } catch (NoSuchFieldException e) {
                    //ignore
                }catch (IllegalArgumentException e){
                    throw new ConfigFormatError("parse config error in line of "+line+
                            ",char at "+index+", the key "+key+" require type of "+typeName+" but available value is "+value);
                }

            }
        }
        return  t;
    }

    private String forMateKey(String key) {
        String regex= "-[a-z]";
        Pattern pattern= Pattern.compile(regex);
        Matcher matcher=pattern.matcher(key);
        while (matcher.find()){
            String s=matcher.group();
//            logUtils.d(s);
            key= key.replace(s,s.toUpperCase(Locale.ROOT).substring(1));
        }
        return key;
    }
}
