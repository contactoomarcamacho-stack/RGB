import com.camacho.rgbcontrol.IrCodes;
import java.util.Arrays;

public final class IrCodesTest {
    private static void check(boolean value,String message) {
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        // Known MSB display code for ON and the equivalent wire-order LSB bytes.
        check(Arrays.equals(IrCodes.pattern(0x00F7C03FL,true),IrCodes.pattern(0x00EF03FCL,false)),"MSB/LSB equivalence");
        check(IrCodes.profile(2,3)==0x00EF03FCL,"F7 ON");
        check(IrCodes.profile(0,3)==0x00FF02FDL,"V1 compatibility");
        check(IrCodes.profile(1,3)==0x00FF40BFL,"reversed command");
        for(int p=0;p<IrCodes.PROFILES.length;p++) for(int a=0;a<24;a++) {
            long frame=IrCodes.profile(p,a);
            check((((frame >>> 8)^frame)&255)==255,"command complement");
        }
        for(int cmd=0;cmd<256;cmd++) {
            long frame=IrCodes.frame(0,255,cmd);
            int[] pulse=IrCodes.pattern(frame,false);
            check(pulse.length==67 && pulse[0]==9000 && pulse[1]==4500 && pulse[66]==560,"NEC framing");
            long decoded=0;
            for(int b=0;b<4;b++) {
                int value=0;
                for(int bit=0;bit<8;bit++) {
                    check(pulse[2+(b*8+bit)*2]==560,"mark duration");
                    int space=pulse[3+(b*8+bit)*2];
                    check(space==560 || space==1690,"space duration");
                    if(space==1690) value|=1<<bit;
                }
                decoded=(decoded<<8)|value;
            }
            check(decoded==frame,"decoded bytes for command "+cmd);
            int duration=0; for(int n:pulse) duration+=n;
            check(duration<2000000,"Android pattern duration");
        }
        check(IrCodes.parse(" 0x00f7c03f ")==0x00F7C03FL,"hex parsing");
        for(String bad:new String[]{"", "123", "100000000", "ZZZZZZZZ", "-0000001", "00 FF 02 FD"}) {
            boolean rejected=false;
            try { IrCodes.parse(bad); } catch(IllegalArgumentException ex) { rejected=true; }
            check(rejected,"invalid hex: "+bad);
        }
        System.out.println("PASS: NEC framing, all 256 commands, profiles, MSB/LSB equivalence and input validation");
    }
}
