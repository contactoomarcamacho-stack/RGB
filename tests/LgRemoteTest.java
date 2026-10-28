import com.camacho.rgbcontrol.IrCodes;
import com.camacho.rgbcontrol.LgCodes;
import com.camacho.rgbcontrol.LocalTvAddress;
import com.camacho.rgbcontrol.MotionMath;
import java.util.Arrays;

public final class LgRemoteTest {
    private static void check(boolean ok,String message) { if(!ok)throw new AssertionError(message); }
    public static void main(String[] args) {
        check(LgCodes.frame(0)==0x04FB08F7L,"LG power wire bytes");
        check(Arrays.equals(IrCodes.pattern(LgCodes.frame(0),false),IrCodes.pattern(0x20DF10EFL,true)),"LG power MSB/LSB equivalence");
        check(Arrays.equals(IrCodes.pattern(LgCodes.frame(6),false),IrCodes.pattern(0x20DF40BFL,true)),"volume up equivalence");
        for(int i=0;i<9;i++) { long frame=LgCodes.frame(i);check((frame>>>16)==0x04FB,"separate LG address");check((((frame>>>8)^frame)&255)==255,"command complement"); }
        check(LocalTvAddress.valid("192.168.1.20") && LocalTvAddress.valid("10.0.0.2") && LocalTvAddress.valid("172.31.2.3"),"private addresses accepted");
        for(String value:new String[]{"8.8.8.8","127.0.0.1","169.254.1.1","192.168.1.255","172.32.0.1","192.168.1.999","example.com","https://192.168.1.2","192.168.01.1","192.168.1.2:3001"}) check(!LocalTvAddress.valid(value),"invalid TV destination: "+value);
        MotionMath m=new MotionMath();
        check(Arrays.equals(m.step(0,0,0,.033),new int[]{0,0}),"stationary phone");
        check(Arrays.equals(m.step(.01,.01,0,.033),new int[]{0,0}),"gyro deadband");
        int[] right=m.step(0,-1,0,.033);check(right[0]>0 && right[1]==0,"horizontal gyro");m.reset();
        int[] down=m.step(-1,0,0,.033);check(down[1]>0 && down[0]==0,"vertical gyro");m.reset();
        int[] roll=m.step(-1,0,Math.PI/2,.033);check(roll[0]>0 && Math.abs(roll[1])<=1,"rotation compensation");m.reset();
        int[] fast=m.step(100,100,0,.05);check(Math.abs(fast[0])<=45 && Math.abs(fast[1])<=45,"bounded motion");
        check(Arrays.equals(m.step(1,1,0,2),new int[]{0,0}),"ignore stale sample");
        check(Arrays.equals(m.step(Double.NaN,0,0,.033),new int[]{0,0}),"invalid sensor sample");
        m.reset();int[] flat=m.step3D(0,0,-1,0,0,9.81,.033);check(flat[0]>0 && flat[1]==0,"flat phone uses gyro Z for yaw");
        m.reset();int[] upright=m.step3D(0,-1,0,0,9.81,0,.033);check(upright[0]>0 && upright[1]==0,"upright phone uses gyro Y for yaw");
        check(Arrays.equals(m.step3D(0,0,0,0,0,0,.033),new int[]{0,0}),"invalid gravity");
        System.out.println("PASS: LG IR commands, TV destination validation and gyroscope motion integration");
    }
}
