# CAMACHO RGB CONTROL · luces y TV LG

Versión 5.1 (versionCode 7). El botón TV de la rueda LED abre una segunda pantalla basada en la referencia del usuario: fondo oscuro, controles en cian, encendido, flechas, confirmación, volumen, silencio y micrófono. El mando LG no muestra títulos ni palabras: solo iconos. Las etiquetas para lectores de pantalla se conservan. Atrás de Android vuelve al control de luces. La rueda LED, sus códigos y la frecuencia de 40 kHz se conservan.

## TV por infrarrojo

Los nueve botones usan el juego común LG NEC (bytes de dirección 04 FB) a 38 kHz. La potencia es un conmutador encendido/apagado. Volumen y flechas repiten mientras se mantienen pulsados. Requiere emisor IR accesible y apuntar al receptor. La tabla es un candidato LG habitual; confirmar con el televisor real antes de afirmar compatibilidad específica. No se envían códigos de servicio.

## Puntero por movimiento (experimental)

1. Encendé la TV y conectá teléfono y TV a la misma red Wi-Fi.
2. En el control LG, mantené el botón central de confirmación pulsado para abrir la configuración.
3. Ingresá la dirección IPv4 privada de tu TV (se ve en sus ajustes de red) y tocá Conectar.
4. La primera vez, confirmá que la IP corresponde a tu TV y aceptá su certificado local. La app recuerda su huella únicamente para esa IP; si cambia, vuelve a pedir confirmación. Luego aceptá la solicitud de vinculación que aparece en la TV.
5. Si webOS concede la conexión y el teléfono tiene giroscopio, aparece un punto verde y se activa el movimiento. Mové suavemente el teléfono; El botón central hace clic con el puntero. Las flechas siguen funcionando como navegación convencional.
6. Mantené el botón central pulsado y elegí Desactivar para volver a navegación normal. Al salir de la pantalla LG o poner la app en segundo plano, sensores y conexiones se detienen. No se reactiva automáticamente: volvé a Conectar cuando lo necesites.

Esta función usa el protocolo local de webOS por WSS en el puerto 3001 y su socket de entrada. No imita el emparejamiento Bluetooth ni la radio del Magic Remote. Requiere webOS y permisos compatibles con el modelo/firmware; no es compatible con todos los televisores LG ni con NetCast. No hay búsqueda automática de TV, fallback a conexiones sin cifrar, voz, micrófono ni funciones de servicio.

El giroscopio mide los movimientos y el acelerómetro ayuda a compensar la inclinación lateral. Se filtra el ruido y se limitan los desplazamientos. Si no existe giroscopio, la app lo informa y mantiene los botones IR. No se guarda un historial de sensores ni se envían datos a servidores externos. Solo se conecta a la IP privada que ingresa el usuario; la primera vinculación y los cambios de certificado requieren confirmación. La clave de vinculación se guarda en las preferencias privadas de la app.

## Control de voz

Tocá el icono de micrófono del control LG. Se abre el reconocedor de voz disponible en Android, configurado para español de Uruguay. Ejemplos: «subí el volumen», «bajá el volumen», «silencio», «arriba», «abajo», «izquierda», «derecha», «confirmar», «encendé la tele» y «apagá el televisor». Cada orden envía una sola pulsación del botón correspondiente. Alimentación y silencio siguen siendo conmutadores, igual que los botones físicos: la app no conoce el estado real del televisor.

Se ignoran frases ajenas al vocabulario y negaciones como «no apagues la tele». No es una búsqueda de contenido ni el asistente de voz de LG. La app no escucha permanentemente ni incorpora grabación de audio: recibe el texto del servicio Android después de que el usuario toca el micrófono. Ese servicio puede mostrar su propia pantalla, solicitar acceso al micrófono o necesitar Internet; si no existe o no está disponible, la app lo informa sin cerrarse. La voz requiere apuntar al receptor IR, igual que los botones. Si había un puntero vinculado activo, al volver del reconocedor se intenta restaurar esa conexión.

## Ajuste de pantalla

LgGeometry usa el área segura real y ajusta tamaños y posiciones de los diez iconos. En vertical agrupa encendido, cruceta y botones inferiores; en horizontal mueve alimentación/micrófono a la izquierda y volumen a la derecha para aprovechar la altura. No estira círculos ni muestra las leyendas anteriores. El control LED conserva sus nombres y diseño previos; la solicitud de quitar palabras corresponde a la pantalla LG. Los diálogos de conexión, los avisos de error y la pantalla del servicio de voz conservan texto necesario para usarlos.

## Validación y compilación

Actions → Compilar APK LED → ejecución exitosa → artefacto CAMACHO-RGB-CONTROL-LED. Extraé app-debug.apk para instalar. Java 17, Gradle 8.9, AGP 8.7.3, compileSdk/targetSdk 35, minSdk 23. Dependencia de transporte: Java-WebSocket 1.5.7 (MIT), con su dependencia de registro SLF4J.

Las pruebas verifican pulsos NEC y LG, complementos, equivalencia MSB/LSB, rueda LED, dimensiones previas, validación de IP y conversión de movimiento. El workflow ejecuta assembleDebug y lintDebug. La vinculación real, permiso del socket de puntero, certificado del TV, sensibilidad de sensores y respuesta física requieren prueba en el teléfono y televisor del usuario. No se presume un modelo específico a partir de fotos de un anuncio de mandos.

## Referencias primarias

- LG: https://www.lg.com/us/support/help-library/lg-tv-how-to-set-up-the-lg-thinq-app-on-your-lg-smart-tv--20152745625356
- API local webOS de Connect SDK (LG Electronics): https://github.com/ConnectSDK/Connect-SDK-Android-Core/blob/master/src/com/connectsdk/service/WebOSTVService.java
- Formato del socket de puntero: https://github.com/ConnectSDK/Connect-SDK-Android-Core/blob/master/src/com/connectsdk/service/webos/WebOSTVMouseSocketConnection.java
- Códigos LG: https://www.lg.com/us/business/download/resources/BT00001837/206-4322_EV960H_Comm_Mode_Setup_Rev_C.pdf
- Transporte: https://github.com/TooTallNate/Java-WebSocket

El protocolo se implementa de forma independiente usando sus formatos públicos; no se copia código fuente de Connect SDK en la aplicación. Java-WebSocket se integra como dependencia MIT. La instalación de un APK de depuración puede requerir desinstalar la versión anterior si su firma difiere (se borran los datos locales).

Voz Android: https://developer.android.com/reference/android/speech/RecognizerIntent
