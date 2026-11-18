package com.camacho.rgbcontrol;

/** Gyroscope integration with deadband, bounded delta and subpixel retention. */
public final class MotionMath {
    private double remainderX,remainderY;
    public int[] step3D(double wx,double wy,double wz,double gx,double gy,double gz,double seconds) {
        double length=Math.sqrt(gx*gx+gy*gy+gz*gz);
        if(Double.isNaN(length) || Double.isInfinite(length) || length<1 || Double.isNaN(wz) || Double.isInfinite(wz)) { reset(); return new int[]{0,0}; }
        double roll=Math.hypot(gx,gy)>2 ? Math.atan2(gx,gy):0;
        double pitch=wx*Math.cos(roll)-wy*Math.sin(roll);
        double yaw=(wx*gx+wy*gy+wz*gz)/length;
        return step(pitch,yaw,0,seconds);
    }
    public void reset() { remainderX=0; remainderY=0; }
    public int[] step(double wx,double wy,double roll,double seconds) {
        if(Double.isNaN(wx) || Double.isInfinite(wx) || Double.isNaN(wy) || Double.isInfinite(wy) || Double.isNaN(roll) || Double.isInfinite(roll) || Double.isNaN(seconds) || Double.isInfinite(seconds) || seconds<=0 || seconds>.10) { reset(); return new int[]{0,0}; }
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
