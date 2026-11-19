package com.camacho.rgbcontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Large premium source cards instead of an unstyled Android list. */
public final class LgSourcesDialog {
    public interface Listener {
        void selectHdmi(int number);
        void showTvInputPicker();
        void connectTv();
    }
    private static final int BG=0xFF101821, CARD=0xFF1A2936, TEXT=0xFFF5F8FA, MUTED=0xFF93ACBD;
    private LgSourcesDialog() {}
    private static int dp(Activity a,float d) { return Math.round(a.getResources().getDisplayMetrics().density*d); }
    private static GradientDrawable background(int fill,int stroke,int radius) {
        GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(radius);
        if(stroke!=0)d.setStroke(1,stroke);return d;
    }
    private static TextView label(Activity a,String text,int size,int color,boolean bold) {
        TextView v=new TextView(a);v.setText(text);v.setTextSize(size);v.setTextColor(color);
        if(bold)v.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));
        return v;
    }
    public static void show(Activity a,Listener listener) {
        final AlertDialog dialog=new AlertDialog.Builder(a).create();
        ScrollView scroll=new ScrollView(a);scroll.setFillViewport(false);
        LinearLayout root=new LinearLayout(a);root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(a,18),dp(a,20),dp(a,18),dp(a,20));root.setBackground(background(BG,0,dp(a,22)));
        scroll.addView(root);
        TextView eyebrow=label(a,"SELECTOR DE ENTRADAS",12,0xFF55D8E9,true);
        eyebrow.setLetterSpacing(.16f);root.addView(eyebrow);
        TextView title=label(a,"Mis dispositivos",26,TEXT,true);
        title.setPadding(0,dp(a,5),0,dp(a,4));root.addView(title);
        TextView subtitle=label(a,"Elegí qué querés ver en tu TV LG",13,MUTED,false);
        subtitle.setPadding(0,0,0,dp(a,18));root.addView(subtitle);
        addCard(a,root,1,"TV BOX","HDMI 1  ·  Reproductor",0xFF36DDCB,0,dialog,listener);
        addCard(a,root,2,"HDMI 2","Entrada disponible",0xFF63A7F0,1,dialog,listener);
        addCard(a,root,3,"PLAY 4","HDMI 3  ·  Consola",0xFF8C91FF,2,dialog,listener);
        TextView info=label(a,"Si la entrada no cambia, abrí el selector de la TV y elegila con las flechas.",12,MUTED,false);
        info.setPadding(dp(a,2),dp(a,9),dp(a,2),dp(a,12));root.addView(info);
        LinearLayout extras=new LinearLayout(a);extras.setOrientation(LinearLayout.HORIZONTAL);
        addAux(a,extras,"TODAS LAS ENTRADAS",0xFF203344,()->{dialog.dismiss();listener.showTvInputPicker();});
        addAux(a,extras,"CONECTAR TV",0xFF203344,()->{dialog.dismiss();listener.connectTv();});
        root.addView(extras);
        dialog.setView(scroll);
        dialog.setOnShowListener(d->{
            Window w=dialog.getWindow();
            if(w!=null) {w.setBackgroundDrawableResource(android.R.color.transparent);
                w.setLayout(Math.min(a.getResources().getDisplayMetrics().widthPixels-dp(a,28),dp(a,500)),-2);}
        });
        dialog.show();
    }
    private static void addAux(Activity a,LinearLayout row,String name,int bg,Runnable click) {
        TextView b=label(a,name,10,0xFFCBE8F2,true);b.setGravity(Gravity.CENTER);
        b.setBackground(background(bg,0xFF3C5869,dp(a,11)));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(a,47),1);
        lp.setMargins(dp(a,3),0,dp(a,3),0);row.addView(b,lp);
        b.setOnClickListener(v->click.run());
    }
    private static void addCard(Activity a,LinearLayout parent,int hdmi,String name,String hint,int accent,int type,AlertDialog dialog,Listener listener) {
        LinearLayout card=new LinearLayout(a);card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(a,12),dp(a,12),dp(a,12),dp(a,12));
        card.setBackground(background(CARD,0xFF345063,dp(a,17)));card.setClickable(true);
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(a,88));
        cp.setMargins(0,0,0,dp(a,10));parent.addView(card,cp);
        DeviceIcon icon=new DeviceIcon(a,type,accent);
        LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(a,66),dp(a,60));
        ip.setMargins(0,0,dp(a,13),0);card.addView(icon,ip);
        LinearLayout texts=new LinearLayout(a);texts.setOrientation(LinearLayout.VERTICAL);texts.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(texts,new LinearLayout.LayoutParams(0,-1,1));
        TextView label=label(a,name,20,TEXT,true);texts.addView(label);
        TextView sub=label(a,hint,12,MUTED,false);sub.setPadding(0,dp(a,3),0,0);texts.addView(sub);
        TextView chevron=label(a,"›",30,accent,true);card.addView(chevron);
        card.setContentDescription(name+", HDMI "+hdmi);
        card.setOnClickListener(v->{dialog.dismiss();listener.selectHdmi(hdmi);});
    }
    private static final class DeviceIcon extends View {
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private final int type,accent;
        DeviceIcon(Activity a,int t,int c){super(a);type=t;accent=c;}
        @Override protected void onDraw(Canvas c){
            float w=getWidth(),h=getHeight(),cx=w/2,cy=h/2,s=Math.min(w,h);
            p.setColor(0xFF11212E);p.setStyle(Paint.Style.FILL);
            c.drawRoundRect(new RectF(0,0,w,h),s*.2f,s*.2f,p);
            p.setColor(accent);p.setStrokeWidth(s*.055f);p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);p.setStyle(Paint.Style.STROKE);
            if(type==0){ // TV box with media play symbol
                c.drawRoundRect(new RectF(w*.17f,h*.25f,w*.83f,h*.66f),s*.09f,s*.09f,p);
                p.setStyle(Paint.Style.FILL);Path play=new Path();
                play.moveTo(w*.44f,h*.35f);play.lineTo(w*.61f,h*.455f);
                play.lineTo(w*.44f,h*.56f);play.close();c.drawPath(play,p);
                c.drawCircle(w*.73f,h*.57f,s*.023f,p);
            } else if(type==1){ // HDMI socket
                Path port=new Path();port.moveTo(w*.22f,h*.35f);port.lineTo(w*.78f,h*.35f);
                port.lineTo(w*.71f,h*.65f);port.lineTo(w*.29f,h*.65f);port.close();c.drawPath(port,p);
                c.drawLine(w*.39f,h*.45f,w*.39f,h*.56f,p);
                c.drawLine(w*.5f,h*.45f,w*.5f,h*.56f,p);
                c.drawLine(w*.61f,h*.45f,w*.61f,h*.56f,p);
            } else { // stylized games controller, no external assets
                Path pad=new Path();pad.moveTo(w*.24f,h*.56f);pad.quadTo(w*.18f,h*.26f,w*.39f,h*.33f);
                pad.lineTo(w*.61f,h*.33f);pad.quadTo(w*.82f,h*.26f,w*.76f,h*.56f);
                pad.quadTo(w*.73f,h*.76f,w*.6f,h*.58f);pad.lineTo(w*.4f,h*.58f);
                pad.quadTo(w*.27f,h*.76f,w*.24f,h*.56f);pad.close();c.drawPath(pad,p);
                c.drawLine(w*.32f,h*.46f,w*.45f,h*.46f,p);
                c.drawLine(w*.385f,h*.395f,w*.385f,h*.525f,p);
                p.setStyle(Paint.Style.FILL);c.drawCircle(w*.64f,h*.43f,s*.035f,p);
                c.drawCircle(w*.71f,h*.49f,s*.035f,p);
            }
            p.setStyle(Paint.Style.FILL);
        }
    }
}
