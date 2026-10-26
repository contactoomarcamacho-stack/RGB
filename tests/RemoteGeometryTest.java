import com.camacho.rgbcontrol.RemoteGeometry;

public final class RemoteGeometryTest {
    private static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
    private static void verify(int width,int height,int l,int t,int r,int b) {
        RemoteGeometry g=new RemoteGeometry(width,height,l,t,r,b);
        check(g.width >= (width-l-r)*.97f && g.height >= (height-t-b)*.97f,"body must fill both dimensions");
        check(g.left>=l && g.top>=t && g.left+g.width<=width-r+.01f && g.top+g.height<=height-b+.01f,"body inside safe area");
        for(int i=0;i<24;i++) {
            int x=g.keyLeft(i),y=g.keyTop(i);
            check(x>=l && y>=t && x+g.keySize<=width-r && y+g.keySize<=height-b,"key inside screen: "+i);
            for(int j=i+1;j<24;j++) {
                boolean overlap=x<g.keyLeft(j)+g.keySize && g.keyLeft(j)<x+g.keySize
                    && y<g.keyTop(j)+g.keySize && g.keyTop(j)<y+g.keySize;
                check(!overlap,"overlapping keys: "+i+","+j);
            }
        }
        check(g.keyTop(23)+g.keySize > t+(height-t-b)*.84f,"bottom row uses screen height");
    }
    public static void main(String[] args) {
        int[][] sizes={{320,568},{360,800},{412,915},{1080,2400},{720,1600},{800,1280},{1600,2560},{1280,800},{2400,1080}};
        for(int[] size:sizes) {
            verify(size[0],size[1],0,0,0,0);
            verify(size[0],size[1],12,48,12,72);
        }
        verify(1080,2400,0,110,0,90);
        verify(1080,2400,0,0,0,0);
        System.out.println("PASS: remote fills portrait, tablet and landscape screens; insets, all keys and non-overlap verified");
    }
}
