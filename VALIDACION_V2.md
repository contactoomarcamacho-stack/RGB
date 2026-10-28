# Validación 5.2

Se elimina completamente la interfaz y el código de control por voz, incluida la clase VoiceCommands. El mando LG conserva nueve iconos, sin textos visibles; silencio se centra abajo en vertical. Se mantienen adaptación de pantalla, control LED, códigos IR LG y configuración opcional del puntero por movimiento.

LgLayoutTest verifica límites y ausencia de superposición de los nueve iconos en nueve tamaños, con y sin márgenes del sistema. Se conservan las pruebas NEC, LED, LG, sensores y direcciones locales. El workflow compila assembleDebug y ejecuta lintDebug.

Pendiente: instalar esta actualización en el teléfono y confirmar la nueva distribución sin micrófono.
