# Validación visual de cuentas — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Mostrar validaciones junto a cada campo de registro sin alterar las reglas ni flujos de la aplicación.

**Architecture:** `ValidadorCuenta` produce los errores estructurados, y la pantalla Compose los muestra después de un envío fallido. El backend SQLite y las firmas de sus callbacks se conservan.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Android JUnit, SQLite existente.

**Spec:** `docs/superpowers/specs/2026-10-24-validacion-visual-cuentas-design.md`

## Global Constraints

- Solo Android local: no Spring Boot, Retrofit, microservicios ni Java nuevo.
- Trabajar en rama de mejora nacida de `qa`; no modificar `main` ni `entrega-01`.
- Mantener el comportamiento previo de `ValidadorCuenta.errorRegistro` y `AccesoScreen`.

## Review Focus

- RUT ficticio vacío o mal formado: debe aparecer bajo el campo RUT.
- Correo con formato inválido: debe aparecer bajo Correo electrónico.
- Dos contraseñas diferentes: debe aparecer bajo Confirmar contraseña.
- Corregir un campo inválido no debe borrar los errores de otros campos.
- Un error de base de datos, con campos válidos, debe mostrarse globalmente.

---

### Task 1: Errores estructurados del dominio

**Files:**
- Modify: `app/src/main/java/cl/duocuc/segurosbpo/model/ValidadorCuenta.kt`
- Test: `app/src/test/java/cl/duocuc/segurosbpo/ValidadorCuentaCamposTest.kt`

**Interfaces:**
- Produces: `ValidadorCuenta.erroresRegistro(nombre, rut, correo, telefono, clave, confirmacion): ErroresRegistro`, con propiedades de error nullable para cada campo, y método `primerError(): String?`.
- Conserva: `ValidadorCuenta.errorRegistro(...)` con su orden y textos anteriores.

- [ ] Step 1: Test para seis campos inválidos simultáneos, datos válidos y prioridad del primer error.
- [ ] Step 2: Ejecutar `:app:testDebugUnitTest` y comprobar fallo por método ausente.
- [ ] Step 3: Implementar un resultado de validación tipado y reutilizarlo desde `errorRegistro`.
- [ ] Step 4: Ejecutar suite unitaria y verificar éxito.
- [ ] Step 5: Commit del resultado.

### Task 2: Errores inline y reactividad Compose

**Files:**
- Modify: `app/src/main/java/cl/duocuc/segurosbpo/ui/screens/AccesoScreen.kt`
- Test: `app/src/androidTest/java/cl/duocuc/segurosbpo/AccesoPantallaCoberturaRealTest.kt`

**Interfaces:**
- Consumes: `ValidadorCuenta.erroresRegistro` y las seis propiedades nullable.
- Mantiene callbacks `onRegistrar`, `onIniciar`, `onDemoLiquidador`, `onDemoAsegurado`.

- [ ] Step 1: Test Compose que envía un registro inválido, encuentra errores junto al campo y ve desaparecer solo el error corregido.
- [ ] Step 2: Ejecutar test instrumentado y verificar fallo esperado por UI faltante.
- [ ] Step 3: Añadir `isError` y `supportingText` y refrescar mensajes cuando se edita.
- [ ] Step 4: Ejecutar instrumentadas y suite completa, verificar éxito.
- [ ] Step 5: Commit del resultado.

### Task 3: Auditoría vigente de siete criterios

**Files:**
- Create: `docs/AUDITORIA_PAUTA_DSY1105_2026-10-24.md`
- Modify: `docs/BITACORA_AVANCES_QA.md`

**Interfaces:**
- Consumes: código de `qa`, guía maestra, workflows y evidencia verificable.
- Produces: tabla por criterio: evidencia, riesgos y prueba pendiente, sin atribuir nota definitiva.

- [ ] Step 1: Inspeccionar fuentes, tests y CI de los siete criterios.
- [ ] Step 2: Registrar auditoría con enlaces al archivo/CI y limitaciones reales.
- [ ] Step 3: Verificar rutas documentadas y diff antes de commit.
- [ ] Step 4: Commit del resultado.
