# Ejecutar Seguros BPO en Android Studio

## Alcance
Versión local de la primera evaluación, inspirada en `MiRegistroAvatar`: **Kotlin + Jetpack Compose + MVVM + SQLite + cámara + galería**. No usa Spring Boot, Retrofit, API REST ni microservicios.

## Preparar el proyecto

1. En GitHub, abrir [APP-APP](https://github.com/Nicricht/APP-APP) en rama `main`.
2. Clonar o descargar **el proyecto descomprimido** del repositorio. No abrir directamente el archivo `Proyecto_Seguros_BPO-main.zip`, porque ese ZIP conserva la versión original anterior a los cambios.
3. En **Android Studio → Open**, elegir la carpeta que contiene `settings.gradle.kts` y `gradlew`.
4. Permitir **Gradle Sync** e instalar los SDK requeridos por el proyecto, incluido Android 37.2 si Android Studio lo solicita.
5. Ejecutar `app` en un emulador Android compatible o teléfono conectado.
6. A la primera ejecución, la aplicación crea la BD `seguros_bpo.db`, tablas `siniestros` y `evidencias`, y cinco **siniestros ficticios de demostración** para enseñar las cuatro etapas. **No borra casos al actualizar una versión anterior.**
7. Elegir uno de los asegurados ficticios. Buscar por número, compañía, tipo o comuna; entrar al detalle.
8. Pulsar **Reportar**, completar fecha, compañía activa, comuna y descripción. Al registrar se genera un número `SIN-AA-NNNNNN` guardado en SQLite.
9. Volver a **Mis siniestros**, abrir el nuevo caso, pulsar **Documentos y evidencias**.
10. Indicar tipo de documento y probar **Tomar fotografía**, **Elegir de la galería** o **Adjuntar PDF**. Se guardan localmente, sin subirlos a ningún servidor.
11. En la sección de evidencias, pulsar **Ver imagen** para abrir la foto almacenada. Los PDF se guardan en la base local, sin visor interno.
12. Cerrar y reabrir la aplicación para comprobar persistencia; cambiar asegurado y verificar que no aparezcan los casos de otro.

## Pruebas y artefacto APK

GitHub Actions ejecuta automáticamente:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest --no-daemon
```

Consultar [GitHub Actions](https://github.com/Nicricht/APP-APP/actions). Una ejecución exitosa publica el artefacto **SegurosBPO-debug-apk**, que contiene `app-debug.apk`.

## Limitaciones y comprobaciones pendientes

- Es un **MVP académico** sin usuarios ni pólizas reales.
- Selección de asegurado **simulada**, no es autenticación productiva.
- Sin Internet, la información persistida localmente puede consultarse.
- Los cambios y evidencias no se sincronizan con otro teléfono.
- La cámara utiliza `TakePicturePreview`, una captura de tamaño de vista previa.
- Requiere probar en dispositivo/emulador los permisos de cámara, galería y PDF, navegación, reinicio, migración de BD y accesibilidad.
- No afirmar cumplimiento de todos los criterios de la pauta sin evidencia del docente, el compañero y las pruebas en teléfono.

La guía completa de requisitos se conserva en [README.md](../README.md). El registro de cambios está en [ETAPA_1_ANDROID.md](ETAPA_1_ANDROID.md).

## Historial y rol liquidador local

1. Desde **Mis siniestros**, abre un caso y entra a **Ver historial**. En los nuevos casos se registran el reporte, las correcciones y los documentos adjuntados.
2. En la lista principal pulsa **Liquidador**; es una selección ficticia como los roles del proyecto del profesor, no un login real.
3. Elige el equipo **Vehículos** o **Hogar**. Solo se listan sus casos asignados.
4. Abre un caso en evaluación o liquidación. Escribe una gestión y marca si puede verla el asegurado; pulsa **Registrar gestión**.
5. Comprueba que solo aparece en el historial del asegurado si se marcó como visible.
6. Usa **Confirmar avance** para ir a la siguiente etapa. Si vas a cerrar, elige **Aprobado** o **Rechazado** y escribe el mensaje obligatorio.
7. Vuelve a la vista del asegurado para revisar la nueva etapa, la última actualización y el resultado de cierre.
8. Reabre la aplicación para comprobar que las gestiones y las etapas sobreviven al reinicio.

**Base de datos:** SQLite está en versión 4. Se conservan las tablas y registros anteriores mediante migraciones incrementales. Los casos existentes sin gestión histórica reciben una anotación informativa, sin inventar eventos previos.

**Advertencia:** los casos nuevos quedan «Recibido» sin liquidador, como indica el caso académico. La asignación por analista y las solicitudes de documentos pendientes no están implementadas. No dar por terminadas esas funciones ni el bloqueo RN-09 de punta a punta.
