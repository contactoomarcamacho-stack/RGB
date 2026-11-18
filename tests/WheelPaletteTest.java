import com.camacho.rgbcontrol.WheelPalette;
import java.util.HashSet;
import java.util.Set;

public final class WheelPaletteTest {
    private static void check(boolean ok,String text) { if(!ok) throw new AssertionError(text); }
    public static void main(String[] args) {
        check(WheelPalette.actionAt(0)==4,"right red");
        check(WheelPalette.actionAt(-90)==4,"top red");
        check(WheelPalette.actionAt(225)==5,"upper left green");
        check(WheelPalette.actionAt(315)==20,"upper right yellow");
        check(WheelPalette.actionAt(22.5f)==22,"red to magenta follows short hue arc");
        Set<Integer> actual=new HashSet<>(),expected=new HashSet<>();
        for(int action:WheelPalette.ACTIONS) expected.add(action);
        for(int step=-7200;step<=7200;step++) {
            float angle=step/10f; int action=WheelPalette.actionAt(angle);
            check(expected.contains(action),"only existing colour actions");
            check(action==WheelPalette.actionAt(angle+360),"angle wrapping");
            check(WheelPalette.hueAt(angle)>=0 && WheelPalette.hueAt(angle)<360,"hue range");
            actual.add(action);
        }
        check(actual.equals(expected),"all 15 colours reachable");
        System.out.println("PASS: wheel direction, hue wrapping, short hue arc and all 15 LED colours");
    }
}
