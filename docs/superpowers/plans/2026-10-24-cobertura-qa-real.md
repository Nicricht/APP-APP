# Cobertura real de APP-APP — plan de ejecución

> **Objetivo interno del equipo:** aspirar al 100 % de líneas y ramas JaCoCo de la app Android. La pauta docente de DSY1105 **no** exige ese porcentaje. No confundir 100 % de pruebas ejecutadas con 100 % de cobertura.

**Base:** rama `qa` tras el PR #11 (`712b59c`). Fuente de verdad: `docs/GUIA_MAESTRA_DSY1105.md` y la referencia `MiRegistroAvatar.zip`.

**Arquitectura inalterable:** Kotlin, Jetpack Compose, MVVM y SQLite local. Sin Spring Boot, Retrofit, API REST ni Java nuevo. Las ramas `main` y `entrega-01` permanecen intactas.

**Línea base verificada:** [Actions #40142749271](https://github.com/Nicricht/APP-APP/actions/runs/40142749271), 3176/3181 líneas (99,84 %), 1111/1147 ramas (96,86 %), suite instrumentada verde.

## Tareas

### 1. Formulario de acceso y validaciones visuales

- [ ] Añadir pruebas Compose que comprueben los seis mensajes por campo, sus correcciones reactivas y la respuesta del formulario válido.
- [ ] Verificar que una regresión visual es detectada por la prueba y mantener el comportamiento existente.
- [ ] Quitar redundancia en expresiones de mensajes solo si la prueba la protege, sin retirar validaciones.
- [ ] Ejecutar las pruebas Android y examinar JaCoCo.

### 2. Casos reales restantes

- [ ] Añadir pruebas orientadas a resultado de la sesión, estadísticas, perfil, plazos y navegación según líneas/ramas no cubiertas del reporte XML.
- [ ] Preferir recorridos públicos de la app sobre pruebas de getters o de internals sintéticos de Compose.
- [ ] Ejecutar la suite completa, comprobar efectos y verificar que la cobertura no retrocede.

### 3. Resultado verificable y entrega

- [ ] Guardar métricas, run IDs y pendientes reales en la bitácora de QA y el issue BPO-13 (#6).
- [ ] Revisar el diff, publicar PR hacia `qa` y comprobar el estado real de los workflows.
- [ ] Marcar 100 % **solo** si JaCoCo informa cero líneas y cero ramas perdidas en la ejecución final.

## Criterio de seguridad de pruebas

Si una rama es teóricamente imposible por el compilador (p. ej. manejo de `null` tras una validación que garantiza no nulidad), documentarla; no introducir acciones ficticias, dependencias ni exclusiones amplias para elevar artificialmente el porcentaje.
