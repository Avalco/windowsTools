package avalco.tools.windows;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class Main {
    enum KeyCode {
        QUIT('1', JIntellitype.MOD_CONTROL),
        CLIPBOARD('2', JIntellitype.MOD_CONTROL),
        SCREENSHOT('3',JIntellitype.MOD_CONTROL);
        final int identify;
        static final Map<Integer,KeyCode> map=new HashMap<>();
        static {
            for (KeyCode keyCode:KeyCode.values()){
                map.put(keyCode.identify,keyCode);
            }
        }
        KeyCode(int code, int modify) {
            this.identify = ((modify & 0xFF) << 24) | (code & 0x00FFFFFF);
        }

        public int getModify() {
            return (identify >>> 24) & 0xFF;
        }

        public int getCode() {
            return (identify & 0x00FFFFFF);
        }

        public static KeyCode getByIdentify(int identify){
            return map.get(identify);
        }
    }

    public static void main(String[] args) {
        for (KeyCode keyCode:KeyCode.values()){
            JIntellitype.getInstance().registerHotKey(keyCode.identify, keyCode.getModify(), keyCode.getCode());
        }
        JIntellitype.getInstance().addHotKeyListener(new HotkeyListener() {
            @Override
            public void onHotKey(int identifier) {
                KeyCode keyCode=KeyCode.getByIdentify(identifier);
                switch (keyCode) {
                    case QUIT:
                        System.exit(0);
                        break;
                    case SCREENSHOT:
                        //创建一个robot对象
                        Robot robut= null;
                        try {
                            System.out.println("create screenshot");
                            System.out.println(System.currentTimeMillis());
                            robut = new Robot();
                            //获取屏幕分辨率
                            Dimension d=  Toolkit.getDefaultToolkit().getScreenSize();
                            //创建该分辨率的矩形对象
                            Rectangle screenRect=new  Rectangle(d);
                            //根据这个矩形截图
                            BufferedImage bufferedImage=robut.createScreenCapture(screenRect);
                            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
                            String name="screenshot-"+simpleDateFormat.format(System.currentTimeMillis())+".png";
                            File file=new File("E:\\xzf\\WinTools\\Screenshot\\"+name);
                            System.out.println("create screenshot:"+file.getAbsolutePath());
                            ImageIO.write(bufferedImage,"png",file);
                            System.out.println(System.currentTimeMillis());
                        } catch (AWTException | IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });
    }
}
