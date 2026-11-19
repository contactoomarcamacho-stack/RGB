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

/** Unified RGB companion LG remote: premium sources and tap-to-launch AdFree. */
public final class LgActivity extends Activity implements SensorEventListener {
    private ConsumerIrManager ir;
    private LgRemoteView remote;
    private boolean active,busy,moving;
    private boolean pendingAdFree,connecting,searching,discoveryAttempted;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final Handler handler=new Handler(Looper.getMainLooper());
    private SensorManager sensors;
    private Sensor gyro,accel;
    private long lastSample;
    private final double[] gravity={0,9.81,0};
    private final MotionMath math=new MotionMath();
    private WebOsPointer pointer;
    private SharedPreferences prefs;
    private TextView connectionStatus;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ir=(ConsumerIrManager)getSystemService(Context.CONSUMER_IR_SERVICE);
        prefs=getSharedPreferences("lg-tv",MODE_PRIVATE);
        sensors=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        if(sensors!=null) {
            gyro=sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            accel=sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        remote=new LgRemoteView(this,new LgRemoteView.Listener() {
            public void press(int action) {
                if(action==LgCodes.INPUT_MENU) showInputMenu();
                else if(action==LgCodes.YOUTUBE_ADFREE) launchAdFree();
                else send(action);
            }
            public void configurePointer() { configure(); }
        });
        remote.setOnApplyWindowInsetsListener((v,insets)->{
            remote.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
            return insets;
        });
        setContentView(remote);
        pointer=new WebOsPointer(prefs,new WebOsPointer.Listener() {
            public void status(String message) {
                if(!active)return;
                if(!pointer.isReady())stopMotion();
                if(connectionStatus!=null)connectionStatus.setText(message);
                if(message.startsWith("La TV no aceptó") || message.startsWith("La TV rechazó"))
                    toast(message);
            }
            public void ready() { if(active)startMotion(); }
            public void connected() {
                connecting=false;
                if(!active)return;
                if(pendingAdFree) {
                    pendingAdFree=false;
                    if(!pointer.launchAdFree())toast("No se pudo abrir YouTube AdFree.");
                }
            }
            public void connectionFailed(String host) {
                connecting=false;
                if(!active)return;
                if(pendingAdFree && !discoveryAttempted) {
                    searchForTv();
                } else if(pendingAdFree) {
                    pendingAdFree=false;
                    showManualFallback();
                }
            }
            public void certificate(String host,String hash) {
                connecting=false;
                if(!active)return;
                new AlertDialog.Builder(LgActivity.this)
                    .setTitle("Autorizar tu TV LG")
                    .setMessage("Detecté una TV en "+host+". Para enlazarla de forma segura, comprobá que esta dirección corresponde a tu tele. Solo la primera vez hay que aceptar el certificado y la solicitud de control en pantalla.\n\nHuella SHA-256:\n"+hash)
                    .setPositiveButton("Confiar y conectar",(d,w)->{
                        prefs.edit().putString("cert-"+host,hash).apply();
                        connectHost(host);
                    })
                    .setNegativeButton("Cancelar",(d,w)->{pendingAdFree=false;})
                    .show();
            }
        });
        fullscreen();
    }

