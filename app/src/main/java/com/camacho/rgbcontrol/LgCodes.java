package com.camacho.rgbcontrol;

/** Common LG TV NEC commands (address 04, inverse FB), separate from the LED codes. */
public final class LgCodes {
    private LgCodes() {}
    public static final int POWER=0,UP=1,DOWN=2,LEFT=3,RIGHT=4,OK=5,VOLUME_UP=6,VOLUME_DOWN=7,MUTE=8;
    private static final int[] COMMANDS={0x08,0x40,0x41,0x07,0x06,0x44,0x02,0x03,0x09};
    public static final String[] LABELS={"Apagar o encender","Arriba","Abajo","Izquierda","Derecha","OK","Subir volumen","Bajar volumen","Silencio"};
    public static long frame(int action) {
        if(action<0 || action>=COMMANDS.length) throw new IllegalArgumentException("Botón LG inválido");
        return IrCodes.frame(4,0xFB,COMMANDS[action]);
    }
}
