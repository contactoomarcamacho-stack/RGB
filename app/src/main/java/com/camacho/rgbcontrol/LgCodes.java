package com.camacho.rgbcontrol;

/** Common LG TV NEC commands (address 04, inverse FB), separate from the LED codes. */
public final class LgCodes {
    private LgCodes() {}
    public static final int POWER=0,UP=1,DOWN=2,LEFT=3,RIGHT=4,OK=5,VOLUME_UP=6,VOLUME_DOWN=7,MUTE=8;
    public static final int INPUT_MENU=9,HOME=10,BACK=11,YOUTUBE_ADFREE=12,HDMI_1=13,HDMI_2=14,HDMI_3=15;
    // Codes on LG NEC address 04 FB. YouTube is a network action, never an IR code.
    private static final int[] COMMANDS={0x08,0x40,0x41,0x07,0x06,0x44,0x02,0x03,0x09,0x0B,0x7C,0x28,-1,0xCE,0x91,0xD0};
    public static final String[] LABELS={"Apagar o encender","Arriba","Abajo","Izquierda","Derecha","OK","Subir volumen","Bajar volumen","Silencio","Entradas HDMI","Aplicaciones de la TV","Volver","YouTube AdFree","HDMI 1","HDMI 2","HDMI 3"};
    public static long frame(int action) {
        if(action<0 || action>=COMMANDS.length || COMMANDS[action]<0) throw new IllegalArgumentException("Botón LG inválido");
        return IrCodes.frame(4,0xFB,COMMANDS[action]);
    }
}
