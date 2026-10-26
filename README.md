# CAMACHO RGB CONTROL · control LED

Aplicación Android con una sola pantalla que reproduce la distribución del control de 24 teclas de la foto: carcasa clara, paneles grises, botones circulares de colores, brillo, OFF/ON, R/G/B/W y FLASH/STROBE/FADE/SMOOTH. Sin título, menús, búsquedas ni ajustes en la pantalla.

## Configuración

Versión 3.1 (versionCode 4), mismo applicationId `com.camacho.rgbcontrol`, Android 6 o posterior. Usa el perfil RGB F7 a 40 kHz que el usuario confirmó que controla su tira LED. Las tramas y asignación de las 24 teclas se conservan de V2. Si existían asignaciones individuales guardadas en V2, se respetan al actualizar conservando los datos.

Diseñada para la tira LED; no se declara compatibilidad con la bombilla. Requiere emisor físico accesible mediante Android ConsumerIrManager y apuntar al receptor. Un aviso temporal aparece solo ante errores de emisión; no hay mensajes de estado permanentes. No requiere red ni incorpora permisos adicionales.

La interfaz utiliza botones nativos con nombres accesibles, respuesta visual al pulsar e iconos vectoriales para brillo. La carcasa ocupa el 98 % del ancho y alto disponibles, sin imponer la proporción fija del mando físico. Los 24 botones conservan su forma circular y se distribuyen en cuatro columnas y seis filas, respetando los márgenes de las barras del sistema; oculta las barras durante el uso normal mediante modo inmersivo.

## Descargar APK

En la ejecución exitosa más reciente de **Actions → Compilar APK LED**, descargá el artefacto **CAMACHO-RGB-CONTROL-LED**. Extraé `app-debug.apk` e instalalo. Si Android rechaza la actualización por una firma de depuración diferente, desinstalar la versión anterior permite instalarla, pero elimina los datos y códigos personalizados. Para actualizaciones permanentes hace falta una firma de lanzamiento estable.

## Desarrollo y validación

Java 17, Gradle 8.9, Android Gradle Plugin 8.7.3, compileSdk/targetSdk 35 y minSdk 23. GitHub Actions ejecuta `sh tests/run.sh` y `gradle assembleDebug lintDebug`, y adjunta el APK. El workflow admite propuestas de cambios a main y ejecución manual.

Las pruebas verifican tamaños de teléfono y tablet en vertical y horizontal, márgenes, límites y ausencia de superposición de las 24 teclas. También verifican los 256 comandos NEC, complementos, cabecera/duraciones, perfiles, equivalencia MSB/LSB y entrada hexadecimal. Referencia de emisión: https://developer.android.com/reference/android/hardware/ConsumerIrManager

La validación final de tamaño, tacto y respuesta de cada función requiere instalar esta versión en el teléfono. No se incorporan cambios de protocolo respecto al perfil ya probado por el usuario.
