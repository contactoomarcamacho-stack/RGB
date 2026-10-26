package com.camacho.rgbcontrol;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;

/** Native, accessible buttons laid out like the photographed 24-key remote. */
public final class RemoteView extends ViewGroup {
    public interface Listener { void onPress(int action); }
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF body = new RectF();
    private final Button[] keys = new Button[24];
    private RemoteGeometry geometry;
    private static final String[] CAPTIONS = {
        "", "", "OFF", "ON", "R", "G", "B", "W",
        "", "", "", "FLASH", "", "", "", "STROBE",
        "", "", "", "FADE", "", "", "", "SMOOTH"
    };
    private static final int[] COLORS = {
        0xFFF7F7F3,0xFFF7F7F3,0xFF22252B,0xFFE32A36,
        0xFFE42C37,0xFF00A85E,0xFF153BD9,0xFFF8F8F5,
        0xFFEE423E,0xFF009C7B,0xFF0069EF,0xFF888C96,
        0xFFFA6540,0xFF00BADE,0xFF6926B9,0xFF888C96,
        0xFFFF963A,0xFF009FE5,0xFFAA38B8,0xFF888C96,
        0xFFF4DC47,0xFF0080D7,0xFFDB31B4,0xFF888C96
    };
    public RemoteView(Context context,Listener listener) {
        super(context);
        setWillNotDraw(false);
        setBackgroundColor(0xFFF7F7F4);
        setClipToPadding(false);
        for (int i=0;i<24;i++) {
            final int action=i;
            Button key = i<2 ? new SunButton(context,i==0) : new Button(context);
            key.setText(CAPTIONS[i]); key.setAllCaps(false); key.setGravity(Gravity.CENTER);
            key.setTypeface(Typeface.create("sans-serif-condensed",Typeface.NORMAL));
            key.setTextColor(i==2 || i==3 ? Color.WHITE : 0xFF12141B);
            key.setContentDescription(IrCodes.LABELS[i]);
            key.setPadding(0,0,0,0); key.setMinWidth(0); key.setMinHeight(0);
            key.setMinimumWidth(0); key.setMinimumHeight(0);
            key.setStateListAnimator(null);
            StateListDrawable background = new StateListDrawable();
            background.addState(new int[]{android.R.attr.state_pressed},disc(shade(COLORS[i],0.72f)));
            background.addState(new int[]{},disc(COLORS[i]));
            key.setBackground(background);
            key.setOnClickListener(v->listener.onPress(action));
            keys[i]=key; addView(key);
        }
    }
    private static int shade(int color,float amount) {
        return Color.rgb(Math.min(255,Math.round(Color.red(color)*amount)),
            Math.min(255,Math.round(Color.green(color)*amount)),Math.min(255,Math.round(Color.blue(color)*amount)));
    }
    private GradientDrawable disc(int color) {
        int highlight=Color.rgb((Color.red(color)*4+255)/5,(Color.green(color)*4+255)/5,(Color.blue(color)*4+255)/5);
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{highlight,color,shade(color,0.83f)});
        d.setShape(GradientDrawable.OVAL); d.setStroke(1,shade(color,0.75f)); return d;
    }
    @Override protected void onMeasure(int widthSpec,int heightSpec) {
        int width=MeasureSpec.getSize(widthSpec), height=MeasureSpec.getSize(heightSpec);
        setMeasuredDimension(width,height);
        geometry=new RemoteGeometry(width,height,getPaddingLeft(),getPaddingTop(),getPaddingRight(),getPaddingBottom());
        body.set(geometry.left,geometry.top,geometry.left+geometry.width,geometry.top+geometry.height);
        int size=geometry.keySize;
        for (int i=0;i<24;i++) {
            keys[i].setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,size*(i%4==3 && i>=8 ? 0.23f : 0.38f));
            keys[i].measure(MeasureSpec.makeMeasureSpec(size,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(size,MeasureSpec.EXACTLY));
        }
    }
    @Override protected void onLayout(boolean changed,int left,int top,int right,int bottom) {
        for(int i=0;i<24;i++) {
            int x=geometry.keyLeft(i),y=geometry.keyTop(i);
            keys[i].layout(x,y,x+keys[i].getMeasuredWidth(),y+keys[i].getMeasuredHeight());
        }
    }
    private void round(Canvas canvas,float l,float t,float r,float b,float radius,int color) {
        paint.setColor(color); canvas.drawRoundRect(new RectF(l,t,r,b),radius,radius,paint);
    }
    private void panel(Canvas canvas,int col,int row,int columns,int rows) {
        float gap=Math.min(geometry.cellWidth,geometry.cellHeight)*0.035f;
        float x=geometry.gridLeft+col*geometry.cellWidth,y=geometry.gridTop+row*geometry.cellHeight;
        round(canvas,x+gap,y+gap,x+columns*geometry.cellWidth-gap,y+rows*geometry.cellHeight-gap,
            Math.min(geometry.cellWidth,geometry.cellHeight)*0.12f,0xFF9699A1);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (geometry==null) return;
        float x=body.left,y=body.top,w=body.width(),h=body.height();
        float unit=Math.min(w,h);
        round(canvas,x,y,x+w,y+h,unit*0.035f,0xFF24252A);
        round(canvas,x+unit*0.004f,y+unit*0.004f,x+w-unit*0.004f,y+h-unit*0.004f,unit*0.031f,0xFFC2C4C9);
        round(canvas,x+unit*0.010f,y+unit*0.010f,x+w-unit*0.010f,y+h-unit*0.010f,unit*0.026f,0xFFF7F7F4);
        panel(canvas,0,0,2,1);
        panel(canvas,2,0,2,1);
        panel(canvas,0,1,3,5);
        panel(canvas,3,1,1,1);
    }

    private static final class SunButton extends Button {
        private final Paint icon = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final boolean up;
        SunButton(Context context,boolean up) { super(context); this.up=up; }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float size=Math.min(getWidth(),getHeight()), cx=getWidth()/2f, cy=getHeight()*0.43f;
            icon.setColor(0xFF343842); icon.setStyle(Paint.Style.STROKE); icon.setStrokeWidth(size*0.027f);
            canvas.drawCircle(cx,cy,size*0.17f,icon);
            for(int i=0;i<8;i++) {
                double angle=i*Math.PI/4;
                canvas.drawLine(cx+(float)Math.cos(angle)*size*0.21f,cy+(float)Math.sin(angle)*size*0.21f,
                    cx+(float)Math.cos(angle)*size*0.28f,cy+(float)Math.sin(angle)*size*0.28f,icon);
            }
            icon.setStyle(Paint.Style.FILL); icon.setTypeface(Typeface.DEFAULT_BOLD);
            icon.setTextSize(size*0.22f); icon.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(up ? "+" : "−",cx,getHeight()*0.87f,icon);
        }
    }
}
