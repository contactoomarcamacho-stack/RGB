# Estado de la revisión V2

Base revisada: `contactoomarcamacho-stack/RGB`, rama `main`, commit `5849a63501692bd725cac322ec13f9cd9cec7951`.

## Verificado

- Lectura de MainActivity, manifiesto, configuración de Gradle y workflow de la V1.
- La V1 ya incluye TRANSMIT_IR. No se identificó un fallo que permita asegurar la causa física: el único perfil NEC puede no coincidir con el receptor.
- Compilación de las dos clases de producción y de las pruebas Java con Eclipse ECJ 3.37.0, Java 17 y clases de API Android 35. Se excluyeron las clases Java estándar del jar Android para evitar conflicto con los módulos del runtime anfitrión.
- Ejecución exitosa de `IrCodesTest`: decodificación independiente de los pulsos para los 256 comandos, complementos, cabecera, longitud y duración, perfiles, equivalencia MSB/LSB y validación hexadecimal.
- `git diff --check` sin errores.

## Pendiente

- Compilación del APK y Android Lint mediante el workflow incluido. Este entorno no tiene Android SDK/Gradle completos configurados. La comprobación de Java no equivale a un APK compilado.
- Prueba en el teléfono real: detección de emisor y rangos, búsqueda, pausa, asignación de botones, persistencia tras cerrar la app y respuesta física de las luces.

## Cambios de compilación

VersionCode 2 / versionName 2.0; conserva applicationId y minSdk 23. Target SDK 35 y ajuste de márgenes según las barras del sistema. Java 17, Gradle 8.9 y AGP 8.7.3 en CI. No se cambiaron dependencias de la aplicación ni se agregaron permisos.
