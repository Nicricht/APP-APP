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
- [ ] Confirmar ejecución real en Android Studio o emulador (depende de CI y prueba de dispositivo).

## Recorrido que implementamos

1. Abrir aplicación → `Mis siniestros`.
2. Elegir un asegurado ficticio. La selección se conserva localmente.
3. Pulsar `Reportar` y completar el formulario.
4. Validar fecha, compañía, comuna, descripción y confirmación de datos.
5. Guardar el siniestro en SQLite y mostrar el número generado.
6. Ir a `Mis siniestros` y abrir el `Detalle`.
7. Si está en etapa `Recibido`, corregir su descripción, siguiendo el patrón de pantalla de edición del profesor.

**Restricción de la primera etapa:** datos guardados en el propio teléfono; no hay sincronización entre dispositivos ni servicios remotos. El historial, cambios de etapa, notificaciones y documentos no están todavía desarrollados.

## Próximas tareas, ordenadas por pauta y referencia del profesor

- [ ] Integrar **cámara y galería** para evidencias, replicando el uso didáctico de permisos y Activity Result en `PerfilScreen` del profesor.
- [ ] Guardar o referenciar la evidencia localmente y asociarla al siniestro, con validaciones.
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
| Recursos nativos | 15 | Todavía no conectados a Seguros BPO | Cámara + galería |

No se declara obtenida ninguna calificación: la nota la determina la pauta del docente tras evaluar la aplicación funcionando.

## Comprobación

- Los modelos y validadores Kotlin fueron compilados localmente con `kotlinc`.
- La compilación Android y las pruebas unitarias se ejecutan mediante `.github/workflows/android-qa.yml`; comprobar su estado antes de fusionar.
- Sin SDK Android local no se ha ejecutado el emulador aquí.
- No se ha reemplazado el ZIP de origen ni se ha modificado `main`.

## Instrucción al compañero

Trabajar desde una rama independiente y no sobrescribir sin revisar las nuevas pantallas o el `SiniestroDbHelper`. Registrar issues o tarjetas Trello por funcionalidad y acompañar los PR con evidencia de pruebas.
