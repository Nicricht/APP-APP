# APP-APP: Seguimiento de Siniestros

Proyecto de Desarrollo de Aplicaciones Móviles (DSY1105). Es una aplicación Android para registrar y consultar siniestros de seguros con datos ficticios.

## Plan de finalizacion y control QA (23-10-2026)

Para continuar el desarrollo, consultar estos documentos vigentes en la rama `qa`:

- [Plan maestro para terminar la app](docs/PLAN_MAESTRO_FINALIZACION.md): roles Asegurado/Liquidador, cuentas demo, asignacion por area, tomar caso, flujos, arquitectura, mejoras visuales y backlog BPO-00 a BPO-12.
- [Protocolo QA obligatorio](docs/PROTOCOLO_QA_OBLIGATORIO.md): todo cambio se integra y prueba en `qa` antes de proponer una promocion; `main` y `entrega-01` permanecen intactas.
- [Matriz de aceptacion y pruebas](docs/MATRIZ_ACEPTACION_Y_PRUEBAS.md): casos para permisos, migraciones, documentos, estados, graficos, Android, APK y recorrido completo.

**Importante:** estos son planes de implementacion, no funcionalidades ya terminadas. El QA instrumentado del commit auditado del 23-10-2026 estaba fallando y debe corregirse primero. Registro/login y boton `Tomar caso` son ampliaciones acordadas respecto de la identificacion simulada y la asignacion por analista opcional del caso original.

## Herramientas utilizadas

- Android Studio y Kotlin.
- Jetpack Compose y Material Design 3 para las pantallas.
- MVVM con ViewModel y StateFlow.
- Navigation Compose para cambiar entre pantallas.
- SQLiteOpenHelper para guardar la información en el teléfono.
- Cámara y galería con ActivityResultContracts.

La estructura y los componentes siguen las actividades realizadas en `MiRegistroAvatar`.

## Funcionalidades

- Selección de un asegurado ficticio.
- Registro de siniestros y número de identificación.
- Lista de siniestros y búsqueda.
- Consulta del detalle y corrección de la descripción.
- Historial de movimientos registrados.
- Adjuntar fotografías, imágenes y archivos PDF.
- Perfil simulado de liquidador, gestiones y cambios de etapa.

Los datos se almacenan localmente. En esta evaluación no se usan microservicios, Retrofit ni Spring Boot.

## Ejecución

1. Abrir el proyecto en Android Studio desde la carpeta que contiene `settings.gradle.kts`.
2. Esperar la sincronización de Gradle.
3. Seleccionar un emulador o conectar un teléfono Android.
4. Ejecutar `app`.

Las instrucciones detalladas están en [Cómo ejecutar Android](docs/COMO_EJECUTAR_ANDROID.md).

## Organización del repositorio

- [`main`](../../tree/main): aplicación para presentar, siguiendo el ejemplo del profesor.
- [`qa`](../../tree/qa): pruebas unitarias adicionales, integración SQLite y pruebas de navegación Android.

Las comprobaciones se pueden ver en [GitHub Actions](../../actions). La compilación correcta no sustituye las pruebas de cámara y galería en un dispositivo.

## Documentación

- [Control final de los 100 puntos](docs/CONTROL_FINAL_100_PUNTOS.md).
- [Guía para explicar el proyecto en la presentación](docs/GUIA_DEFENSA_ORAL.md).
- [Guía completa del caso y pauta](docs/GUIA_MAESTRA_DSY1105.md).
- [Estado de avance](docs/ETAPA_1_ANDROID.md).
- [Cómo ejecutar el proyecto](docs/COMO_EJECUTAR_ANDROID.md).

## Pendiente de revisión final

- Probar todos los recorridos en un emulador o teléfono.
- Verificar permisos, documentos y migraciones de SQLite.
- Terminar las funcionalidades locales que se acuerden para esta evaluación.
- Reunir las evidencias de GitHub y Trello de ambos integrantes.
