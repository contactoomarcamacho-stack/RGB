# CAMACHO RGB CONTROL V2

Control Android de 24 botones con pruebas de códigos NEC, búsqueda de comandos y asignaciones persistentes. Mantiene el mismo identificador de aplicación; versionCode 2, versionName 2.0.

## Por qué la V1 podía abrir sin controlar las luces

La V1 emitía únicamente los bytes `00 FF comando complemento`, con cada byte LSB primero y portadora fija de 38 kHz. Tener permiso TRANSMIT_IR y abrir la app no demuestra que esos bytes coincidan con el receptor. Tampoco que Android acepte el envío demuestra una respuesta de las luces. No se puede afirmar la causa física sin probar el teléfono y el receptor.

No había selección de familia, frecuencia, orden de bits ni asignaciones personalizadas. La V2 conserva ese perfil y agrega alternativas. Son candidatos; no se promete compatibilidad universal.

## Instalar y encontrar el código

1. Subí el contenido del proyecto V2 a la raíz del repositorio, reemplazando los archivos existentes e incluyendo `.github/workflows/android.yml`. Después abrí **Actions → Compilar APK V2**, elegí una ejecución exitosa y descargá **CAMACHO-RGB-CONTROL-V2** en Artifacts. Extraé e instalá `app-debug.apk`. El ZIP de código fuente no es un APK instalable.
2. Si Android no permite actualizar por diferencia de firma de depuración, desinstalá la V1 e instalá V2. Esto borra los datos locales anteriores. Para actualizaciones estables se necesita una firma de lanzamiento permanente.
3. Revisá el diagnóstico de emisor IR. Apuntá al receptor de las luces a corta distancia. Probá ON y los colores en los tres perfiles.
4. Si no responde, elegí la dirección de búsqueda. Empezá en 38 kHz. Tocá **PROBAR SIGUIENTE CÓDIGO** o **INICIAR / CONTINUAR BÚSQUEDA**. Se recorren los 256 comandos de esa dirección, con una pausa de 1,5 segundos tras cada envío. La búsqueda puede apagar, cambiar de color o activar efectos.
5. Cuando veas una respuesta, tocá **PAUSAR**. Revisá el último código y usá **REPETIR ÚLTIMO CÓDIGO** para confirmar. Si ya pasó al siguiente código, reiniciá y avanzá manualmente hasta el que respondió. No hay detección automática de respuesta.
6. Elegí la función que realmente hicieron las luces (ON, ROJO, etc.) y tocá **FUNCIONÓ: ASIGNAR AL BOTÓN ELEGIDO**. Confirmá para guardar. Repetí para cada función necesaria. Encontrar un comando no identifica automáticamente los demás.
7. Si termina sin respuesta, cambiá de dirección y repetí. También podés probar 36 o 40 kHz si el emisor las admite. Cambiar dirección o frecuencia reinicia el recorrido. No es una búsqueda exhaustiva de todos los protocolos/direcciones.

Cada asignación guarda trama, frecuencia y orden de bits independientemente del perfil. Mantené pulsado un botón para borrar su asignación. Perfil y frecuencia seleccionados se conservan al salir. La búsqueda se pausa al salir de la aplicación; tras recrear la actividad el recorrido y último envío empiezan de nuevo.

## Códigos y orden de bits

- V1: reproduce los bytes y la tabla originales.
- V1 con bits invertidos: invierte los bits del comando y recalcula su complemento; es una alternativa de prueba.
- RGB F7: candidato de control de 24 teclas. El código ON mostrado como `00F7C03F` en orden MSB equivale a bytes `00EF03FC` enviados LSB primero. La dirección `00EF` en pantalla no es un error: se invierte cada byte para conservar la señal física.
- Búsqueda: `00FF`, `00EF`, `00F7`, `FF00`, `0000`, siempre bytes LSB y comando de 00 a FF con complemento.
- Manual: exactamente ocho dígitos hexadecimales. Seleccioná bytes LSB o bits MSB según la representación de la fuente. El ejemplo `00F7C03F` usa MSB. No acepta Pronto, tramas de longitud distinta ni otros protocolos.

Se genera una trama NEC completa de 67 duraciones en microsegundos, con cabecera 9000/4500, marcas de 560 y espacios de 560/1690. Los envíos son secuenciales en un hilo de trabajo, sin bloquear la interfaz ni acumular pulsaciones. El barrido se detiene ante errores. Una señal que ya empezó a transmitirse puede terminar después de pulsar PAUSAR o salir de la app.

## Requisitos y límites

Emisor físico accesible mediante Android `ConsumerIrManager`, permiso `TRANSMIT_IR`, receptor de luces IR compatible y línea de vista. No controla Bluetooth, Wi-Fi o RF; no recibe ni copia el mando original. Si el fabricante no informa las frecuencias, la app avisa y permite probar; si informa rangos, rechaza frecuencias fuera de ellos.

Referencia: https://developer.android.com/reference/android/hardware/ConsumerIrManager

## Compilar y verificar

GitHub Actions fija Java 17 y Gradle 8.9 para AGP 8.7.3, prepara Android SDK, ejecuta las pruebas de pulsos y `gradle assembleDebug lintDebug`, y adjunta el APK. También admite **Run workflow** eligiendo la rama donde subiste V2. La rama principal `main` compila con cada cambio; las propuestas de cambios también compilan.

Pruebas de codificación sin Android: `sh tests/run.sh`. Comprueban los 256 comandos, complemento NEC, cabecera y duración, equivalencia de representaciones MSB/LSB, perfiles y entrada hexadecimal. La validación real del alcance, hardware y función de cada comando requiere probar en las luces del usuario.
