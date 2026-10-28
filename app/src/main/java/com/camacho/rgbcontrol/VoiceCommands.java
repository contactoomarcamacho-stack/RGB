package com.camacho.rgbcontrol;

import java.text.Normalizer;
import java.util.Locale;

/** Closed Spanish command vocabulary; unrelated speech never sends a TV command. */
public final class VoiceCommands {
    private VoiceCommands() {}
    public static int parse(String phrase) {
        if(phrase==null)return -1;
        String s=Normalizer.normalize(phrase.toLowerCase(Locale.ROOT),Normalizer.Form.NFD)
            .replaceAll("\\p{M}","").replaceAll("[^a-z0-9 ]"," ").replaceAll("\\s+"," ").trim();
        s=s.replaceFirst("^(por favor |podes |puedes |podrias )","").replaceFirst(" por favor$","").trim();
        if(s.matches("(subi|sube|subir|aumenta|aumentar)( el)? volumen|mas volumen|volumen (arriba|mas)"))return LgCodes.VOLUME_UP;
        if(s.matches("(baja|bajar|baje)( el)? volumen|baja volumen|menos volumen|volumen (abajo|menos)"))return LgCodes.VOLUME_DOWN;
        if(s.matches("silencio|silenciar|mute|mutear|(activa|activar)( el)? silencio|(quita|quitar|saca|sacar|desactiva|desactivar)( el)? silencio|desmutear"))return LgCodes.MUTE;
        if(s.matches("arriba|subir|subi|sube|(ir|anda|ve|mover)( hacia| para)? arriba"))return LgCodes.UP;
        if(s.matches("abajo|bajar|baja|(ir|anda|ve|mover)( hacia| para)? abajo"))return LgCodes.DOWN;
        if(s.matches("izquierda|(a la|hacia la|para la|ir a la|anda a la|mover a la) izquierda"))return LgCodes.LEFT;
        if(s.matches("derecha|(a la|hacia la|para la|ir a la|anda a la|mover a la) derecha"))return LgCodes.RIGHT;
        if(s.matches("ok|okay|aceptar|acepta|confirmar|confirma|seleccionar|selecciona|entrar|enter|clic|click"))return LgCodes.OK;
        if(s.matches("(encender|enciende|encende|prender|prende|apagar|apaga|apague)( el| la)?( televisor| television| tele| tv)?|encendido|apagado|power"))return LgCodes.POWER;
        return -1;
    }
}
