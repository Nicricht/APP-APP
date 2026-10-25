# Experiencia Seguros BPO Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reparar los flujos de registro, siniestro y evidencias mientras se aplica una identidad visual coherente a las pantallas principales.

**Architecture:** Extender el diseño Compose actual mediante componentes reutilizables y estados explícitos. Centralizar reglas en el modelo/ViewModel y exponer las evidencias mediante consultas SQLite acotadas al propietario o liquidador asignado; usar FileProvider para archivos temporales.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, SQLiteOpenHelper, JUnit, Gradle 9.6.

**Spec:** `docs/superpowers/specs/2026-10-25-experiencia-seguros-bpo-design.md`

## Global Constraints

- Preservar datos de SQLite y compatibilidad de rutas actuales; incluir migración para columnas nuevas.
- Máximo 15 adjuntos de hasta 5 MB, solo imágenes y PDF.
- Comuna escrita permitida; no obligar región ni ciudad.
- Contraseñas actuales se conservan provisionalmente mientras se resuelve RNF-08.

## Review Focus

1. Formulario sin empresa/tipo, incluso tras limpiar: se rechaza con un error por campo.
2. Formulario válido con compañía activa y tipo escogidos: se registra correctamente.
3. Adjuntar un tipo de documento que el liquidador solicitó: coincide con la solicitud y cambia su estado.
4. Asegurado no puede abrir adjuntos de otro usuario; liquidador no puede leer archivos sin asignación.
5. Sin lector de PDF instalado o sin permisos de cámara: se presenta un mensaje no destructivo.

---

### Task 1: Identidad visual y registro

**Files:** `ui/theme/{Color,Theme,Type}.kt`, `ui/screens/AccesoScreen.kt`, `navigation/AppNavegacion.kt`.

- [ ] Añadir pruebas representativas para presentación/validación donde haya lógica pura.
- [ ] Configurar esquema azul marino/turquesa, tipografía y componentes accesibles.
- [ ] Hacer la pantalla de acceso desplazable con teclado, ejemplo de RUT, icono de visibilidad y snackbar de éxito de duración breve; eliminar CTA de Camila Rojas.
- [ ] Ejecutar las pruebas y revisar errores de compilación.

### Task 2: Formulario sin elecciones predeterminadas

**Files:** `model/{FormularioUiState,ValidadorSiniestro}.kt`, `viewmodel/FormularioViewModel.kt`, `ui/{components/SelectorOpciones,screens/FormularioScreen}.kt`, `data/SiniestroDbHelper.kt`, tests de formulario.

- [ ] Prueba RED: estado inicial sin compañía/tipo, registrar vacío produce dos errores y completar ambos permite avanzar.
- [ ] Validar valores ausentes en modelo, ViewModel y base de datos, y mostrar selector con placeholder y error por campo.
- [ ] Ejecutar pruebas GREEN y regresión completa.

### Task 3: Documentos y visores por rol

**Files:** `model/{Evidencia,TipoDocumento}.kt`, `data/{SiniestroDbHelper,VisualizadorEvidencias}.kt`, `viewmodel/SiniestrosViewModel.kt`, `ui/screens/{EvidenciasScreen,CasosLiquidadorScreen}.kt`, `navigation/AppNavegacion.kt`, `AndroidManifest.xml`, `res/xml/`, tests.

- [ ] Prueba RED: catálogo de tipos y manejo de datos solicitados; consultas con restricción de rol.
- [ ] Agregar selector y comentario persistido con migración SQLite, quitar texto local y aplicar `imePadding`.
- [ ] Exponer imágenes y PDF con comprobación de pertenencia/asignación; abrir archivos de forma segura y con error legible.
- [ ] Cargar evidencias en pantalla de liquidador y presentar botones de consulta; verificar la asociación con solicitudes.
- [ ] Ejecutar pruebas GREEN y revisar flujos por ambos roles.

### Task 4: Perfil, avisos y casos

**Files:** `ui/screens/{PerfilCuentaScreen,AvisosScreen,CasosLiquidadorScreen}.kt`, componentes de UI y pruebas pertinentes.

- [ ] Crear jerarquía visual, tarjetas, avatar y acciones de perfil.
- [ ] Rediseñar avisos con estados y botones directos; hacer explícito dónde tomar un caso disponible.
- [ ] Validar accesibilidad, estado vacío, persistencia y navegación con pruebas y revisión manual.

### Task 5: Verificación y entrega

- [ ] Ejecutar `bash gradlew :app:testDebugUnitTest :app:assembleDebug --console=plain` con JDK/SDK compatibles.
- [ ] Ejecutar comprobaciones estáticas y revisar `git diff --check`.
- [ ] Crear commits separados y una rama/PR desde `feature/ux-profesional-20261025`, sin modificar la rama de referencia.
- [ ] Documentar los límites no resueltos (RNF-08, notificación fuera de la app y sincronización entre dispositivos).
