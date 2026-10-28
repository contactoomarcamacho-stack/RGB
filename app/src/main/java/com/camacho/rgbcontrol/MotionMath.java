package com.camacho.rgbcontrol;

/** Gyroscope integration with deadband, bounded delta and subpixel retention. */
public final class MotionMath {
    private double remainderX,remainderY;
    public void reset() { remainderX=0; remainderY=0; }
    public int[] step(double wx,double wy,double roll,double seconds) {
        if(Double.isNaN(wx) || Double.isInfinite(wx) || Double.isNaN(wy) || Double.isInfinite(wy) || Double.isNaN(roll) || Double.isInfinite(roll) || seconds<=0 || seconds>.10) { reset(); return new int[]{0,0}; }
        double horizontal=-(wy*Math.cos(roll)+wx*Math.sin(roll));
        double vertical=-(wx*Math.cos(roll)-wy*Math.sin(roll));
        if(Math.abs(horizontal)<.025) horizontal=0;
        if(Math.abs(vertical)<.025) vertical=0;
        remainderX+=Math.max(-45,Math.min(45,horizontal*seconds*900));
        remainderY+=Math.max(-45,Math.min(45,vertical*seconds*900));
        int x=(int)remainderX,y=(int)remainderY; remainderX-=x; remainderY-=y;
        return new int[]{x,y};
    }
}
