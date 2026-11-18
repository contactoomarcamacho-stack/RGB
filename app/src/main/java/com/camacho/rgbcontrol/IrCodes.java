package com.camacho.rgbcontrol;

/** NEC bytes in transmission order, each byte transmitted least-significant bit first. */
public final class IrCodes {
    private IrCodes() {}
    public static final String[] LABELS = {
        "Brillo +", "Brillo −", "OFF", "ON", "ROJO", "VERDE", "AZUL", "BLANCO",
        "NARANJA", "VERDE 2", "AZUL 2", "FLASH", "NARANJA 2", "CIAN", "VIOLETA", "STROBE",
        "AMARILLO", "CELESTE", "ROSA", "FADE", "AMARILLO 2", "AZUL 3", "ROSA 2", "SMOOTH"
    };
    public static final String[] PROFILES = {"V1 (original)", "V1 con bits invertidos", "RGB F7 (candidato)"};
    private static final int[] ORIGINAL = {
        0x3A,0xBA,0x82,0x02,0x1A,0x9A,0xA2,0x22,0x2A,0xAA,0x92,0x12,
        0x0A,0x8A,0xB2,0x32,0x38,0xB8,0x78,0xF8,0x18,0x98,0x58,0xD8
    };
    // F7 table expressed as wire-order bytes (reverse of displayed MSB hexadecimal).
    private static final int[] F7 = {
        0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x0B,
        0x0C,0x0D,0x0E,0x0F,0x10,0x11,0x12,0x13,0x14,0x15,0x16,0x17
    };
    public static int reverseByte(int value) { return Integer.reverse(value & 255) >>> 24; }
    public static long frame(int first, int second, int command) {
        return ((long)(first & 255) << 24) | ((long)(second & 255) << 16)
            | ((long)(command & 255) << 8) | ((~command) & 255);
    }
    public static long profile(int profile, int action) {
        if (profile < 0 || profile >= PROFILES.length || action < 0 || action >= LABELS.length)
            throw new IllegalArgumentException("Perfil o botón inválido");
        return frame(0, profile == 2 ? 0xEF : 0xFF,
            profile == 2 ? F7[action] : profile == 1 ? reverseByte(ORIGINAL[action]) : ORIGINAL[action]);
    }
    public static long parse(String text) {
        String hex = text.trim().replaceFirst("^(0x|0X)", "");
        if (!hex.matches("[0-9a-fA-F]{8}")) throw new IllegalArgumentException("Ingresá exactamente 8 dígitos hexadecimales");
        return Long.parseLong(hex,16);
    }
    public static int[] pattern(long frame, boolean msbFirst) {
        int[] result = new int[67]; int pos = 0;
        result[pos++] = 9000; result[pos++] = 4500;
        for (int b = 3; b >= 0; b--) {
            int value = (int)(frame >>> (b * 8)) & 255;
            for (int bit = 0; bit < 8; bit++) {
                result[pos++] = 560;
                result[pos++] = ((value >>> (msbFirst ? 7-bit : bit)) & 1) == 1 ? 1690 : 560;
            }
        }
        result[pos] = 560;
        return result;
    }
    public static String hex(long frame) { return String.format(java.util.Locale.ROOT,"%08X",frame); }
}
