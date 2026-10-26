# Seguros BPO — Entrega Android DSY1105

Aplicación Android de seguimiento de siniestros con tres perfiles de demostración: **Asegurado**, **Liquidador** y **Analista**. Es un proyecto académico con datos locales ficticios.

## Tecnología y alcance

- Kotlin, Jetpack Compose y Material 3.
- Arquitectura MVVM, ViewModel/StateFlow y Navigation Compose.
- SQLiteOpenHelper para cuentas, siniestros, historial, documentos y avisos.
- Cámara, galería y selector de documentos del sistema.
- Todo funciona localmente. **No hay backend Java, Spring Boot, Retrofit, API REST ni microservicios en esta entrega.**

## Funcionalidades

- Crear cuenta o entrar con usuarios de demostración y separar los datos por perfil.
- Reportar siniestros de Vehículos u Hogar con validaciones y número identificador.
- Consultar, buscar y editar casos; ver etapa, próximo paso, historial y documentos.
- Adjuntar imágenes y archivos PDF con límites de tipo, tamaño y cantidad.
- Liquidadores: tomar casos por especialidad, registrar gestiones, solicitar documentos, cambiar etapas y cerrar casos según las reglas.
- Avisos locales, perfiles y estadísticas.

## Abrir y ejecutar

1. Clonar esta rama o descargarla en ZIP desde GitHub; abrir **la carpeta raíz** en Android Studio (donde está `settings.gradle.kts`).
2. Sincronizar Gradle con Java 25 y Android SDK 37.2, tal como utiliza la CI.
3. Ejecutar el módulo `app` en un teléfono o emulador. El paquete debug es `cl.duocuc.segurosbpo.prueba`.
4. Usar el acceso demo o crear datos ficticios; consultar [Cuentas de demostración](docs/CUENTAS_DEMO_QA.md).

Para compilar y correr las pruebas unitarias de dominio:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest --no-daemon
```

Las pruebas unitarias están en `app/src/test/`. La batería extendida de pruebas instrumentadas y los ZIP de referencia académica **no se copian a esta rama de entrega**.

## Verificación

La rama `otro` utiliza las técnicas de la aplicación de referencia `MiRegistroAvatar`: Compose, ViewModel, StateFlow, navegación y SQLiteOpenHelper.

Las pruebas JUnit se ejecutan separadamente de la aplicación. La compilación no incorpora instrumentación de cobertura ni configuración de firma de QA.

El flujo Android Entrega compila la APK, ejecuta las pruebas unitarias y comprueba instalación y arranque en Android API 30 y 35. Cada resultado debe consultarse en la ejecución del commit correspondiente. Estas comprobaciones no sustituyen la demostración manual de cámara, galería y los recorridos de negocio.

Los documentos antiguos de `docs/` conservan revisiones históricas; sus resultados y pendientes corresponden a las versiones indicadas en cada uno.

## Documentación para presentar

- [Guía maestra DSY1105](docs/GUIA_MAESTRA_DSY1105.md) — requisitos y pauta.
- [Guía de defensa oral](docs/GUIA_DEFENSA_ORAL.md).
- [Polimorfismo y animaciones](docs/POO_ANIMACIONES_EN_LA_DEFENSA.md).
- [Auditoría de la pauta](docs/AUDITORIA_PAUTA_DSY1105_2026-10-24.md) — riesgos y evidencias, evaluación docente pendiente.
- [Cómo ejecutar Android](docs/COMO_EJECUTAR_ANDROID.md) — contiene instrucciones de QA histórico; para esta entrega usar las indicaciones superiores.

La rama de entrega se prepara sin modificar `main`, `qa` ni `entrega-01`. No es una declaración de nota 100/100; esa evaluación corresponde al docente.
