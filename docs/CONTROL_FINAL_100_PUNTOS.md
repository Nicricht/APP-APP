# Control final de evaluación: APP-APP

**Proyecto:** Seguimiento de siniestros de Servicios Corporativos Andes SpA  
**Evaluación actual:** aplicación Android local, usando el ejemplo `MiRegistroAvatar`.  
**Ramas:** `main` (entrega Android) y `qa` (pruebas adicionales).  
**Revisión:** 21-10-2026.

Este documento cruza la **pauta de siete criterios y 100 puntos** proporcionada por el estudiante con el código actualmente disponible en `main`, el ZIP original `MiRegistroAvatar` y los resultados de GitHub Actions. **No es una nota anticipada** ni sustituye la evaluación del docente.

## 1. Resumen de la pauta (100 puntos)

| Criterio de la pauta | Máximo | Evidencia verificada en código | Estado y riesgo antes de presentar |
|---|---:|---|---|
| Interfaz, estructura y navegación | **15** | `ui/screens/`, `navigation/AppNavegacion.kt`, `navigation/Rutas.kt`; pruebas Compose en `qa` | **Parcialmente comprobado.** Falta revisar tamaño de pantalla, accesibilidad, retroceso y recorrido visual completo. |
| Formularios y validaciones visuales | **15** | Reporte, edición de descripción, gestión de liquidador, cierre y adjuntos con mensajes | **Parcial.** Faltan solicitudes de documentos y pruebas visuales de todos los errores. |
| Validaciones centralizadas y reactividad | **10** | `FormularioUiState`, `FormularioViewModel`, `SiniestrosViewModel`, `ValidadorSiniestro`, `ValidadorEvidencia`, `ReglasEtapa` | **Avanzado, no certificado al máximo.** Probar transiciones y revisar reglas pendientes. |
| Animaciones funcionales | **10** | `AnimatedVisibility`, `fadeIn`, `expandVertically` para registro exitoso | **Parcial.** Una animación funcional no demuestra todas las situaciones solicitadas por la pauta. |
| Clases, herencia y polimorfismo | **15** | `data class`, `enum class`, `ViewModel`, `AndroidViewModel`, `SQLiteOpenHelper`, métodos `override` | **Riesgo alto.** Hay herencia del framework; no se ha verificado un ejemplo explícito de polimorfismo entre tipos propios del negocio. No atribuirlo al proyecto del profesor si él no lo implementó. |
| GitHub, Trello y colaboración | **20** | Repositorio, ramas, commits, documentación, issues y CI en GitHub | **Pendiente de evidencia humana.** Acreditar tareas y contribuciones reales de **ambos** integrantes y tablero Trello. No inventar autores ni commits. |
| Dos recursos nativos del teléfono | **15** | Cámara mediante `TakePicturePreview` y selector de imágenes con `GetContent`, más PDFs y SQLite | **Código y algunos flujos probados.** Falta evidencia de captura real, selección y denegación de permisos en el teléfono/emulador. |
| **Total** | **100** | | **Puntaje real: pendiente de evaluación docente.** |

**Convenciones:** «existe en código» no significa «probado visualmente»; «compila» no significa «funciona todo»; «prueba automatizada verde» no significa «100 % de cobertura».

## 2. Checklist que debemos marcar antes de entregar

### A. Interfaz y navegación · 15 puntos

- [x] Existen pantallas `MisSiniestros`, `Formulario`, `Detalle`, `Editar`, `Historial`, `Evidencias` y `CasosLiquidador`.
- [x] Se usan rutas de Navigation Compose y retorno entre pantallas.
- [x] Se usa Material 3 y hay componentes reutilizables como `CampoTexto`, `BotonPrincipal` y `TarjetaSeccion`.
- [x] Existen pruebas automatizadas básicas de navegación en la rama `qa`.
- [ ] Comprobar que todas las pantallas son legibles en un dispositivo pequeño, especialmente los tres controles horizontales de `MisSiniestrosScreen`.
- [ ] Verificar que al entrar y volver no aparezcan datos de otro asegurado.
- [ ] Probar orientación, teclado, scroll, textos largos y accesibilidad con lector de pantalla.
- [ ] Obtener capturas legibles de lista, registro, detalle, historial, documentos y liquidador.

### B. Formularios · 15 puntos

