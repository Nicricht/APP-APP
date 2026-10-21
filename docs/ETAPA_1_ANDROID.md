# Etapa 1 de desarrollo Android: evaluación actual

**Repositorio:** Nicricht/APP-APP  
**Rama de trabajo:** `feature/android-local-mvvm`  
**Referencia de clases:** `MiRegistroAvatar` del profesor.  
**Alcance acordado:** solo Android, Kotlin, Jetpack Compose, Material Design 3, MVVM, Navigation Compose y SQLite local.

> **IMPORTANTE:** Spring Boot, Retrofit, API REST y microservicios se dejan para la **tercera evaluación**. No forman parte de esta etapa. El README maestro conserva esos requisitos para más adelante, pero no deben introducirse ahora.

## Cambios desarrollados en la etapa 1

- [x] Extraer y versionar como archivos el proyecto Android original que estaba comprimido en un ZIP.
- [x] Mantener componentes, recursos, formulario y estilo base del proyecto.
- [x] Añadir modelos `Siniestro` y `EtapaSiniestro`.
- [x] Añadir navegación al estilo de `AppNavegacion.kt` y `Rutas.kt` del profesor.
- [x] Crear `MisSiniestrosScreen`, `DetalleSiniestroScreen` y `EditarSiniestroScreen`.
- [x] Implementar `SiniestroDbHelper` con `SQLiteOpenHelper` para persistencia.
- [x] Implementar `SiniestrosViewModel` con `StateFlow` y selección de asegurado ficticio.
- [x] Conectar `FormularioScreen` a SQLite para registrar casos de verdad.
- [x] Generar identificador desde el ID de SQLite, en vez de reiniciar un contador del formulario.
- [x] Extraer las validaciones de campos a `ValidadorSiniestro`.
- [x] Añadir pruebas unitarias para fecha, compañía y campos obligatorios.
- [x] Configurar GitHub Actions para compilar y ejecutar tests.
- [ ] Confirmar ejecución real en Android Studio o emulador (la compilación de GitHub Actions no reemplaza la prueba de hardware).

## Recorrido que implementamos

1. Abrir aplicación → `Mis siniestros`.
2. Elegir un asegurado ficticio. La selección se conserva localmente.
3. Pulsar `Reportar` y completar el formulario.
4. Validar fecha, compañía, comuna, descripción y confirmación de datos.
5. Guardar el siniestro en SQLite y mostrar el número generado.
6. Ir a `Mis siniestros` y abrir el `Detalle`.
7. Si está en etapa `Recibido`, corregir su descripción, siguiendo el patrón de pantalla de edición del profesor.

**Restricción de la primera etapa:** datos guardados en el propio teléfono; no hay sincronización entre dispositivos ni servicios remotos. El historial, cambios de etapa y notificaciones no están todavía desarrollados. Los documentos se guardan localmente, sin sincronización.

## Próximas tareas, ordenadas por pauta y referencia del profesor

- [x] Integrar **cámara y galería** para evidencias, con Activity Result y permiso de cámara como en `PerfilScreen` del profesor.
- [x] Guardar **imágenes y PDF como BLOB en SQLite**, asociados al siniestro, con un tipo de documento obligatorio y validaciones. La tabla nueva se crea migrando de BD v1 a v2, sin eliminar los siniestros.
- [ ] Completar pantallas y operaciones necesarias del alcance actual, sin funcionalidades de una evaluación posterior.
- [ ] Reforzar validaciones visuales y pruebas de flujos de navegación y SQLite.
- [ ] Añadir animaciones discretas si se necesitan para cumplir el criterio de la pauta, sin inventar pantallas.
- [ ] Ejecutar prueba de instalación y uso en Android Studio o teléfono.
- [ ] Registrar issues, tarjetas Trello, PR y contribuciones auténticas de ambos integrantes.

## Evidencia por criterio de los 100 puntos

| Criterio | Máximo | Avance concreto de etapa 1 | Falta |
|---|---:|---|---|
| UI y navegación | 15 | Rutas y pantallas nuevas | Prueba de navegación y refinamiento |
| Formularios | 15 | Formulario existente conectado a SQLite y validaciones | Probar errores y formularios pendientes |
| Validaciones centralizadas | 10 | Validador separado + ViewModel y StateFlow | Pruebas de integración |
| Animaciones | 10 | Todavía no desarrolladas | Animaciones útiles |
| POO y persistencia | 15 | Modelo, SQLiteOpenHelper y ViewModel | Pruebas SQLite y relación con evidencia |
| Colaboración | 20 | Rama y commits trazables en GitHub | Trello, issues, commits propios de ambos |
| Recursos nativos | 15 | Cámara + galería/selector PDF integrados en la pantalla de evidencias | Falta prueba en dispositivo/emulador y evidencia visual |

No se declara obtenida ninguna calificación: la nota la determina la pauta del docente tras evaluar la aplicación funcionando.

## Comprobación

- Los modelos y validadores Kotlin fueron compilados localmente con `kotlinc`.
- La compilación Android y las pruebas unitarias se ejecutan mediante `.github/workflows/android-qa.yml`; comprobar su estado antes de fusionar.
- Sin SDK Android local no se ha ejecutado el emulador aquí.
- No se ha reemplazado el ZIP de origen ni se ha modificado `main`.

## Instrucción al compañero

Trabajar desde una rama independiente y no sobrescribir sin revisar las nuevas pantallas o el `SiniestroDbHelper`. Registrar issues o tarjetas Trello por funcionalidad y acompañar los PR con evidencia de pruebas.


## Actualización: cámara, galería y PDF (21-10-2026)

- Nueva pantalla `EvidenciasScreen.kt`, accesible desde cada detalle de siniestro.
- Fotografía con `TakePicturePreview` y permiso `CAMERA` como en el proyecto del profesor. La captura es una **vista previa** (resolución reducida), no una foto de resolución completa.
- Imagen con `GetContent("image/*")` y PDF con `GetContent("application/pdf")`. El selector del sistema concede acceso al archivo elegido, así que no se solicitan permisos amplios de almacenamiento.
- `ProcesadorEvidencia.kt` valida el MIME y comprueba cabeceras básicas del archivo; lectura con límite de 5 MB.
- `SiniestroDbHelper.kt` versión 2: mantiene casos previos y añade tabla `evidencias` con BLOB, FK hacia siniestro y filtro por asegurado ficticio. Máximo 15 por caso, tipo obligatorio y sin adjuntar después del cierre.
- `ValidadorEvidenciaTest.kt`: casos límite de tamaño, formato, documento obligatorio, cantidad 15 y 16.
- **Pendiente de validación:** ejecución en cámara real, navegación completa, permisos rechazados y almacenamiento persistente en emulador/teléfono. El resultado de QA automatizado debe verificarse antes de fusionar.
- **No hacer todavía:** Retrofit, Spring Boot, microservicios, subida de archivos remota ni notificaciones push de la tercera evaluación.
