package com.camacho.rgbcontrol;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private ConsumerIrManager ir;
    private TextView status;
    // Common 24-key RGB remote NEC codes. These are test candidates, not guaranteed for every lamp.
    // NEC address 0x00FF, command bytes mapped by common 24-key RGB IR controllers.
    private static final int[][] CODES = {
        {0x3A,0xBA,0x82,0x02}, // brightness up, down, off, on
        {0x1A,0x9A,0xA2,0x22}, // red, green, blue, white
        {0x2A,0xAA,0x92,0x12},
        {0x0A,0x8A,0xB2,0x32},
        {0x38,0xB8,0x78,0xF8},
        {0x18,0x98,0x58,0xD8}
    };
    private static final String[][] LABELS = {
        {"☀ +", "☀ −", "OFF", "ON"},
        {"ROJO", "VERDE", "AZUL", "BLANCO"},
        {"NARANJA", "VERDE 2", "AZUL 2", "FLASH"},
        {"NARANJA 2", "CIAN", "VIOLETA", "STROBE"},
        {"AMARILLO", "CELESTE", "ROSA", "FADE"},
        {"AMARILLO 2", "AZUL 3", "ROSA 2", "SMOOTH"}
    };
    private static final int[][] COLORS = {
        {0xFFE8AD28,0xFFE8AD28,0xFF303030,0xFF2E8540},
        {0xFFEA2929,0xFF19A951,0xFF2555ED,0xFFDDDDDD},
        {0xFFFF6D17,0xFF2AD66B,0xFF1D70ED,0xFF333333},
        {0xFFFF8B22,0xFF0CCFC8,0xFF9834C8,0xFF333333},
        {0xFFFFCF16,0xFF22BCE7,0xFFFF43B6,0xFF333333},
        {0xFFF4DE1C,0xFF3457DE,0xFFED74C4,0xFF333333}
    };
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ir = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xFF10131A);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(18,24,18,24);
        scroll.addView(root);
        TextView title = new TextView(this);
        title.setText("CAMACHO RGB CONTROL"); title.setTextColor(Color.WHITE); title.setTextSize(23);
        title.setTypeface(null, Typeface.BOLD); title.setGravity(Gravity.CENTER);
        root.addView(title);
        status = new TextView(this); status.setTextColor(0xFFB6C3D1); status.setTextSize(14);
        status.setGravity(Gravity.CENTER); status.setPadding(0,12,0,22);
        status.setText(ir != null && ir.hasIrEmitter() ? "Emisor IR detectado. Apuntá a la luz." : "No se detectó emisor infrarrojo compatible.");
        root.addView(status);
        for(int r=0;r<6;r++) {
            LinearLayout line = new LinearLayout(this); line.setOrientation(LinearLayout.HORIZONTAL);
            for(int c=0;c<4;c++) {
                final int code=CODES[r][c];
                Button b = new Button(this); b.setText(LABELS[r][c]); b.setTextSize(11);
                b.setAllCaps(false); b.setTextColor(Color.WHITE);
                GradientDrawable bg = new GradientDrawable(); bg.setColor(COLORS[r][c]); bg.setCornerRadius(16);
                b.setBackground(bg);
                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,66,1);
                p.setMargins(4,5,4,5); line.addView(b,p);
                b.setOnClickListener(v -> send(code));
            }
            root.addView(line);
        }
        Button allOff=new Button(this); allOff.setText("APAGAR TODO (REPETIR OFF)");
        allOff.setTextColor(Color.WHITE); allOff.setBackgroundColor(0xFFAD2432);
        root.addView(allOff);
        allOff.setOnClickListener(v -> { for(int i=0;i<3;i++) send(0x82); });
        TextView hint=new TextView(this);
        hint.setText("Códigos de prueba para controles RGB de 24 teclas. Si no responden las luces, se necesita identificar el protocolo del control original. El infrarrojo requiere apuntar hacia el receptor.");
        hint.setTextColor(0xFFAAB3C1); hint.setTextSize(12); hint.setPadding(6,20,6,0);
        root.addView(hint);
        setContentView(scroll);
    }
    private void send(int command) {
        if(ir==null || !ir.hasIrEmitter()) { Toast.makeText(this,"Este teléfono no expone un emisor IR compatible",Toast.LENGTH_SHORT).show(); return; }
        // NEC: 9ms mark, 4.5ms space; 32 bits LSB first; 560us mark per bit.
        int[] bytes={0x00,0xFF,command & 0xFF,(~command)&0xFF};
        int[] pulse=new int[2+32*2+1]; int pos=0;
        pulse[pos++]=9000; pulse[pos++]=4500;
        for(int value:bytes) for(int bit=0;bit<8;bit++) {
            pulse[pos++]=560;
            pulse[pos++]=((value>>bit)&1)==1 ? 1690 : 560;
        }
        pulse[pos]=560;
        try { ir.transmit(38000,pulse); status.setText("Señal IR enviada: 0x"+Integer.toHexString(command).toUpperCase()); }
        catch(Exception ex) { Toast.makeText(this,"Error enviando IR: "+ex.getMessage(),Toast.LENGTH_LONG).show(); }
    }
}
