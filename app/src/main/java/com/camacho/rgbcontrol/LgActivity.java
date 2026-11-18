package com.camacho.rgbcontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.*;
import android.os.*;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class LgActivity extends Activity implements SensorEventListener {
    private ConsumerIrManager ir; private LgRemoteView remote; private boolean active,busy,moving;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final Handler handler=new Handler(Looper.getMainLooper());
    private SensorManager sensors;private Sensor gyro,accel;private long lastSample;private final double[] gravity={0,9.81,0};
    private final MotionMath math=new MotionMath();private WebOsPointer pointer;private SharedPreferences prefs;private TextView connectionStatus;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);ir=(ConsumerIrManager)getSystemService(Context.CONSUMER_IR_SERVICE);
        prefs=getSharedPreferences("lg-tv",MODE_PRIVATE);sensors=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        if(sensors!=null) { gyro=sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE);accel=sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); }
        remote=new LgRemoteView(this,new LgRemoteView.Listener() {
            public void press(int action) {
                if(action==LgCodes.INPUT_MENU) showInputMenu();
                else if(action==LgCodes.YOUTUBE_ADFREE) launchAdFree();
                else send(action);
            }
            public void configurePointer() { configure(); }
        });
        remote.setOnApplyWindowInsetsListener((v,insets)->{remote.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});
        setContentView(remote);
        pointer=new WebOsPointer(prefs,new WebOsPointer.Listener() {
            public void status(String message) { if(!active)return; if(!pointer.isReady())stopMotion();if(connectionStatus!=null)connectionStatus.setText(message);Toast.makeText(LgActivity.this,message,Toast.LENGTH_LONG).show(); }
            public void ready() { if(active)startMotion(); }
            public void certificate(String host,String hash) {
                if(!active)return;
                new AlertDialog.Builder(LgActivity.this).setTitle("Vincular esta TV LG")
                    .setMessage("La TV en "+host+" presentó un certificado local. Confirmá que esa IP corresponde a tu TV. Se recordará únicamente este certificado para esa IP.\n\nHuella SHA-256:\n"+hash)
                    .setPositiveButton("Confiar y vincular",(d,w)->{prefs.edit().putString("cert-"+host,hash).apply();pointer.connect(host);})
                    .setNegativeButton("Cancelar",null).show();
            }
        });
        fullscreen();
    }
    private void fullscreen() { getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION); }
    @Override public void onWindowFocusChanged(boolean focus) { super.onWindowFocusChanged(focus);if(focus)fullscreen(); }
    private void showInputMenu() {
        if(!active)return;
        String[] options={"HDMI 1","HDMI 2","HDMI 3","Abrir selector de entradas en la TV","Vincular TV por Wi-Fi"};
        new AlertDialog.Builder(this).setTitle("Fuentes de video")
            .setItems(options,(dialog,index)->{
                if(index<3) selectHdmi(index+1);
                else if(index==3) send(LgCodes.INPUT_MENU);
                else configure();
            }).setNegativeButton("Cerrar",null).show();
    }
    private void selectHdmi(int number) {
        if(pointer!=null && pointer.isControlReady()) {
            if(!pointer.switchHdmi(number)) toast("No se pudo enviar HDMI "+number);
        } else {
            // Use the LG discrete input IR code. Some models need the onscreen input selector.
            send(LgCodes.HDMI_1+number-1);
            toast("HDMI "+number+" enviado. Si no cambia, abrí el selector de entradas.");
        }
    }
    private void launchAdFree() {
        if(!active)return;
        if(pointer!=null && pointer.isControlReady()) {
            if(!pointer.launchAdFree()) toast("No se pudo abrir YouTube AdFree");
        } else {
            new AlertDialog.Builder(this).setTitle("YouTube AdFree")
                .setMessage("Para abrir directamente la aplicación instalada en tu LG, primero vinculá la TV por Wi-Fi. El control HDMI y APPS también funcionan por infrarrojo.")
                .setPositiveButton("Conectar TV", (dialog,which)->configure())
                .setNegativeButton("Cancelar",null).show();
        }
    }
    private void send(int action) {
        if(!active)return;
        if(action==LgCodes.OK && moving && pointer.click())return;
        if(busy)return;
        try { if(ir==null || !ir.hasIrEmitter()) { toast("Este dispositivo no expone emisor IR para los botones de TV");return; } }
        catch(RuntimeException ex) { toast("No se pudo acceder al emisor IR");return; }
        busy=true;
        worker.execute(()->{
            String failure=null;
            try { ir.transmit(38000,IrCodes.pattern(LgCodes.frame(action),false)); }
            catch(RuntimeException ex) { failure="No se pudo emitir el comando LG"; }
            final String error=failure;
            handler.postDelayed(()->{busy=false;if(active && !isDestroyed() && error!=null)toast(error);},50);
        });
    }
    private void configure() {
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);int pad=Math.round(18*getResources().getDisplayMetrics().density);box.setPadding(pad,pad,pad,pad);
        TextView info=new TextView(this);info.setText("Vinculación local con tu LG webOS. Teléfono y TV deben estar en la misma red Wi-Fi. Ingresá la IP local de la TV y aceptá la solicitud de control en pantalla. Esta conexión permite abrir YouTube AdFree y cambiar HDMI directamente. El puntero por movimiento es opcional.");box.addView(info);
        EditText address=new EditText(this);address.setSingleLine(true);address.setInputType(InputType.TYPE_CLASS_PHONE);address.setHint("192.168.1.20");address.setText(prefs.getString("host",""));box.addView(address);
        connectionStatus=new TextView(this);connectionStatus.setText(gyro==null ? "Sin giroscopio: el control Wi-Fi sigue disponible; solo el puntero está desactivado." : moving ? "Puntero activo. Mantené el teléfono estable y movelo suavemente; el botón central hace clic." : "Control Wi-Fi disponible tras vincular. El puntero por movimiento es opcional.");box.addView(connectionStatus);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Conectar TV LG por Wi-Fi")
            .setView(box).setPositiveButton("Conectar",null).setNeutralButton("Desactivar",(d,w)->{stopMotion();pointer.close();})
            .setNegativeButton("Cerrar",null).create();
        dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            String host=address.getText().toString().trim();
             if(!LocalTvAddress.valid(host)) { address.setError("Ingresá una IP local válida de tu TV");return; }
            stopMotion();prefs.edit().putString("host",host).apply();pointer.connect(host);
        }));dialog.show();
    }
    private void startMotion() {
        if(gyro==null || !active)return;math.reset();lastSample=0;gravity[0]=0;gravity[1]=9.81;gravity[2]=0;
        moving=sensors.registerListener(this,gyro,SensorManager.SENSOR_DELAY_GAME);
        if(accel!=null)sensors.registerListener(this,accel,SensorManager.SENSOR_DELAY_UI);
        remote.setMotion(moving);if(!moving)toast("El teléfono no pudo iniciar el giroscopio");
    }
    private void stopMotion() { moving=false;lastSample=0;math.reset();if(sensors!=null)sensors.unregisterListener(this);if(remote!=null)remote.setMotion(false); }
    @Override public void onSensorChanged(SensorEvent event) {
        if(!active || !moving || !pointer.isReady())return;
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER) {
            for(int i=0;i<3;i++)gravity[i]=gravity[i]*.85+event.values[i]*.15;
            return;
        }
        if(event.sensor.getType()!=Sensor.TYPE_GYROSCOPE)return;
        if(lastSample==0) { lastSample=event.timestamp;return; }
        double seconds=(event.timestamp-lastSample)*1e-9;
        if(seconds<.032)return;lastSample=event.timestamp;
        int[] delta=math.step3D(event.values[0],event.values[1],event.values[2],gravity[0],gravity[1],gravity[2],seconds);pointer.move(delta[0],delta[1]);
    }
    @Override public void onAccuracyChanged(Sensor sensor,int accuracy) { }
    private void toast(String message) { Toast.makeText(this,message,Toast.LENGTH_LONG).show(); }
    @Override protected void onResume() { super.onResume();active=true; }
    @Override protected void onPause() { active=false;remote.stopInteraction();stopMotion();if(pointer!=null)pointer.close();super.onPause(); }
    @Override protected void onDestroy() { if(pointer!=null)pointer.close();worker.shutdown();super.onDestroy(); }
}
