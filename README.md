# CAMACHO RGB CONTROL
Aplicación Android de control IR RGB de 24 botones. Requiere un emisor IR accesible mediante Android ConsumerIrManager. Los códigos NEC incluidos son candidatos de prueba, no se garantiza compatibilidad con todos los controladores RGB.

## Compilar en GitHub
1. Crear un repositorio nuevo en GitHub.
2. Subir **el contenido de esta carpeta** a la raíz del repositorio, incluida `.github/workflows/build-apk.yml`.
3. Abrir **Actions → Compilar APK → Run workflow**.
4. Al finalizar, descargar el artefacto **CAMACHO-RGB-CONTROL-APK** y extraer `app-debug.apk`.
5. Instalar el APK en el teléfono y probar apuntando al receptor de las luces.

Si el teléfono no expone el emisor IR o los códigos del control original son diferentes, la aplicación no podrá controlar esas luces sin adaptar los códigos o el hardware.
