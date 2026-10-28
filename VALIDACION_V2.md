# Validación 5.0

Se conserva el control LED previamente probado. TV ahora abre un control LG con nueve acciones IR comunes. El puntero por movimiento es opcional y experimental, requiere giroscopio, webOS, red local, certificado aceptado por el usuario y autorización en la pantalla de la TV. No hay emulación Bluetooth/RF.

Pruebas automáticas: NEC y complementos, equivalencia de potencia y volumen LG con códigos MSB conocidos, validación de IP privada (rechazo de hosts públicos, localhost, direcciones inválidas y URLs), integración del giroscopio (reposo, ruido, ejes, compensación de inclinación, límites y muestras obsoletas), rueda LED y pruebas de geometría existentes. Compilación y Lint mediante GitHub Actions.

Validación física pendiente: todas las teclas LG en el modelo real; emparejamiento WSS y permisos webOS; certificado presentado por la TV; conexión al socket de puntero; movimiento, sensibilidad, inclinación, clic y parada al salir. No se afirma compatibilidad garantizada ni que los sensores solos permitan controlar un TV sin conexión de datos.