    private void fullscreen() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }
    @Override public void onWindowFocusChanged(boolean focus) {
        super.onWindowFocusChanged(focus);if(focus)fullscreen();
    }
    private void showInputMenu() {
        if(!active)return;
        LgSourcesDialog.show(this,new LgSourcesDialog.Listener() {
            public void selectHdmi(int number) { LgActivity.this.selectHdmi(number); }
            public void showTvInputPicker() { send(LgCodes.INPUT_MENU); }
            public void connectTv() { searchForTv(); }
        });
    }
    private void selectHdmi(int number) {
        if(pointer!=null && pointer.isControlReady()) {
            if(!pointer.switchHdmi(number))toast("No se pudo cambiar a HDMI "+number);
        } else {
            // LG models without discrete input support can use FUENTES > Todas las entradas.
            send(LgCodes.HDMI_1+number-1);
        }
    }
    private void launchAdFree() {
        if(!active)return;
        if(pointer!=null && pointer.isControlReady()) {
            pointer.launchAdFree();
            return;
        }
        pendingAdFree=true;
        if(connecting || searching) {
            toast("Conectando con la TV para abrir YouTube AdFree…");
            return;
        }
        discoveryAttempted=false;
        String saved=prefs.getString("host","");
        if(LocalTvAddress.valid(saved)) {
            connectHost(saved);
        } else {
            searchForTv();
        }
    }
    private void connectHost(String host) {
        if(!active || pointer==null)return;
        connecting=true;
        prefs.edit().putString("host",host).apply();
        pointer.connect(host);
    }
    private void searchForTv() {
        if(!active || searching)return;
        discoveryAttempted=true;
        searching=true;connecting=false;
        toast("Buscando tu TV LG por Wi-Fi…");
        worker.execute(()->{
            String found=LgTvDiscovery.discover(getApplicationContext());
            handler.post(()->{
                if(!active || isDestroyed())return;
                searching=false;
                if(found!=null) {
                    connectHost(found);
                } else {
                    showManualFallback();
                }
            });
        });
    }
    private void showManualFallback() {
        if(!active)return;
        new AlertDialog.Builder(this)
            .setTitle("No encontré la TV automáticamente")
            .setMessage("El celular y la TV tienen que estar en la misma red Wi-Fi. Si el router bloquea la búsqueda automática, podés vincularla una sola vez indicando la IP. Después la app la recuerda.")
            .setPositiveButton("Configurar una vez",(d,w)->configure())
            .setNegativeButton("Cerrar",null).show();
    }
    private void send(int action) {
        if(!active)return;
        if(action==LgCodes.OK && moving && pointer!=null && pointer.click())return;
        if(busy)return;
        try {
            if(ir==null || !ir.hasIrEmitter()) {
                toast("Este dispositivo no expone un emisor infrarrojo para TV");
                return;
            }
        }catch(RuntimeException ex){toast("No se pudo acceder al emisor IR");return;}
        busy=true;
        worker.execute(()->{
            String failure=null;
            try {ir.transmit(38000,IrCodes.pattern(LgCodes.frame(action),false));}
            catch(RuntimeException ex){failure="No se pudo emitir el comando LG";}
            final String error=failure;
            handler.postDelayed(()->{
                busy=false;
                if(active && !isDestroyed() && error!=null)toast(error);
            },50);
        });
    }
    private void configure() {
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);
        int pad=Math.round(18*getResources().getDisplayMetrics().density);
        box.setPadding(pad,pad,pad,pad);
        TextView info=new TextView(this);
        info.setText("La TV LG y el celular deben estar en la misma red Wi-Fi. Podés ingresar su IP una sola vez. Cuando autorices la vinculación en la TV, la aplicación recordará la dirección y abrirá YouTube AdFree directamente en los siguientes usos.");
        box.addView(info);
        EditText address=new EditText(this);
        address.setSingleLine(true);
        address.setInputType(InputType.TYPE_CLASS_PHONE);
        address.setHint("192.168.1.20");
        address.setText(prefs.getString("host",""));
        box.addView(address);
        connectionStatus=new TextView(this);
        connectionStatus.setText(moving?"Puntero activo por movimiento.":"El puntero es opcional. HDMI y apps no necesitan giroscopio.");
        box.addView(connectionStatus);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Vincular TV LG")
            .setView(box).setPositiveButton("Conectar",null)
            .setNeutralButton("Buscar TV",(d,w)->searchForTv())
            .setNegativeButton("Cerrar",null).create();
        dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            String host=address.getText().toString().trim();
            if(!LocalTvAddress.valid(host)) {
                address.setError("Ingresá una IP privada válida de tu TV");
                return;
            }
            dialog.dismiss();
            discoveryAttempted=true;
            stopMotion();
            connectHost(host);
        }));
        dialog.show();
    }
    private void startMotion() {
        if(gyro==null || !active || sensors==null)return;
        math.reset();lastSample=0;
        gravity[0]=0;gravity[1]=9.81;gravity[2]=0;
        moving=sensors.registerListener(this,gyro,SensorManager.SENSOR_DELAY_GAME);
        if(accel!=null)sensors.registerListener(this,accel,SensorManager.SENSOR_DELAY_UI);
        remote.setMotion(moving);
        if(!moving)toast("No se pudo iniciar el puntero del teléfono");
    }
    private void stopMotion() {
        moving=false;lastSample=0;math.reset();
        if(sensors!=null)sensors.unregisterListener(this);
        if(remote!=null)remote.setMotion(false);
    }
    @Override public void onSensorChanged(SensorEvent event) {
        if(!active || !moving || pointer==null || !pointer.isReady())return;
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER) {
            for(int i=0;i<3;i++)gravity[i]=gravity[i]*.85+event.values[i]*.15;
            return;
        }
        if(event.sensor.getType()!=Sensor.TYPE_GYROSCOPE)return;
        if(lastSample==0){lastSample=event.timestamp;return;}
        double seconds=(event.timestamp-lastSample)*1e-9;
        if(seconds<.032)return;lastSample=event.timestamp;
        int[] delta=math.step3D(event.values[0],event.values[1],event.values[2],
            gravity[0],gravity[1],gravity[2],seconds);
        pointer.move(delta[0],delta[1]);
    }
    @Override public void onAccuracyChanged(Sensor sensor,int accuracy) {}
    private void toast(String message) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
    @Override protected void onResume() {
        super.onResume();active=true;
        String host=prefs.getString("host","");
        if(LocalTvAddress.valid(host) && pointer!=null && !pointer.isControlReady() && !connecting)
            connectHost(host);
    }
    @Override protected void onPause() {
        active=false;pendingAdFree=false;connecting=false;
        remote.stopInteraction();stopMotion();
        if(pointer!=null)pointer.close();
        super.onPause();
    }
    @Override protected void onDestroy() {
        if(pointer!=null)pointer.close();
        worker.shutdownNow();
        super.onDestroy();
    }
}
