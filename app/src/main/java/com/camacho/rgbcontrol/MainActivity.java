package com.camacho.rgbcontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.widget.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private ConsumerIrManager ir;
    private ConsumerIrManager.CarrierFrequencyRange[] ranges;
    private SharedPreferences prefs;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private TextView status, lastView;
    private Spinner profiles, frequencies, addresses, order, actions;
    private EditText manual;
    private LinearLayout root;
    private boolean busy, scanning, active;
    private int scanCommand, scanAddress, scanFrequency;
    private long lastFrame;
    private int lastFrequency;
    private boolean lastMsb, hasLast;
    private final int[] frequencyValues = {38000,36000,40000};
    // Includes byte-order and MSB/LSB address variants. Search commands 00..FF for each.
    private final int[][] addressValues = {{0,255},{0,239},{0,247},{255,0},{0,0}};
    private final Runnable nextScan = () -> scanNext();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("rgb-v2",MODE_PRIVATE);
        ir = (ConsumerIrManager)getSystemService(Context.CONSUMER_IR_SERVICE);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xFF10131A);
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(24),dp(16),dp(24)); scroll.addView(root);
        scroll.setOnApplyWindowInsetsListener((v,insets)->{
            root.setPadding(dp(16)+insets.getSystemWindowInsetLeft(),dp(24)+insets.getSystemWindowInsetTop(),
                dp(16)+insets.getSystemWindowInsetRight(),dp(24)+insets.getSystemWindowInsetBottom());
            return insets;
        });
        text("CAMACHO RGB CONTROL · V2",23);
        status = text("",14);
        try {
            if (ir == null || !ir.hasIrEmitter()) {
                status.setText("No se detectó un emisor IR accesible a Android. La app no puede emitir desde este dispositivo.");
            } else {
                ranges = ir.getCarrierFrequencies();
                StringBuilder report = new StringBuilder("Emisor IR detectado. Frecuencias: ");
                if (ranges == null || ranges.length == 0) report.append("sin información del fabricante; probá 38 kHz.");
                else for (ConsumerIrManager.CarrierFrequencyRange r : ranges)
                    report.append(r.getMinFrequency()/1000.0).append("–").append(r.getMaxFrequency()/1000.0).append(" kHz; ");
                status.setText(report);
            }
        } catch (RuntimeException ex) { status.setText("No se pudo consultar el emisor: " + ex.getMessage()); }
        text("Apuntá el emisor del teléfono al receptor de las luces, a corta distancia. La respuesta se confirma mirando las luces.",14);
        text("Control de 24 botones",19);
        profiles = spinner(IrCodes.PROFILES);
        profiles.setSelection(prefs.getInt("profile",0));
        text("Frecuencia para control y pruebas",14);
        frequencies = spinner(new String[]{"38 kHz", "36 kHz", "40 kHz"});
        frequencies.setSelection(prefs.getInt("frequency",0));
        for (int row=0;row<6;row++) {
            LinearLayout line = new LinearLayout(this);
            for (int col=0;col<4;col++) {
                final int action = row*4+col;
                Button button = new Button(this); button.setText(IrCodes.LABELS[action]); button.setTextSize(11);
                line.addView(button,new LinearLayout.LayoutParams(0,dp(60),1));
                button.setOnClickListener(v -> {
                    pauseScan();
                    String saved = prefs.getString("key-"+action,null);
                    if (saved == null) transmit(IrCodes.profile(profiles.getSelectedItemPosition(),action),selectedFrequency(),false,null);
                    else {
                        try {
                            String[] parts = saved.split(":");
                            transmit(IrCodes.parse(parts[0]),Integer.parseInt(parts[1]),Boolean.parseBoolean(parts[2]),null);
                        } catch (RuntimeException ex) { status.setText("Código guardado inválido. Borrá la asignación y probá de nuevo."); }
                    }
                });
                button.setOnLongClickListener(v -> {
                    pauseScan();
                    new AlertDialog.Builder(this).setTitle(IrCodes.LABELS[action])
                        .setMessage("¿Borrar el código personalizado de este botón?")
                        .setPositiveButton("Borrar",(d,w)->prefs.edit().remove("key-"+action).apply())
                        .setNegativeButton("Cancelar",null).show(); return true;
                });
            }
            root.addView(line);
        }
        text("Buscar códigos NEC",19);
        text("Probá primero ON y los colores de cada perfil. Si no responden, buscá por dirección: se prueban 256 comandos. Cualquier comando podría apagar o cambiar las luces. Cuando respondan, tocá PAUSAR. La app no recibe ni aprende la señal del control original.",14);
        addresses = spinner(new String[]{"00 FF · V1", "00 EF · F7 con bytes LSB", "00 F7 · variante", "FF 00 · variante", "00 00 · variante"});
        button("PROBAR SIGUIENTE CÓDIGO", () -> { pauseScan(); scanNext(); });
        button("INICIAR / CONTINUAR BÚSQUEDA (1,5 s)", () -> {
            if (busy || scanning) return;
            scanning = true; scanNext();
        });
        button("PAUSAR", () -> { pauseScan(); status.setText(busy ? "Pausado. Finalizando la señal en curso…" : "Pausado. Revisá el último código enviado."); });
        button("REINICIAR BÚSQUEDA EN 00", () -> { pauseScan(); if (!busy) { scanCommand=0; status.setText("Búsqueda reiniciada. Elegí una dirección y probá el siguiente código."); } });
        text("Código manual NEC de 32 bits",19);
        text("8 dígitos hexadecimales, sin espacios. Elegí el orden según la fuente del código. No convierte Pronto ni otros protocolos.",14);
        manual = new EditText(this); manual.setSingleLine(true); manual.setTextColor(Color.WHITE);
        manual.setHintTextColor(0xFFB6C3D1); manual.setHint("Ejemplo MSB: 00F7C03F"); root.addView(manual);
        order = spinner(new String[]{"Bytes LSB (V1 / búsqueda)", "Bits MSB (ejemplo 00F7C03F)"});
        button("PROBAR CÓDIGO MANUAL", () -> {
            pauseScan();
            try { transmit(IrCodes.parse(manual.getText().toString()),selectedFrequency(),order.getSelectedItemPosition()==1,null); }
            catch (IllegalArgumentException ex) { status.setText(ex.getMessage()); }
        });
        lastView = text("Todavía no se envió ningún código.",14);
        button("REPETIR ÚLTIMO CÓDIGO", () -> {
            pauseScan(); if(hasLast) transmit(lastFrame,lastFrequency,lastMsb,null);
            else status.setText("Primero probá un código.");
        });
        text("Guardar el último código que funcionó",19);
        actions = spinner(IrCodes.LABELS);
        button("FUNCIONÓ: ASIGNAR AL BOTÓN ELEGIDO", () -> {
            pauseScan();
            if (busy) { status.setText("Esperá a que termine el envío antes de guardar."); return; }
            if (!hasLast) { status.setText("Primero probá un código."); return; }
            final int action = actions.getSelectedItemPosition();
            new AlertDialog.Builder(this).setTitle("Asignar a " + IrCodes.LABELS[action])
                .setMessage("Último envío: " + description(lastFrame,lastFrequency,lastMsb) + "\nConfirmá que esa fue la función que hicieron las luces.")
                .setPositiveButton("Guardar",(d,w)->{
                    prefs.edit().putString("key-"+action,IrCodes.hex(lastFrame)+":"+lastFrequency+":"+lastMsb).apply();
                    status.setText("Guardado para " + IrCodes.LABELS[action] + ". Mantené pulsado ese botón para borrar su código.");
                }).setNegativeButton("Cancelar",null).show();
        });
        text("Cada botón guardado conserva su código, frecuencia y orden de bits. Los demás usan el perfil seleccionado. Si ningún código responde, puede tratarse de otro protocolo, un emisor no compatible o un receptor diferente. No hay compatibilidad universal garantizada.",13);
        setContentView(scroll);
    }
    private int dp(int n) { return Math.round(n*getResources().getDisplayMetrics().density); }
    private TextView text(String value,int size) {
        TextView v = new TextView(this); v.setText(value); v.setTextColor(Color.WHITE); v.setTextSize(size);
        v.setPadding(0,dp(8),0,dp(8)); root.addView(v); return v;
    }
    private Spinner spinner(String[] values) {
        Spinner s = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,values);
        s.setBackgroundColor(0xFFE1E6EE); s.setAdapter(adapter); root.addView(s); return s;
    }
    private void button(String label,Runnable action) {
        Button b = new Button(this); b.setText(label); root.addView(b); b.setOnClickListener(v->action.run());
    }
    private int selectedFrequency() { return frequencyValues[frequencies.getSelectedItemPosition()]; }
    private void pauseScan() { scanning=false; handler.removeCallbacks(nextScan); }
    private void scanNext() {
        if (busy) return;
        int address = addresses.getSelectedItemPosition(), frequency = selectedFrequency();
        if (address != scanAddress || frequency != scanFrequency) { scanCommand=0; scanAddress=address; scanFrequency=frequency; }
        if (scanCommand >= 256) { pauseScan(); status.setText("Terminó esta dirección (256 códigos). Cambiá la dirección o la frecuencia y continuá."); return; }
        final int command = scanCommand;
        long frame = IrCodes.frame(addressValues[address][0],addressValues[address][1],command);
        transmit(frame,frequency,false,()->{
            scanCommand=command+1;
            if (scanning) handler.postDelayed(nextScan,1500);
        });
    }
    private String description(long frame,int frequency,boolean msb) {
        return IrCodes.hex(frame) + " · " + frequency/1000 + " kHz · " + (msb ? "MSB" : "bytes LSB");
    }
    private void transmit(long frame,int frequency,boolean msb,Runnable after) {
        if (busy || !active) return;
        try {
            if (ir == null || !ir.hasIrEmitter()) throw new IllegalStateException("No hay emisor IR compatible accesible a Android");
            if (ranges != null && ranges.length > 0) {
                boolean supported=false;
                for (ConsumerIrManager.CarrierFrequencyRange r:ranges)
                    if (frequency >= r.getMinFrequency() && frequency <= r.getMaxFrequency()) supported=true;
                if (!supported) throw new IllegalArgumentException("El emisor no admite " + frequency/1000 + " kHz. Elegí otra frecuencia.");
            }
        } catch (RuntimeException ex) { pauseScan(); status.setText(ex.getMessage()); return; }
        busy=true;
        status.setText("Enviando " + description(frame,frequency,msb));
        worker.execute(()->{
            String error=null;
            try { ir.transmit(frequency,IrCodes.pattern(frame,msb)); }
            catch (RuntimeException ex) { error=ex.getClass().getSimpleName()+": "+ex.getMessage(); }
            final String failure=error;
            handler.post(()->{
                busy=false;
                if (isDestroyed()) return;
                if (failure != null) { pauseScan(); status.setText("Error de emisión: " + failure); return; }
                lastFrame=frame; lastFrequency=frequency; lastMsb=msb; hasLast=true;
                lastView.setText("Último envío: " + description(frame,frequency,msb));
                status.setText("Android completó el envío. Confirmá mirando las luces.");
                if (after != null && active) after.run();
            });
        });
    }
    @Override protected void onResume() { super.onResume(); active=true; }
    @Override protected void onPause() {
        active=false; pauseScan();
        prefs.edit().putInt("profile",profiles.getSelectedItemPosition()).putInt("frequency",frequencies.getSelectedItemPosition()).apply();
        super.onPause();
    }
    @Override protected void onDestroy() { pauseScan(); worker.shutdown(); super.onDestroy(); }
}