- [x] Registrar siniestro con compañía, tipo, fecha, comuna, descripción y confirmación.
- [x] Validar fecha imposible/futura, compañía inactiva, campos vacíos y confirmación.
- [x] Corregir descripción de casos recibidos.
- [x] Registrar gestión visible o interna; cerrar con resultado y mensaje.
- [x] Adjuntar archivos con tipo de documento obligatorio y límites definidos.
- [ ] Probar cada formulario con datos correctos, vacíos e inválidos en el emulador.
- [ ] Implementar o justificar según el alcance de esta evaluación la solicitud formal de documentación pendiente.
- [ ] Comprobar mensaje claro cuando falla el almacenamiento o se cancela una acción.
- [ ] Confirmar visualmente que ninguna acción guarda dos veces por doble pulsación.

### C. Validación centralizada y reactividad · 10 puntos

- [x] `FormularioViewModel` conserva el estado del reporte usando `StateFlow` y `copy`.
- [x] `SiniestrosViewModel` comparte las listas, filtros y cambios entre pantallas.
- [x] `ValidadorSiniestro`, `ValidadorEvidencia` y `ReglasEtapa` separan reglas de las pantallas.
- [x] La rama `qa` tiene tests de validadores, SQLite e interacciones.
- [ ] Comprobar que un caso editado aparece actualizado inmediatamente en lista y detalle.
- [ ] Resolver las reglas de negocio de documentos pendientes antes de afirmar que impiden avanzar.
- [ ] Revisar errores y concurrencia de guardado sin bloquear la interfaz.

### D. Animaciones · 10 puntos

- [x] La confirmación exitosa del registro aparece con `AnimatedVisibility`.
- [ ] Demostrar en el dispositivo que la animación es visible y no molesta.
- [ ] Evaluar una segunda animación sencilla y justificada solo si es necesaria para cumplir el nivel superior, sin incorporar otra arquitectura.
- [ ] Respetar configuraciones de accesibilidad y no usar animaciones decorativas excesivas.

### E. Clases, herencia y polimorfismo · 15 puntos

- [x] Explicar `data class Siniestro`, `FormularioUiState`, `Gestion` y `Evidencia`: agrupan información.
- [x] Explicar los `enum class` de etapas, asegurados, tipos y liquidador.
- [x] Mostrar **herencia real**: `FormularioViewModel : ViewModel`, `SiniestrosViewModel : AndroidViewModel`, `SiniestroDbHelper : SQLiteOpenHelper` y `MainActivity : ComponentActivity`.
- [x] Mostrar **sobrescritura**: `onCreate`, `onUpgrade`, `onCleared` y `onCreate` de la actividad.
- [ ] Confirmar con la pauta/docente qué forma de **polimorfismo** exige: reutilizar métodos de clases base de Android puede no demostrar polimorfismo de negocio.
- [ ] Si se exige, desarrollar un ejemplo pequeño, usado realmente y comprendido por ambos, sin jerarquías artificiales.

### F. GitHub, Trello y trabajo colaborativo · 20 puntos

