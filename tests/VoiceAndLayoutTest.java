import com.camacho.rgbcontrol.VoiceCommands;
import com.camacho.rgbcontrol.LgCodes;
import com.camacho.rgbcontrol.LgGeometry;

public final class VoiceAndLayoutTest {
    private static void check(boolean ok,String message) { if(!ok)throw new AssertionError(message); }
    private static void phrase(String words,int expected) { check(VoiceCommands.parse(words)==expected,"voice: "+words); }
    private static void layout(int width,int height,int l,int t,int r,int b) {
        LgGeometry g=new LgGeometry(width,height,l,t,r,b);
        for(int i=0;i<10;i++) {
            check(g.left(i)>=l && g.top(i)>=t,"safe start "+i);
            check(g.left(i)+Math.round(g.keyWidth[i])<=width-r && g.top(i)+Math.round(g.keyHeight[i])<=height-b,"safe end "+i);
            for(int j=i+1;j<10;j++) {
                boolean overlap=g.left(i)<g.left(j)+g.keyWidth[j] && g.left(j)<g.left(i)+g.keyWidth[i]
                    && g.top(i)<g.top(j)+g.keyHeight[j] && g.top(j)<g.top(i)+g.keyHeight[i];
                check(!overlap,"overlapping icons "+i+","+j+" at "+width+"x"+height);
            }
        }
        check(g.cx-g.radius>=l && g.cx+g.radius<=width-r && g.cy-g.radius>=t && g.cy+g.radius<=height-b,"direction ring inside safe area");
    }
    public static void main(String[] args) {
        phrase("Subí el volumen",LgCodes.VOLUME_UP);phrase("sube volumen",LgCodes.VOLUME_UP);phrase("más volumen",LgCodes.VOLUME_UP);
        phrase("Bajá el volumen",LgCodes.VOLUME_DOWN);phrase("menos volumen",LgCodes.VOLUME_DOWN);
        phrase("Silencio",LgCodes.MUTE);phrase("quitá el silencio",LgCodes.MUTE);phrase("mute",LgCodes.MUTE);
        phrase("Arriba",LgCodes.UP);phrase("abajo",LgCodes.DOWN);phrase("a la izquierda",LgCodes.LEFT);phrase("derecha",LgCodes.RIGHT);
        phrase("OK",LgCodes.OK);phrase("Confirmar",LgCodes.OK);phrase("click",LgCodes.OK);
        phrase("Encendé la tele",LgCodes.POWER);phrase("Apagá el televisor",LgCodes.POWER);
        phrase("Por favor subí el volumen",LgCodes.VOLUME_UP);phrase("bajá el volumen por favor",LgCodes.VOLUME_DOWN);
        for(String unknown:new String[]{"","no apagues la tele","no subas el volumen","buscar un video","volumen","hola",null})phrase(unknown,-1);
        int[][] sizes={{320,568},{360,800},{412,915},{1080,2400},{800,1280},{1280,800},{2400,1080},{600,600},{900,700}};
        for(int[] s:sizes) {layout(s[0],s[1],0,0,0,0);layout(s[0],s[1],8,32,8,48);}
        System.out.println("PASS: Spanish voice commands, rejection of unrelated/negative phrases, and adaptive icon-only TV layout");
    }
}
