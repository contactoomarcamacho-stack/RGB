package com.camacho.rgbcontrol;

/** Map the visible wheel to the 15 colour commands supported by the existing LED remote. */
public final class WheelPalette {
    private WheelPalette() {}
    public static final int[] ACTIONS={4,8,12,16,20,5,9,13,17,21,10,6,14,18,22};
    public static final float[] HUES={0,10,20,35,55,120,150,175,190,210,225,240,270,300,325};
    public static final float[] STOPS={0,300,260,220,170,120,0,60,0};
    public static float normalize(float degrees) { return (degrees%360+360)%360; }
    public static float hueAt(float angle) {
        float position=normalize(angle)/45f;
        int i=(int)position;
        float delta=(STOPS[i+1]-STOPS[i]+540)%360-180;
        return normalize(STOPS[i]+delta*(position-i));
    }
    public static int actionAt(float angle) {
        float hue=hueAt(angle),best=Float.MAX_VALUE; int action=4;
        for(int i=0;i<HUES.length;i++) {
            float distance=Math.abs(hue-HUES[i]); distance=Math.min(distance,360-distance);
            if(distance<best) { best=distance; action=ACTIONS[i]; }
        }
        return action;
    }
}
