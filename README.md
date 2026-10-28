# CAMACHO RGB CONTROL · rueda táctil LED

Versión 4.0, versionCode 5. Pantalla oscura basada en la referencia del usuario: brillo a ambos lados de OFF/ON, rueda de colores grande, FLASH/STROBE/FADE/SMOOTH, WHITE y TV. El botón TV se muestra sin función y no emite señales.

## Uso

Apuntá el emisor del teléfono al receptor de la tira LED. Tocá o deslizá el dedo por el aro de color para cambiar la luz. El marcador muestra la posición del dedo. El receptor utiliza los 15 colores del mando IR original: se envía el color disponible más cercano, no RGB arbitrario de 24 bits. El centro oscuro no es una segunda zona de selección.

OFF, ON, WHITE y los cuatro efectos conservan los comandos de la versión anterior. Los iconos de sol permiten bajar o subir brillo; mantenerlos pulsados repite el ajuste. Se usa F7 a 40 kHz, que el usuario ya confirmó con su tira LED. Se respetan los códigos individuales previamente guardados si se actualiza sin borrar datos.

Los envíos son secuenciales, limitados a aproximadamente uno cada 120 ms. Si el gesto produce cambios más rápido, se conserva únicamente el comando más reciente para terminar en el último color elegido, sin acumular una cola de colores antiguos. Al salir se descartan los comandos pendientes y se detiene la repetición de brillo. Una señal en curso puede terminar.

La interfaz se adapta al área disponible y respeta los márgenes del sistema. Admite navegación por teclado en la rueda y etiquetas accesibles. La aplicación no recibe información de las luces: el marcador y los botones no confirman el estado físico del receptor.

## APK y validación

Actions → Compilar APK LED → ejecución exitosa → artefacto CAMACHO-RGB-CONTROL-LED → extraer app-debug.apk. El workflow ejecuta pruebas NEC, geometría y selección de la rueda, seguidas de assembleDebug y lintDebug. Requiere Java 17 / Gradle 8.9 / AGP 8.7.3; Android 6 o posterior y emisor accesible a ConsumerIrManager.

Para actualización estable se necesita una firma de lanzamiento permanente; las firmas de depuración pueden variar entre compilaciones. La respuesta final de los gestos, tamaño en pantalla y alcance se comprueban instalando el APK en el teléfono real.
