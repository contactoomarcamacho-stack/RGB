# Validación del control LED

El usuario confirmó que RGB F7 a 40 kHz controla el encendido, apagado y colores de su tira LED en V2. Esta versión conserva esas señales y cambia la interfaz por una réplica de la distribución del mando fotografiado. La bombilla queda fuera del alcance solicitado.

Las clases Java se verifican contra API Android 35; IrCodesTest comprueba la codificación NEC. El workflow de GitHub ejecuta las pruebas, assembleDebug y lintDebug antes de adjuntar el APK.

Pendiente de prueba en el teléfono: aspecto visual, tamaño de botones, modo inmersivo y funcionamiento físico de cada tecla tras instalar la versión 3.1. La interfaz se dibuja con formas vectoriales y botones nativos; no utiliza la fotografía como superficie táctil.

Ajuste de pantalla 3.1: se elimina la proporción fija 0,62 de la carcasa. RemoteGeometry distribuye las 24 teclas en todo el ancho y alto seguros, manteniendo botones circulares. RemoteGeometryTest cubre nueve tamaños en ambas orientaciones, variantes con márgenes de sistema, límites y ausencia de superposición. No se modifican MainActivity, IrCodes ni el envío IR respecto a 3.0.
