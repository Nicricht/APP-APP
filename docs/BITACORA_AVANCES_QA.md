# Bitácora de avances y evidencias de QA

Repositorio: [Nicricht/APP-APP](https://github.com/Nicricht/APP-APP). Rama de trabajo: `qa`.
Las ramas `main` y `entrega-01` no se modifican sin autorización expresa.

## Cómo registrar cada avance

Por cada cambio, anotar fecha, objetivo, archivos, pruebas asociadas, SHA y resultado real.
Distinguir **implementado** de **verificado**, indicar bloqueos y no atribuir
una ejecución verde de otro SHA al commit actual. Los nuevos cambios se
documentan junto con el código en el mismo commit cuando sea posible.

## 2026-10-24: auditoría del estado vigente

- Línea base auditada: `ac474a1e3f04194fe523fbe159c1a5f1d412b0f7`.
- Android Kotlin QA: [ejecución 40064213295](https://github.com/Nicricht/APP-APP/actions/runs/40064213295), correcta.
- Android instrumented QA: [ejecución 40064213329](https://github.com/Nicricht/APP-APP/actions/runs/40064213329), correcta.
- Cobertura instrumentada de esa línea base: **2985/3085 líneas (96,76 %) y 910/1001 ramas (90,91 %)**, extraída del log del job. No es el 100 %.
- Instalación automática de la APK de pruebas mediante ADB: exitosa en los emuladores API 30 y API 35, asociada al run 40064213295. No sustituye la prueba en el teléfono físico.
- Mayores huecos medidos: `AppNavegacionKt` (32 líneas), `PerfilCuentaScreenKt` (26 líneas, 23 ramas), `FormularioScreenKt` (15 líneas) y `SiniestrosViewModel` (10 líneas, 11 ramas).
- Alcance: app académica local Kotlin/Compose/SQLite. La base y las cuentas de demostración no son un backend remoto compartido.

## 2026-10-24: cobertura de validación de cuentas

- Commit: [9e5af050](https://github.com/Nicricht/APP-APP/commit/9e5af0507aca5b267505566cd9ad31d0d2046e17).
- Archivo añadido: `ValidadorCuentaRamasAndroidTest.kt`.
- Casos: RUT con estructura errónea, distintos dígitos verificadores, correo, longitud de teléfono, contraseñas, confirmación y registro válido.
- Android Kotlin QA: [40068165776](https://github.com/Nicricht/APP-APP/actions/runs/40068165776), correcta.
- Android instrumented QA: [40068165774](https://github.com/Nicricht/APP-APP/actions/runs/40068165774), pendiente de resultado al registrar esta entrada.
- No se declara un nuevo porcentaje hasta obtener el reporte del SHA.

## 2026-10-24: integridad transaccional al tomar un siniestro

- Archivo añadido: `app/src/androidTest/java/cl/duocuc/segurosbpo/TomarCasoRollbackCoberturaTest.kt`.
- Prueba 1 (ASI-006): se provoca un error de base de datos al crear el aviso y se verifica que **no** quede asignación, cambio de etapa, gestión ni aviso parcial. Se elimina el disparador temporal y se repite la asignación con éxito.
- Prueba 2 (ASI-005): dos instancias independientes del helper intentan tomar el mismo caso de forma secuencial; solo la primera lo consigue, sin gestiones ni avisos duplicados. **No equivale a una prueba de simultaneidad real**.
- Estado al registrar: **implementadas; esperando resultado de CI para el commit que incluya esta bitácora**.
- Sin cambios de lógica de producción, esquema SQLite, `main` ni `entrega-01`.

## 2026-10-24: resultado del rollback y cobertura instrumentada

- Commit auditado: [0e2fc9f1](https://github.com/Nicricht/APP-APP/commit/0e2fc9f19411806d1af19dd1ea863b3109480e82).
- Workflows terminados correctamente: [Kotlin QA 40068907997](https://github.com/Nicricht/APP-APP/actions/runs/40068907997) e [instrumented QA 40068907995](https://github.com/Nicricht/APP-APP/actions/runs/40068907995).
- Las pruebas de reversión de asignación, recuperación y rechazo de segundas asignaciones pasaron dentro de la suite instrumentada.
- JaCoCo instrumentado: **2988/3085 líneas (96,86 %)**, **920/1001 ramas (91,91 %)**.
- Instalación ADB en emuladores API 30 y API 35: correcta para el SHA.
- No son pruebas en dispositivo físico ni certifican 100 % de cobertura.

## 2026-10-24: mensajes de cámara y galería en perfil

- Código: `ui/screens/PerfilCuentaScreen.kt`.
- Test: `PerfilFotoActividadCoberturaTest.kt`.
- Alcance: permiso de cámara denegado, captura cancelada, fotografía aceptada y selector de galería cancelado. En lugar de un silencio de interfaz, se informa el resultado de la acción. Una fotografía guardada sin error presenta `Foto actualizada`.
- Sin nuevas dependencias, permisos ni cambios en SQLite.
- Estado al escribir: cambio implementado en `qa`; confirmar resultado mediante CI del commit que contiene esta entrada.

## Pendientes sin evidencia suficiente

1. Comprobar el informe de JaCoCo del nuevo SHA; seguir con perfil, navegación, formularios y ViewModel.
2. Ensayar simultaneidad real de asignación si se requiere ASI-004, sin confundirla con acceso secuencial desde dos conexiones.
3. Ejecutar recorrido completo de la APK en dispositivo físico: cámara, galería, PDF, permisos, cierre de sesión, reinicio y persistencia.
4. Revisar seguridad de propiedad por cuenta, experiencia accesible en pantallas pequeñas y evidencia de colaboración del equipo.
5. Registrar cualquier error en GitHub Actions con run ID y SHA y corregirlo en `qa` antes de darlo por cerrado.

Referencias: [Plan maestro](PLAN_MAESTRO_FINALIZACION.md),
[Protocolo QA](PROTOCOLO_QA_OBLIGATORIO.md),
[Matriz de aceptación](MATRIZ_ACEPTACION_Y_PRUEBAS.md).
