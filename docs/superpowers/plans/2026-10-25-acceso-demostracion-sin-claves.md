# Acceso simulado sin claves — implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans. Steps use checkbox syntax.

**Goal:** Ofrecer acceso a asegurados y liquidadores de prueba sin registro ni contraseñas, conforme a FA-01, S-02 y RNF-08.

**Architecture:** Catálogo puro y comprobable de identidades simuladas. SQLite solo almacena información no secreta para editar teléfono/foto y migra la tabla heredada eliminando hashes. Compose presenta botones de elección de perfil; el ViewModel valida y persiste la selección.

**Tech Stack:** Kotlin, Jetpack Compose, SQLiteOpenHelper, JUnit.

**Spec:** `docs/superpowers/specs/2026-10-25-acceso-demostracion-sin-claves-design.md`.

## Constraints

- Sin pantallas de registro ni campos de claves.
- Preservar expedientes de la base SQLite de siniestros.
- Rol Liquidador acotado a la especialidad del equipo seleccionado.

## Review Focus

- SQLite 1/2/3 migrado a nueva tabla sin `salt`/`clave_hash`.
- Reabrir app mantiene persona válida; perfil antiguo inexistente vuelve al selector.
- Asegurado sin siniestros ve panel vacío; otro perfil no ve sus archivos.
- El teclado ya no es necesario para elegir perfil.
- Foto y teléfono guardados sobreviven cambio de rol.

### Task 1. Catálogo y pruebas

- [ ] Escribir test de ocho asegurados y dos liquidadores; ids únicos y roles/equipos correctos.
- [ ] Verificar fallo del test sin la implementación.
- [ ] Implementar `model/PerfilesDemostracion.kt` y ejecutar pruebas disponibles.

### Task 2. Persistencia segura y sesión

- [ ] Implementar y verificar SQLite v4 sin columnas de claves y migración que conserve información de perfil.
- [ ] Eliminar APIs de registrar/iniciar sesión, actualizar ViewModel para selección por id, persistir id válido.
- [ ] Verificar que datos de siniestros permanezcan en su base separada.

### Task 3. Interfaz y documentación

- [ ] Reemplazar AccesoScreen por selector de perfiles claros y botones accesibles.
- [ ] Adaptar perfil y navegación; actualizar docs y retirar validador/componentes de contraseña obsoletos.
- [ ] Verificar diff, compilación y pruebas unitarias/CI; publicar en misma rama y PR.
