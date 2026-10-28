package com.camacho.rgbcontrol;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.Button;

/** Single-screen wheel remote based on the user's reference image. */
public final class WheelRemoteView extends ViewGroup {
    public interface Listener { void onPress(int action); }
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Button[] header=new Button[4],effects=new Button[4];
    private final Button white,tv;
    private final ColourWheel wheel;
    private float x,y,w,h,headerHeight,footerTop,footerHeight;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private Runnable repeat;
    private final Listener listener;
    public WheelRemoteView(Context context,Listener listener) {
        super(context); this.listener=listener; setWillNotDraw(false); setBackgroundColor(0xFF111722);
        header[0]=new SunButton(context,false); header[3]=new SunButton(context,true);
        header[1]=button("OFF",0xFF3A3B3D,Color.WHITE,0xFF3A3B3D,false);
        header[2]=button("ON",0xFFF00025,Color.WHITE,0xFFFB1735,false);
        header[0].setContentDescription("Bajar brillo"); header[3].setContentDescription("Subir brillo");
        addView(header[0]); addView(header[3]);
        header[1].setOnClickListener(v->listener.onPress(2)); header[2].setOnClickListener(v->listener.onPress(3));
        hold(header[0],1); hold(header[3],0);
        String[] labels={"ϟ FLASH","STROBE","FADE","SMOOTH"}; int[] actions={11,15,19,23};
        for(int i=0;i<4;i++) {
            final int action=actions[i]; effects[i]=button(labels[i],0xFF111722,Color.WHITE,i%2==0 ? 0xFF7ABDEA:0xFFB0A9D0,false);
            effects[i].setOnClickListener(v->listener.onPress(action));
        }
        white=button("WHITE",Color.WHITE,0xFF141820,Color.WHITE,true);
        white.setContentDescription("Luz blanca"); white.setOnClickListener(v->listener.onPress(7));
        tv=button("TV",Color.WHITE,0xFF141820,Color.WHITE,true);
        tv.setContentDescription("Abrir control del televisor LG");
        tv.setOnClickListener(v->{ stopInteraction(); getContext().startActivity(new android.content.Intent(getContext(),LgActivity.class)); });
        wheel=new ColourWheel(context,listener); addView(wheel);
    }
    private Button button(String label,int fill,int text,int border,boolean oval) {
        Button b=new Button(getContext()); b.setText(label); b.setAllCaps(false); b.setTextColor(text);
        b.setTypeface(Typeface.create("sans-serif-condensed",Typeface.NORMAL));
        b.setGravity(Gravity.CENTER); b.setPadding(0,0,0,0); b.setMinWidth(0); b.setMinHeight(0);
        b.setMinimumWidth(0); b.setMinimumHeight(0); b.setStateListAnimator(null);
        StateListDrawable states=new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed},background(oval ? 0xFFD5E9FA:0xFF30435B,border,oval));
        states.addState(new int[]{},background(fill,border,oval)); b.setBackground(states); addView(b); return b;
    }
    private GradientDrawable background(int fill,int border,boolean oval) {
        GradientDrawable d=new GradientDrawable(); d.setColor(fill);
        if(oval) d.setShape(GradientDrawable.OVAL); else d.setCornerRadius(dp(21));
        d.setStroke(dp(1),border); return d;
    }
    private int dp(int n) { return Math.round(n*getResources().getDisplayMetrics().density); }
    private void hold(Button button,int action) {
        button.setOnClickListener(v->listener.onPress(action));
        button.setOnTouchListener((v,event)->{
            if(event.getActionMasked()==MotionEvent.ACTION_DOWN) {
                stopRepeat(); repeat=new Runnable() { public void run() { listener.onPress(action); handler.postDelayed(this,200); } };
                handler.postDelayed(repeat,350);
            } else if(event.getActionMasked()==MotionEvent.ACTION_UP || event.getActionMasked()==MotionEvent.ACTION_CANCEL) stopRepeat();
            return false;
        });
    }
    private void stopRepeat() { if(repeat!=null) handler.removeCallbacks(repeat); repeat=null; }
    public void stopInteraction() { stopRepeat(); wheel.stopInteraction(); }
    @Override protected void onDetachedFromWindow() { stopInteraction(); super.onDetachedFromWindow(); }
    @Override protected void onMeasure(int widthSpec,int heightSpec) {
        int width=MeasureSpec.getSize(widthSpec),height=MeasureSpec.getSize(heightSpec); setMeasuredDimension(width,height);
        x=getPaddingLeft(); y=getPaddingTop(); w=Math.max(1,width-getPaddingLeft()-getPaddingRight()); h=Math.max(1,height-getPaddingTop()-getPaddingBottom());
        headerHeight=h*0.17f; footerHeight=h*0.28f; footerTop=y+h-footerHeight;
        for(int i=0;i<4;i++) measure(header[i],w*.22f,headerHeight*.70f,w*.068f);
        for(Button b:effects) measure(b,w*.285f,footerHeight*.32f,Math.min(w*.050f,footerHeight*.13f));
        float circle=Math.min(w*.225f,footerHeight*.38f);
        measure(white,circle,circle,circle*.23f); measure(tv,circle,circle,circle*.35f);
        float diameter=Math.min(w*.95f,(h-headerHeight-footerHeight)*.95f);
        measure(wheel,diameter,diameter,0);
    }
    private void measure(View v,float width,float height,float textSize) {
        if(v instanceof Button) ((Button)v).setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,textSize);
        v.measure(MeasureSpec.makeMeasureSpec(Math.max(1,Math.round(width)),MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(Math.max(1,Math.round(height)),MeasureSpec.EXACTLY));
    }
    private void place(View v,float left,float top) { int a=Math.round(left),b=Math.round(top); v.layout(a,b,a+v.getMeasuredWidth(),b+v.getMeasuredHeight()); }
    @Override protected void onLayout(boolean changed,int l,int t,int r,int b) {
        for(int i=0;i<4;i++) place(header[i],x+w*(.015f+i*.25f),y+(headerHeight-header[i].getMeasuredHeight())/2);
        place(wheel,x+(w-wheel.getMeasuredWidth())/2,y+headerHeight+(h-headerHeight-footerHeight-wheel.getMeasuredHeight())/2);
        for(int i=0;i<4;i++) place(effects[i],x+w*(.045f+(i%2)*.335f),footerTop+footerHeight*(.12f+(i/2)*.43f));
        place(white,x+w*.86f-white.getMeasuredWidth()/2f,footerTop+footerHeight*.10f);
        place(tv,x+w*.86f-tv.getMeasuredWidth()/2f,footerTop+footerHeight*.57f);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas); paint.setColor(0xFF707A8B); paint.setStrokeWidth(Math.max(1,w*.003f));
        canvas.drawLine(x,y+headerHeight,x+w,y+headerHeight,paint);
        canvas.drawLine(x,footerTop,x+w,footerTop,paint);
        canvas.drawLine(x+w*.72f,footerTop,x+w*.72f,y+h,paint);
    }
    private static final class SunButton extends Button {
        private final Paint icon=new Paint(Paint.ANTI_ALIAS_FLAG); private final boolean bright;
        SunButton(Context context,boolean bright) {
            super(context); this.bright=bright; setBackgroundColor(Color.TRANSPARENT); setPadding(0,0,0,0); setMinimumWidth(0); setMinimumHeight(0); setStateListAnimator(null);
        }
        @Override protected void onDraw(Canvas canvas) {
            float s=Math.min(getWidth(),getHeight()),cx=getWidth()/2f,cy=getHeight()/2f;
            icon.setColor(isPressed()?Color.WHITE:bright ? 0xFF929AA6:0xFFF0F1F5); icon.setStyle(Paint.Style.STROKE); icon.setStrokeWidth(s*.035f);
            canvas.drawCircle(cx,cy,s*.17f,icon);
            for(int i=0;i<8;i++) { double a=i*Math.PI/4; canvas.drawLine(cx+(float)Math.cos(a)*s*.27f,cy+(float)Math.sin(a)*s*.27f,cx+(float)Math.cos(a)*s*.38f,cy+(float)Math.sin(a)*s*.38f,icon); }
            if(!bright) { icon.setStyle(Paint.Style.FILL); canvas.drawArc(new RectF(cx-s*.17f,cy-s*.17f,cx+s*.17f,cy+s*.17f),90,180,true,icon); }
        }
    }
    private static final class ColourWheel extends View {
        private final Paint ring=new Paint(Paint.ANTI_ALIAS_FLAG),marker=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Listener listener; private float selected=-90; private int last=-1; private boolean tracking;
        private float cx,cy,radius,stroke; private SweepGradient gradient;
        ColourWheel(Context context,Listener listener) { super(context); this.listener=listener; setClickable(true); setFocusable(true); setContentDescription("Rueda de colores. Deslizá el dedo para cambiar la luz"); }
        @Override protected void onSizeChanged(int w,int h,int oldw,int oldh) {
            cx=w/2f; cy=h/2f; float size=Math.min(w,h); radius=size*.405f; stroke=size*.175f;
            int[] colors=new int[WheelPalette.STOPS.length]; for(int i=0;i<colors.length;i++) colors[i]=Color.HSVToColor(new float[]{WheelPalette.STOPS[i],1,1});
            gradient=new SweepGradient(cx,cy,colors,null);
        }
        @Override protected void onDraw(Canvas canvas) {
            ring.setStyle(Paint.Style.STROKE); ring.setShader(gradient);
            for(int i=4;i>=1;i--) { ring.setStrokeWidth(stroke+i*getWidth()*.007f); ring.setAlpha(18); canvas.drawCircle(cx,cy,radius,ring); }
            ring.setStrokeWidth(stroke); ring.setAlpha(255); canvas.drawCircle(cx,cy,radius,ring); ring.setShader(null);
            if(last>=0) {
                double angle=Math.toRadians(selected); float px=cx+(float)Math.cos(angle)*radius,py=cy+(float)Math.sin(angle)*radius;
                marker.setStyle(Paint.Style.STROKE); marker.setStrokeWidth(Math.max(2,getWidth()*.009f)); marker.setColor(0xFF171C27); canvas.drawCircle(px,py,stroke*.30f,marker);
                marker.setStrokeWidth(Math.max(1,getWidth()*.006f)); marker.setColor(Color.WHITE); canvas.drawCircle(px,py,stroke*.30f,marker);
            }
        }
        @Override public boolean onTouchEvent(MotionEvent event) {
            float dx=event.getX()-cx,dy=event.getY()-cy,distance=(float)Math.hypot(dx,dy);
            switch(event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if(Math.abs(distance-radius)>stroke*.68f) return false;
                    tracking=true; last=-1; getParent().requestDisallowInterceptTouchEvent(true); update(dx,dy); return true;
                case MotionEvent.ACTION_MOVE: if(tracking && distance>radius*.45f) update(dx,dy); return tracking;
                case MotionEvent.ACTION_UP:
                    if(!tracking) return false;
                    if(distance>radius*.45f) update(dx,dy);
                    tracking=false; performClick(); getParent().requestDisallowInterceptTouchEvent(false); return true;
                case MotionEvent.ACTION_CANCEL: stopInteraction(); return true;
                default: return tracking;
            }
        }
        private void update(float dx,float dy) {
            selected=(float)Math.toDegrees(Math.atan2(dy,dx)); int action=WheelPalette.actionAt(selected);
            if(action!=last) { last=action; listener.onPress(action); setContentDescription("Color seleccionado: "+IrCodes.LABELS[action]); }
            invalidate();
        }
        void stopInteraction() { tracking=false; }
        @Override public boolean performClick() { super.performClick(); if(last>=0) listener.onPress(last); return true; }
        @Override public boolean onKeyDown(int code,android.view.KeyEvent event) {
            if(code==android.view.KeyEvent.KEYCODE_DPAD_LEFT || code==android.view.KeyEvent.KEYCODE_DPAD_RIGHT) {
                selected+=code==android.view.KeyEvent.KEYCODE_DPAD_RIGHT ? 12:-12; int action=WheelPalette.actionAt(selected); last=action; listener.onPress(action); invalidate(); return true;
            }
            return super.onKeyDown(code,event);
        }
    }
}
