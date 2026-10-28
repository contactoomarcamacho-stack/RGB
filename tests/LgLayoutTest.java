import com.camacho.rgbcontrol.LgGeometry;

public final class LgLayoutTest {
    private static void check(boolean ok,String message) { if(!ok)throw new AssertionError(message); }
    private static void layout(int width,int height,int l,int t,int r,int b) {
        LgGeometry g=new LgGeometry(width,height,l,t,r,b);
        for(int i=0;i<9;i++) {
            check(g.left(i)>=l && g.top(i)>=t,"safe start "+i);
            check(g.left(i)+Math.round(g.keyWidth[i])<=width-r && g.top(i)+Math.round(g.keyHeight[i])<=height-b,"safe end "+i);
            for(int j=i+1;j<9;j++) {
                boolean overlap=g.left(i)<g.left(j)+g.keyWidth[j] && g.left(j)<g.left(i)+g.keyWidth[i]
                    && g.top(i)<g.top(j)+g.keyHeight[j] && g.top(j)<g.top(i)+g.keyHeight[i];
                check(!overlap,"overlapping icons "+i+","+j+" at "+width+"x"+height);
            }
        }
        check(g.cx-g.radius>=l && g.cx+g.radius<=width-r && g.cy-g.radius>=t && g.cy+g.radius<=height-b,"direction ring inside safe area");
    }
    public static void main(String[] args) {
        int[][] sizes={{320,568},{360,800},{412,915},{1080,2400},{800,1280},{1280,800},{2400,1080},{600,600},{900,700}};
        for(int[] s:sizes) {layout(s[0],s[1],0,0,0,0);layout(s[0],s[1],8,32,8,48);}
        System.out.println("PASS: nine TV icons stay inside safe areas without overlap in portrait and landscape");
    }
}
