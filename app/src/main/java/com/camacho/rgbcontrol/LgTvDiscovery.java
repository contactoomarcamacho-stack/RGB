package com.camacho.rgbcontrol;

import android.content.Context;
import android.net.wifi.WifiManager;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Local-only LG webOS discovery, no Internet/cloud or location permission.
 * SSDP answers come back via unicast UDP; LG service type is checked before
 * accepting a private IPv4 address. Caller runs this on a background worker.
 */
public final class LgTvDiscovery {
    private LgTvDiscovery() {}
    public static String discover(Context context) {
        WifiManager.MulticastLock lock=null;
        DatagramSocket socket=null;
        try {
            WifiManager wifi=(WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if(wifi!=null) {
                lock=wifi.createMulticastLock("rgh-lg-ssdp");
                lock.setReferenceCounted(false);lock.acquire();
            }
            socket=new DatagramSocket();
            socket.setSoTimeout(700);
            byte[] request=("M-SEARCH * HTTP/1.1\r\n"+
                "HOST: 239.255.255.250:1900\r\n"+
                "MAN: \"ssdp:discover\"\r\n"+
                "MX: 1\r\n"+
                "ST: urn:lge-com:service:webos-second-screen:1\r\n\r\n")
                .getBytes(StandardCharsets.US_ASCII);
            DatagramPacket search=new DatagramPacket(request,request.length,InetAddress.getByName("239.255.255.250"),1900);
            socket.send(search);
            long deadline=System.nanoTime()+4_000_000_000L;
            boolean resent=false;
            while(System.nanoTime()<deadline) {
                if(!resent && deadline-System.nanoTime()<2_000_000_000L) {
                    socket.send(search);resent=true;
                }
                byte[] bytes=new byte[4096];
                DatagramPacket answer=new DatagramPacket(bytes,bytes.length);
                try {socket.receive(answer);}catch(java.net.SocketTimeoutException timeout){continue;}
                String data=new String(answer.getData(),answer.getOffset(),answer.getLength(),StandardCharsets.US_ASCII).toLowerCase(Locale.ROOT);
                // Do not connect to unrelated multicast responses or public addresses.
                if(!data.contains("urn:lge-com:service:webos-second-screen:1"))continue;
                String host=answer.getAddress().getHostAddress();
                if(LocalTvAddress.valid(host))return host;
            }
        } catch(Exception ignored) {
            // Search may be blocked on some routers. Manual setup remains available.
        } finally {
            if(socket!=null)socket.close();
            if(lock!=null && lock.isHeld())lock.release();
        }
        return null;
    }
}
