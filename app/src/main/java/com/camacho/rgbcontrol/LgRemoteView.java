package com.camacho.rgbcontrol;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.Button;

/** Dark, cyan LG remote matching the user's first reference. */
public final class LgRemoteView extends ViewGroup {
    public interface Listener { void press(int action); void configurePointer(); }
    private final Button[] keys=new Button[9];
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler handler=new Handler(Looper.getMainLooper()); private Runnable repeat;
    private float x,y,w,h,cx,cy,r; private boolean motion;
    public LgRemoteView(Context context,Listener listener) {
        super(context); setWillNotDraw(false); setBackgroundColor(0xFF151D27);
        for(int i=0;i<9;i++) {
            final int action=i;
            Button b=new IconButton(context,i); keys[i]=b;
            b.setContentDescription(LgCodes.LABELS[i]); b.setBackgroundColor(Color.TRANSPARENT);
            b.setPadding(0,0,0,0); b.setMinWidth(0); b.setMinHeight(0); b.setMinimumWidth(0); b.setMinimumHeight(0); b.setStateListAnimator(null);
            b.setOnClickListener(v->listener.press(action));
            if(i>=1 && i<=4 || i==6 || i==7) b.setOnTouchListener((v,event)->{
                if(event.getActionMasked()==MotionEvent.ACTION_DOWN) {
                    stopInteraction(); listener.press(action);
                    repeat=new Runnable() { public void run() { listener.press(action); handler.postDelayed(this,180); } }; handler.postDelayed(repeat,400);
                    v.setPressed(true); return true;
                }
                if(event.getActionMasked()==MotionEvent.ACTION_UP) { stopInteraction(); v.setPressed(false); return true; }
                if(event.getActionMasked()==MotionEvent.ACTION_CANCEL) { stopInteraction(); v.setPressed(false); return true; }
                return true;
            });
            if(i==LgCodes.OK) b.setOnLongClickListener(v->{listener.configurePointer(); return true;});
            addView(b);
        }
    }
    public void setMotion(boolean enabled) { motion=enabled; invalidate(); }
    public void stopInteraction() { if(repeat!=null) handler.removeCallbacks(repeat); repeat=null; }
    @Override protected void onDetachedFromWindow() { stopInteraction(); super.onDetachedFromWindow(); }
    private void size(View view,float width,float height) {
        view.measure(MeasureSpec.makeMeasureSpec(Math.max(1,Math.round(width)),MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(Math.max(1,Math.round(height)),MeasureSpec.EXACTLY));
    }
    @Override protected void onMeasure(int widthSpec,int heightSpec) {
        int width=MeasureSpec.getSize(widthSpec),height=MeasureSpec.getSize(heightSpec); setMeasuredDimension(width,height);
        x=getPaddingLeft(); y=getPaddingTop(); w=Math.max(1,width-getPaddingLeft()-getPaddingRight()); h=Math.max(1,height-getPaddingTop()-getPaddingBottom());
        cx=x+w/2; cy=y+h*.51f; r=Math.min(w*.37f,h*.195f);
        float power=Math.min(w*.34f,h*.17f),vol=Math.min(w*.25f,h*.125f);
        size(keys[0],power,power); size(keys[5],r*.80f,r*.80f);
        size(keys[1],r*.84f,r*.48f); size(keys[2],r*.84f,r*.48f); size(keys[3],r*.48f,r*.84f); size(keys[4],r*.48f,r*.84f);
        size(keys[6],vol,vol); size(keys[7],vol,vol); size(keys[8],Math.min(w*.18f,h*.09f),Math.min(w*.18f,h*.09f));
    }
    private void center(View view,float x,float y) {
        int a=Math.round(x-view.getMeasuredWidth()/2f),b=Math.round(y-view.getMeasuredHeight()/2f); view.layout(a,b,a+view.getMeasuredWidth(),b+view.getMeasuredHeight());
    }
    @Override protected void onLayout(boolean changed,int left,int top,int right,int bottom) {
        center(keys[0],cx,y+h*.205f); center(keys[5],cx,cy);
        center(keys[1],cx,cy-r*.72f); center(keys[2],cx,cy+r*.72f); center(keys[3],cx-r*.72f,cy); center(keys[4],cx+r*.72f,cy);
        center(keys[6],x+w*.265f,y+h*.805f); center(keys[7],x+w*.735f,y+h*.805f); center(keys[8],cx,y+h*.94f);
    }
    private void ring(Canvas c,float x,float y,float radius) {
        p.setStyle(Paint.Style.STROKE);
        for(int i=6;i>=1;i--) { p.setColor(0x1034DDEB); p.setStrokeWidth(w*.005f+i*w*.010f); c.drawCircle(x,y,radius,p); }
        p.setShader(new LinearGradient(x-radius,y-radius,x+radius,y+radius,0xFF59FAE7,0xFF3298CE,Shader.TileMode.CLAMP));
        p.setStrokeWidth(Math.max(2,w*.006f)); c.drawCircle(x,y,radius,p); p.setShader(null); p.setStyle(Paint.Style.FILL);
    }
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        p.setShader(new RadialGradient(cx,cy,Math.max(w,h),new int[]{0xFF25303C,0xFF111720},null,Shader.TileMode.CLAMP)); c.drawRect(x,y,x+w,y+h,p); p.setShader(null);
        p.setTypeface(Typeface.create("sans-serif-condensed",Typeface.NORMAL)); p.setTextAlign(Paint.Align.CENTER); p.setColor(0xFFB4EEF0); p.setTextSize(Math.min(w*.047f,h*.026f));
        c.drawText("APAGAR / ENCENDER",cx,y+h*.085f,p);
        ring(c,keys[0].getX()+keys[0].getWidth()/2f,keys[0].getY()+keys[0].getHeight()/2f,keys[0].getWidth()*.48f);
        for(int sector=0;sector<4;sector++) {
            Path path=new Path(); float start=45+sector*90;
            path.arcTo(new RectF(cx-r,cy-r,cx+r,cy+r),start+1,88);
            path.arcTo(new RectF(cx-r*.43f,cy-r*.43f,cx+r*.43f,cy+r*.43f),start+89,-88); path.close();
            p.setShader(new RadialGradient(cx,cy,r,new int[]{0xFF121C28,0xFF263C4A,0xFF18232F},new float[]{0,.68f,1},Shader.TileMode.CLAMP));c.drawPath(path,p);p.setShader(null);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1,w*.003f));p.setColor(0xFF477D8C);c.drawPath(path,p);p.setStyle(Paint.Style.FILL);
        }
        ring(c,cx,cy,r);ring(c,cx,cy,r*.43f);
        for(int i=6;i<=7;i++) ring(c,keys[i].getX()+keys[i].getWidth()/2f,keys[i].getY()+keys[i].getHeight()/2f,keys[i].getWidth()*.48f);
        p.setColor(0xFFB4EEF0);p.setTextSize(Math.min(w*.044f,h*.024f));
        c.drawText("SUBIR",x+w*.265f,y+h*.692f,p);c.drawText("VOLUMEN",x+w*.265f,y+h*.721f,p);
        c.drawText("BAJAR",x+w*.735f,y+h*.692f,p);c.drawText("VOLUMEN",x+w*.735f,y+h*.721f,p);
        if(motion) { p.setColor(0xFF5CFFE6); c.drawCircle(x+w*.94f,y+h*.045f,w*.010f,p); }
    }
    private static final class IconButton extends Button {
        private final int action; private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        IconButton(Context context,int action) { super(context);this.action=action; }
        @Override protected void onDraw(Canvas c) {
            float w=getWidth(),h=getHeight(),s=Math.min(w,h),cx=w/2,cy=h/2;
            p.setColor(isPressed()?Color.WHITE:0xFF59E4E4);p.setStrokeWidth(s*.07f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setStyle(Paint.Style.STROKE);
            if(action==0) { c.drawArc(new RectF(cx-s*.23f,cy-s*.23f,cx+s*.23f,cy+s*.23f),-48,276,false,p);c.drawLine(cx,cy-s*.31f,cx,cy-s*.02f,p); }
            else if(action>=1 && action<=4) {
                c.save();c.translate(cx,cy);if(action==2)c.rotate(180);else if(action==3)c.rotate(-90);else if(action==4)c.rotate(90);
                Path path=new Path();path.moveTo(-s*.18f,s*.08f);path.lineTo(0,-s*.10f);path.lineTo(s*.18f,s*.08f);c.drawPath(path,p);c.restore();
            } else if(action==5) { p.setStyle(Paint.Style.FILL);p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));p.setTextSize(s*.43f);p.setTextAlign(Paint.Align.CENTER);c.drawText("OK",cx,cy-(p.ascent()+p.descent())/2,p); }
            else {
                p.setStyle(Paint.Style.FILL);Path speaker=new Path();speaker.moveTo(cx-s*.31f,cy-s*.10f);speaker.lineTo(cx-s*.23f,cy-s*.10f);speaker.lineTo(cx-s*.08f,cy-s*.23f);speaker.lineTo(cx-s*.08f,cy+s*.23f);speaker.lineTo(cx-s*.23f,cy+s*.10f);speaker.lineTo(cx-s*.31f,cy+s*.10f);speaker.close();c.drawPath(speaker,p);
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(s*.055f);
                if(action==8) { c.drawLine(cx+s*.07f,cy-s*.09f,cx+s*.24f,cy+s*.09f,p);c.drawLine(cx+s*.24f,cy-s*.09f,cx+s*.07f,cy+s*.09f,p); }
                else {c.drawLine(cx+s*.03f,cy,cx+s*.29f,cy,p);if(action==6)c.drawLine(cx+s*.16f,cy-s*.13f,cx+s*.16f,cy+s*.13f,p);}
            }
        }
    }
}
