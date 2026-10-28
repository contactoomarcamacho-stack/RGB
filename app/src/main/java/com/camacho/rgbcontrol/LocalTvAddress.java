package com.camacho.rgbcontrol;

/** Manual connection is restricted to RFC1918 IPv4 addresses, never arbitrary hosts. */
public final class LocalTvAddress {
    private LocalTvAddress() {}
    public static boolean valid(String address) {
        if(address==null || !address.matches("[0-9]{1,3}(\\.[0-9]{1,3}){3}")) return false;
        String[] parts=address.split("\\."); int[] n=new int[4];
        for(int i=0;i<4;i++) { n[i]=Integer.parseInt(parts[i]); if(n[i]>255 || (parts[i].length()>1 && parts[i].startsWith("0"))) return false; }
        return (n[0]==10 || (n[0]==192 && n[1]==168) || (n[0]==172 && n[1]>=16 && n[1]<=31)) && n[3]!=0 && n[3]!=255;
    }
}
