package com.camacho.rgbcontrol;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;

/** Optional local webOS pointer connection. No Bluetooth/RF impersonation. */
public final class WebOsPointer {
    public interface Listener { void status(String message); void certificate(String host,String fingerprint); void ready(); }
    private final SharedPreferences prefs; private final Listener listener;
    private final Handler main=new Handler(Looper.getMainLooper());
    private volatile int generation;
    private volatile WebSocketClient control,pointer;
    private volatile String candidate;
    public WebOsPointer(SharedPreferences prefs,Listener listener) { this.prefs=prefs;this.listener=listener; }
    public boolean isReady() { WebSocketClient socket=pointer;return socket!=null && socket.isOpen(); }
    public void connect(String host) {
        close();
        if(!LocalTvAddress.valid(host)) { listener.status("Ingresá la IP local de la TV, por ejemplo 192.168.1.20");return; }
        final int session=generation; candidate=null;
        try {
            control=socket(new URI("wss://"+host+":3001/"),host,session,false);
            listener.status("Conectando con la TV…");control.connect();
            main.postDelayed(()->{
                if(session==generation && !isReady()) { close();listener.status("La TV no completó la vinculación. Revisá webOS, la IP, el Wi-Fi y la autorización en pantalla."); }
            },45000);
        } catch(Exception ex) { listener.status("No se pudo iniciar la conexión con la TV"); }
    }
    private void post(int session,Runnable work) { main.post(()->{if(session==generation)work.run();}); }
    private WebSocketClient socket(URI uri,String host,int session,boolean mouse) throws Exception {
        if(!"wss".equals(uri.getScheme()) || !host.equals(uri.getHost()) || uri.getPort()!=3001 || uri.getUserInfo()!=null)
            throw new IllegalArgumentException("Destino del puntero no válido");
        WebSocketClient ws=new WebSocketClient(uri) {
            @Override protected void onSetSSLParameters(SSLParameters parameters) {
                // Authentication uses the explicitly accepted certificate pin for this exact IP.
                if(Build.VERSION.SDK_INT>=24) parameters.setEndpointIdentificationAlgorithm(null);
            }
            @Override public void onOpen(ServerHandshake handshake) {
                if(session!=generation) { close();return; }
                if(mouse) { post(session,()->{listener.status("Puntero Wi-Fi conectado");listener.ready();});return; }
                try {
                    JSONObject manifest=new JSONObject().put("manifestVersion",1).put("appVersion","5.0")
                        .put("permissions",new JSONArray(new String[]{"CONTROL_INPUT_JOYSTICK","CONTROL_MOUSE_AND_KEYBOARD","CONTROL_INPUT_TEXT"}));
                    JSONObject payload=new JSONObject().put("pairingType","PROMPT").put("forcePairing",false).put("manifest",manifest);
                    String key=prefs.getString("key-"+host,"");if(!key.isEmpty())payload.put("client-key",key);
                    send(new JSONObject().put("type","register").put("id","register").put("payload",payload).toString());
                    post(session,()->listener.status("Aceptá la solicitud de control que aparece en la TV"));
                } catch(Exception ex) { post(session,()->listener.status("No se pudo solicitar autorización a webOS")); }
            }
            @Override public void onMessage(String message) {
                if(mouse || session!=generation)return;
                try {
                    JSONObject data=new JSONObject(message);String type=data.optString("type");JSONObject payload=data.optJSONObject("payload");
                    if("registered".equals(type)) {
                        if(payload!=null && payload.has("client-key"))prefs.edit().putString("key-"+host,payload.getString("client-key")).apply();
                        send(new JSONObject().put("type","request").put("id","pointer").put("uri","ssap://com.webos.service.networkinput/getPointerInputSocket").toString());
                    } else if("response".equals(type) && "pointer".equals(data.optString("id")) && payload!=null) {
                        URI path=new URI(payload.getString("socketPath"));
                        if(pointer!=null)pointer.close();pointer=socket(path,host,session,true);pointer.connect();
                    } else if("error".equals(type)) post(session,()->{WebOsPointer.this.close();listener.status("webOS rechazó el control del puntero. Puede requerir otra configuración o no ser compatible con este modelo.");});
                } catch(Exception ex) { post(session,()->{WebOsPointer.this.close();listener.status("La respuesta de la TV no permite abrir un puntero compatible");}); }
            }
            @Override public void onClose(int code,String reason,boolean remote) {
                post(session,()->{WebOsPointer.this.close();listener.status("Puntero desconectado. Los botones infrarrojos siguen disponibles.");});
            }
            @Override public void onError(Exception ex) {
                post(session,()->{
                    String fingerprint=candidate;WebOsPointer.this.close();
                    if(fingerprint!=null)listener.certificate(host,fingerprint);
                    else listener.status("No se pudo conectar el puntero. Revisá la IP, el Wi-Fi y que la TV tenga webOS compatible.");
                });
            }
        };
        final String expected=prefs.getString("cert-"+host,"");
        X509TrustManager trust=new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            public void checkClientTrusted(X509Certificate[] chain,String type) throws CertificateException { throw new CertificateException("Solo TV"); }
            public void checkServerTrusted(X509Certificate[] chain,String type) throws CertificateException {
                try {
                    if(chain==null || chain.length==0)throw new CertificateException("Sin certificado");
                    byte[] bytes=MessageDigest.getInstance("SHA-256").digest(chain[0].getEncoded());StringBuilder hash=new StringBuilder();
                    for(byte b:bytes)hash.append(String.format(java.util.Locale.ROOT,"%02X",b & 255));
                    String actual=hash.toString();
                    if(!actual.equals(expected)) { candidate=actual;throw new CertificateException("Certificado pendiente de aceptación"); }
                } catch(CertificateException ex) { throw ex; } catch(Exception ex) { throw new CertificateException(ex); }
            }
        };
        SSLContext tls=SSLContext.getInstance("TLS");tls.init(null,new TrustManager[]{trust},null);ws.setSocketFactory(tls.getSocketFactory());
        ws.setConnectionLostTimeout(15);return ws;
    }
    public void move(int dx,int dy) {
        WebSocketClient ws=pointer;
        if(ws!=null && ws.isOpen() && !ws.hasBufferedData() && (dx!=0 || dy!=0)) {
            try { ws.send("type:move\ndx:"+dx+"\ndy:"+dy+"\ndown:0\n\n"); } catch(RuntimeException ignored) { }
        }
    }
    public boolean click() {
        WebSocketClient ws=pointer;if(ws==null || !ws.isOpen())return false;
        try { ws.send("type:click\n\n");return true; } catch(RuntimeException ex) { return false; }
    }
    public void close() {
        generation++;WebSocketClient p=pointer,c=control;pointer=null;control=null;
        if(p!=null)p.close();if(c!=null)c.close();
    }
}
