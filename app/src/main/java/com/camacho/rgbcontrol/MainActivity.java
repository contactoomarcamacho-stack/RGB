package com.camacho.rgbcontrol;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.Toast;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** LED-only remote. Uses the F7 / 40 kHz configuration confirmed on the user's strip. */
public class MainActivity extends Activity {
    private ConsumerIrManager ir;
    private SharedPreferences prefs;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private boolean busy, active;
    private int pending=-1;
    private WheelRemoteView remote;
    private final Handler handler=new Handler(Looper.getMainLooper());

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ir = (ConsumerIrManager)getSystemService(Context.CONSUMER_IR_SERVICE);
        prefs = getSharedPreferences("rgb-v2",MODE_PRIVATE);
        remote = new WheelRemoteView(this,this::send);
        remote.setOnApplyWindowInsetsListener((v,insets)->{
            remote.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
            return insets;
        });
        setContentView(remote);
        fullscreen();
    }
    private void fullscreen() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }
    @Override public void onWindowFocusChanged(boolean focus) {
        super.onWindowFocusChanged(focus); if (focus) fullscreen();
    }
    private void send(int action) {
        if (!active) return;
        if (busy) { pending=action; return; }
        long frame = IrCodes.profile(2,action);
        int frequency = 40000;
        boolean msb = false;
        // Retain any individual codes the user previously confirmed in V2.
        String saved = prefs.getString("key-"+action,null);
        if (saved != null) {
            try {
                String[] parts = saved.split(":");
                frame=IrCodes.parse(parts[0]); frequency=Integer.parseInt(parts[1]); msb=Boolean.parseBoolean(parts[2]);
            } catch (RuntimeException ex) { error("Código guardado inválido para este botón"); return; }
        }
        try {
            if (ir == null || !ir.hasIrEmitter()) { error("No se detectó un emisor infrarrojo compatible"); return; }
        } catch (RuntimeException ex) { error("No se pudo acceder al emisor infrarrojo"); return; }
        final long code = frame;
        final int carrier = frequency;
        final boolean order = msb;
        busy=true;
        final long started=SystemClock.uptimeMillis();
        worker.execute(()->{
            String failure=null;
            try { ir.transmit(carrier,IrCodes.pattern(code,order)); }
            catch (RuntimeException ex) { failure="No se pudo enviar la señal infrarroja"; }
            final String message = failure;
            runOnUiThread(()->{
                if (message != null) {
                    busy=false; pending=-1;
                    if (!isDestroyed() && active) error(message);
                    return;
                }
                // Coalesce fast wheel gestures: retain only the latest requested colour.
                handler.postDelayed(()->{
                    busy=false;
                    if (isDestroyed() || !active) { pending=-1; return; }
                    int next=pending; pending=-1;
                    if (next>=0) send(next);
                },Math.max(0,120-(SystemClock.uptimeMillis()-started)));
            });
        });
    }
    private void error(String message) { Toast.makeText(this,message,Toast.LENGTH_LONG).show(); }
    @Override protected void onResume() { super.onResume(); active=true; }
    @Override protected void onPause() { active=false; pending=-1; remote.stopInteraction(); super.onPause(); }
    @Override protected void onDestroy() { worker.shutdown(); super.onDestroy(); }
}
