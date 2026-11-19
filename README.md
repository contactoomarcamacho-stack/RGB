# CAMACHO RGB CONTROL · versión 5.4

## TV LG · controles premium
La pantalla LG conserva su control circular y añade el botón **FUENTES**.
El selector ahora usa tarjetas grandes con iconos de cada dispositivo:
- **TV BOX**, conectado al **HDMI 1**.
- **HDMI 2**, entrada libre.
- **PLAY 4**, conectado al **HDMI 3**.

Las tarjetas funcionan por IR usando los códigos directos LG existentes o por webOS con la TV vinculada. Si el modelo no admite selección directa, **TODAS LAS ENTRADAS** abre el selector nativo de la TV y permite moverse con flechas y OK. El diseño y los códigos de las luces RGB se conservan sin cambios.

## YouTube AdFree: un toque, sin IP recurrente
El botón AdFree abre el ID instalado `youtube.leanback.v4` de webOS. Se validó contra el repositorio público de YouTube AdFree (webOS Homebrew). Al abrir la pantalla de TV, la app intenta reconectarse en silencio a la IP y clave ya guardadas. Si se pulsa AdFree sin vinculación previa, la app busca la TV local mediante SSDP con el servicio `urn:lge-com:service:webos-second-screen:1`, sin pedir IP primero. Ante fallos de búsqueda, ofrece configurar la dirección manualmente una sola vez.

**Importante:** El control directo de aplicaciones necesita TV LG webOS y teléfono en la misma red local. La primera conexión requiere autorizar el certificado de la TV y aprobar el control en la pantalla de la TV. No hay método IR universal que permita abrir cualquier aplicación arbitraria sin emparejamiento. Algunos routers bloquean SSDP, por lo que se mantiene la configuración manual como opción de respaldo. El ID de YouTube oficial puede coincidir con el de AdFree; la TV abrirá lo que tenga instalado.

## Verificación
- Java 17, Android API 35.
- `tests/run.sh` incluye códigos IR y geometría.
- GitHub Actions ejecuta `assembleDebug lintDebug` y entrega APK.
- Probar en el LG físico el cambio de HDMI, vinculación inicial, reconexión Wi-Fi y aplicación AdFree.

Fuentes: https://github.com/webosbrew/youtube-webos
Servicio webOS SSDP: `urn:lge-com:service:webos-second-screen:1`.
