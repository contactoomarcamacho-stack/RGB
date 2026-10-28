# Validación de la rueda LED 4.0

Se conserva IrCodes y el perfil F7 a 40 kHz que el usuario confirmó físicamente. Se sustituye el mando de 24 círculos por la referencia nueva: aro de colores táctil, brillo, encendido, apagado, efectos y blanco. TV no tiene acción.

WheelPaletteTest recorre ángulos positivos y negativos, verifica los sectores de la referencia, la interpolación circular corta, el rango de matiz y que los 15 comandos de color resultan accesibles sin emitir comandos de efectos o alimentación desde la rueda. Las pruebas NEC existentes siguen verificando los pulsos y complementos.

La emisión conserva únicamente el último comando pendiente durante un envío; mantiene una separación mínima aproximada de 120 ms entre comienzos. Se detiene la repetición de brillo al salir de la actividad. No se agrega acceso a red ni permisos adicionales.

Pendiente de prueba física de esta versión: respuesta a arrastre lento y rápido, color final al levantar el dedo, brillo mantenido, disposición en el teléfono del usuario y botón TV sin acción. El estado visible no es confirmación recibida de las luces.