- [x] Repositorio `Nicricht/APP-APP` y ramas `main`/`qa`.
- [x] GitHub Actions compila `main` y comprueba pruebas ampliadas en `qa`.
- [x] Hay un issue abierto relacionado con cámara/galería: [Issue #2](https://github.com/Nicricht/APP-APP/issues/2).
- [ ] Verificar y documentar **commits auténticos de ambos estudiantes**; las operaciones de un asistente no demuestran por sí solas la contribución individual.
- [ ] Mostrar tablero Trello real, con tareas asignadas a cada integrante, avance y tareas finalizadas.
- [ ] Relacionar tarjetas Trello, issues o commits con funcionalidades visibles.
- [ ] Cerrar el Issue #2 solo después de probar cámara y galería de verdad.
- [ ] Preparar capturas del historial y tablero para mostrar en la presentación.

### G. Dos recursos del dispositivo · 15 puntos

- [x] `EvidenciasScreen.kt`: cámara y galería/selector del sistema; procesamiento en `ProcesadorEvidencia.kt`.
- [x] SQLite asocia evidencia al ID del caso y verifica que el asegurado sea el propietario.
- [x] Hay pruebas de guardado/lectura de imágenes y límites en `qa`.
- [ ] En dispositivo/emulador, tomar una foto real y verla persistida.
- [ ] Elegir imagen desde la galería y verla persistida después de cerrar/reabrir.
- [ ] Denegar el permiso de cámara y confirmar que se muestra un aviso sin cerrar la aplicación.
- [ ] Cancelar cada selector sin error ni archivo huérfano.
- [ ] Seleccionar PDF válido e inválido; comprobar el mensaje y el límite de 5 MB.
- [ ] Guardar una captura de ambos recursos funcionando.

## 3. Riesgos funcionales del caso Seguros BPO

Esta sección no agrega puntos a la pauta de 100: distingue las funciones que pueden preguntar en la defensa del **caso oficial**.

| Función del caso | Situación actual |
|---|---|
| Reportar, identificar número, listar, buscar, detalle | Existe en `main`; QA Android aprobado en recorridos seleccionados |
| Historial de movimientos visibles | Existe; se registra reporte, corrección, adjunto y cambio de etapa |
| Adjuntar documento | Existe en almacenamiento local; cámara física y selector real pendientes de demostración |
| Documentos **solicitados y pendientes** | **No implementado de extremo a extremo**: existe adjuntar, no gestionar solicitudes y entregas |
| Avisos al asegurado por cambios | **No implementado** como sistema funcional de notificaciones |
| Liquidador con gestiones, etapas y cierre | Existe para casos ficticios asignados; QA de reglas y SQLite |
| Asignar casos nuevos | **Pendiente**; los reportados nuevos nacen «Recibido» y sin liquidador, y no existe un flujo de asignación |
| Polimorfismo propio del negocio | **No demostrado explícitamente** |
| Validaciones adicionales (RUT, aviso de reporte +30 días, desistimiento) | **Pendientes o parciales** |
| Integración a servidores | **Fuera del alcance actual por indicación expresa del estudiante**: Spring Boot/Retrofit corresponden a evaluación 3 |

No asegurar que están completos RF-08, RF-09, RF-11 ni todas las RN si no pueden demostrarse. La [guía maestra](GUIA_MAESTRA_DSY1105.md) conserva el detalle oficial para evaluaciones siguientes.

## 4. Evidencia técnica realmente comprobada

- **Aplicación `main`:** [GitHub Actions, compilación y JUnit correctos](https://github.com/Nicricht/APP-APP/actions/runs/39623835586).
- **Código equivalente de `qa`:** [compilación/JUnit correcta](https://github.com/Nicricht/APP-APP/actions/runs/39626365190).
- **Emulador `qa`:** [pruebas instrumentadas e integración correctas](https://github.com/Nicricht/APP-APP/actions/runs/39626365414). La ejecución guarda un artefacto de informes.
- Las versiones anteriores con errores de compilación de test se corrigieron antes de esos resultados. No confundir un fallo del test con una validación de la app.
- **No se ha acreditado** JaCoCo 100 %, pruebas físicas de cámara/galería ni la participación individual de ambos miembros mediante esta revisión.

## 5. Ensayo de demostración (8 a 10 minutos)

1. **Inicio (1 min):** «Esta es una aplicación Android para seguimiento local de siniestros. Usamos Kotlin, Compose, MVVM y SQLite como en `MiRegistroAvatar`».
2. **Asegurado (2 min):** cambiar usuario ficticio, ver solo sus casos, buscar uno y abrir detalle.
3. **Reporte (2 min):** intentar enviar fecha inválida, mostrar error, completar datos y obtener número de siniestro.
4. **Historial (1 min):** abrir el caso, corregir descripción o adjuntar documento y revisar entrada con fecha.
5. **Recursos nativos (1–2 min):** cámara y galería, persistencia de fotografía y mensajes de error.
6. **Liquidador (1–2 min):** abrir un caso de ejemplo en evaluación, registrar gestión visible/interna y avanzar una etapa; mostrar qué ve el asegurado.
7. **Cierre (30 s):** explicar `StateFlow`, `SQLiteOpenHelper`, ramas y evidencias reales de cada integrante.

**Antes de exponer:** ensayar cada pulsación en el mismo dispositivo o emulador que se usará en la sala. Evitar reinstalar/borrar datos de demostración a última hora.

## 6. Reglas de cierre

- [ ] Las dos ramas usan el **mismo código funcional**; las pruebas adicionales permanecen solo en `qa`.
- [ ] La **última ejecución** de ambas ramas está verde, no solo una versión antigua.
- [ ] Los riesgos funcionales pendientes se explican con precisión, sin inventar que se implementaron.
- [ ] Los dos integrantes saben explicar clases, formularios, SQLite, recursos nativos y su propio aporte.
- [ ] El profesor puede iniciar la app, navegar por las pantallas y observar los dos recursos del dispositivo.
- [ ] Trello y GitHub muestran colaboración auténtica.
- [ ] No se integran microservicios, Retrofit, backend ni Playwright a la entrega actual.

**Resultado del control documental:** base Android funcional y QA automatizado aprobado; **cierre de pauta y nota final todavía pendientes de evidencia de dispositivo, POO, animaciones, funciones incompletas y colaboración.**
