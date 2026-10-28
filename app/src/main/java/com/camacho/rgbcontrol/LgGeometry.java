package com.camacho.rgbcontrol;

/** Responsive icon-only TV controls, with a side layout for short/landscape windows. */
public final class LgGeometry {
    public final float x,y,width,height,cx,cy,radius;
    public final float[] centerX=new float[10],centerY=new float[10],keyWidth=new float[10],keyHeight=new float[10];
    public LgGeometry(int screenWidth,int screenHeight,int left,int top,int right,int bottom) {
        x=left;y=top;width=Math.max(1,screenWidth-left-right);height=Math.max(1,screenHeight-top-bottom);
        boolean landscape=width>height*1.2f;
        cx=x+width*.50f;cy=y+height*(landscape ? .50f:.47f);
        radius=landscape ? Math.min(width*.215f,height*.41f):Math.min(width*.43f,height*.23f);
        float power=landscape ? Math.min(width*.16f,height*.28f):Math.min(width*.32f,height*.18f);
        float volume=landscape ? Math.min(width*.16f,height*.26f):Math.min(width*.28f,height*.15f);
        float small=landscape ? Math.min(width*.13f,height*.18f):Math.min(width*.20f,height*.10f);
        key(0,x+width*(landscape ? .125f:.50f),y+height*(landscape ? .23f:.13f),power,power);
        key(5,cx,cy,radius*.80f,radius*.80f);
        key(1,cx,cy-radius*.72f,radius*.84f,radius*.48f);key(2,cx,cy+radius*.72f,radius*.84f,radius*.48f);
        key(3,cx-radius*.72f,cy,radius*.48f,radius*.84f);key(4,cx+radius*.72f,cy,radius*.48f,radius*.84f);
        key(6,x+width*(landscape ? .88f:.25f),y+height*(landscape ? .29f:.795f),volume,volume);
        key(7,x+width*(landscape ? .88f:.75f),y+height*(landscape ? .72f:.795f),volume,volume);
        key(8,x+width*(landscape ? .125f:.32f),y+height*(landscape ? .62f:.94f),small,small);
        key(9,x+width*(landscape ? .125f:.68f),y+height*(landscape ? .86f:.94f),small,small);
    }
    private void key(int i,float cx,float cy,float w,float h) { centerX[i]=cx;centerY[i]=cy;keyWidth[i]=w;keyHeight[i]=h; }
    public int left(int i) { return Math.round(centerX[i]-keyWidth[i]/2); }
    public int top(int i) { return Math.round(centerY[i]-keyHeight[i]/2); }
}
