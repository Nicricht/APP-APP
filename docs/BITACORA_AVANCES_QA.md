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
- Android instrumented QA: [40068165774](https://github.com/Nicricht/APP-APP/actions/runs/40068165774), cancelada al recibir el commit posterior. La prueba quedó incluida y aprobada en las ejecuciones posteriores.
- No se declara un nuevo porcentaje hasta obtener el reporte del SHA.

## 2026-10-24: integridad transaccional al tomar un siniestro

- Archivo añadido: `app/src/androidTest/java/cl/duocuc/segurosbpo/TomarCasoRollbackCoberturaTest.kt`.
- Prueba 1 (ASI-006): se provoca un error de base de datos al crear el aviso y se verifica que **no** quede asignación, cambio de etapa, gestión ni aviso parcial. Se elimina el disparador temporal y se repite la asignación con éxito.
- Prueba 2 (ASI-005): dos instancias independientes del helper intentan tomar el mismo caso de forma secuencial; solo la primera lo consigue, sin gestiones ni avisos duplicados. **No equivale a una prueba de simultaneidad real**.
- Resultado posterior: pruebas aprobadas en los workflows [40068907995](https://github.com/Nicricht/APP-APP/actions/runs/40068907995) y [40068907997](https://github.com/Nicricht/APP-APP/actions/runs/40068907997) del commit `0e2fc9f1`.
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

## 2026-10-24: QA de perfil aprobado y guía de instalación corregida

- Código auditado: [16753db0](https://github.com/Nicricht/APP-APP/commit/16753db0c07a0c7d18839ac59f4373c067543fa4).
- Workflows aprobados: [Android Kotlin QA 40069761753](https://github.com/Nicricht/APP-APP/actions/runs/40069761753) y [Android instrumented QA 40069761829](https://github.com/Nicricht/APP-APP/actions/runs/40069761829).
- Instalación y apertura automatizadas: correctas en API 30 y API 35; falta ensayo en el teléfono particular.
- JaCoCo instrumentado: **2999/3087 líneas (97,15 %)** y **929/1005 ramas (92,44 %)**.
- Comparado con el commit anterior: +11 líneas cubiertas y +9 ramas cubiertas, con un denominador que aumentó por el nuevo código.
- El perfil pasa de 26 a 16 líneas no cubiertas en el informe de JaCoCo. Nuevos recorridos confirmados: permiso denegado, cámara cancelada, foto aceptada y galería cancelada.
- Documento actualizado: [Cómo ejecutar Android](COMO_EJECUTAR_ANDROID.md). Sustituye instrucciones antiguas de simulación de roles por registro/login, casos disponibles por área, toma de casos, descarga de artefacto de `qa`, instalación en teléfono y diagnóstico de firmas APK.
- Riesgo práctico documentado: los APK de depuración generados por runners temporales pueden tener firmas distintas entre ejecuciones. Ante `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, no desinstalar sin considerar la pérdida de datos SQLite; es necesario confirmar el mensaje ADB del teléfono antes de concluir una causa.
- También se verificaron tres recursos originales del ejemplo del profesor por SHA-1 Git: `avatar_usuario.png`, `banner_registro.png` y `portada_perfil.png`, en coincidencia exacta. Esto no demuestra igualdad de todas las pantallas. Seguimiento en [issue #7](https://github.com/Nicricht/APP-APP/issues/7).
- Estado de esta entrada: cambio documental sobre una versión de código ya verificada; no modifica Kotlin, manifest, gradle, recursos o base SQLite.

## 2026-10-24: asignación simultánea y pruebas reactivas de ViewModel

- Código de producción: **sin cambios**; se verifica el comportamiento existente antes de introducir funciones nuevas.
- Prueba instrumentada nueva: `TomarCasoConcurrenciaRealTest.kt`.
- Caso ASI-004: dos hilos, dos instancias de `SiniestroDbHelper` y dos conexiones SQLite compiten por un mismo reporte, coordinados con `CountDownLatch`. Se espera exactamente un éxito y un rechazo, con una sola gestión y un solo aviso.
- Ampliación: `FlujoViewModelCoberturaTest.kt`. Comprueba toma exitosa, rechazo de reasignación, restricciones entre áreas, rollback visible en ViewModel ante error de aviso y búsqueda de duplicados limitada al propietario activo.
- Limitación: el modelo académico solo tiene un liquidador definido por área; la prueba concurrente comprueba exclusión en almacenamiento, **no** la competencia entre dos usuarios distintos de la misma especialidad, que requiere ampliar el modelo.
- Punto de referencia: [QA instrumentado del commit documental 242e4966](https://github.com/Nicricht/APP-APP/actions/runs/40070639355) terminado correctamente. Ese cambio solo contenía documentación.
- Resultado: [Android Kotlin QA 40086509540](https://github.com/Nicricht/APP-APP/actions/runs/40086509540) y [Android instrumented QA 40086509545](https://github.com/Nicricht/APP-APP/actions/runs/40086509545), ambas correctas en `be7d1ea7`. JaCoCo: **3009/3087 líneas (97,47 %) y 939/1005 ramas (93,43 %)**. APK instalada/abierta en emuladores API 30 y 35.
- No se crean comentarios en el código ni se tocan `main` y `entrega-01`.

## 2026-10-24: menú por roles y galería validada con archivos reales

- Archivo nuevo: `app/src/androidTest/java/cl/duocuc/segurosbpo/MenuLateralRolesCoberturaTest.kt`. Ejercita opciones del menú lateral de asegurado (reporte, estadísticas, avisos, perfil, panel y cierre de sesión) y liquidador (panel de trabajo, perfil y salida), comprobando pantallas de destino.
- Archivo ampliado: `app/src/androidTest/java/cl/duocuc/segurosbpo/PerfilFotoActividadCoberturaTest.kt`. Agrega JPEG válido, PDF rechazado para foto, MIME JPEG con contenido PDF y cabecera JPEG con contenido no decodificable. Verifica resultado visual y ausencia de guardados no autorizados.
- Justificación: el informe JaCoCo de `242e4966` mostró acciones de menú lateral no ejecutadas y el recorrido de galería sin cubrir. Las pruebas se enfocan en comportamiento observable, no en llamadas artificiales.
- Código de producción y esquema SQLite: sin cambios. Test data generado y en proveedor instrumentado propio, sin datos reales.
- Estado al registrar: **pendiente de ejecución Android Kotlin QA e instrumentada del commit asociado**.

## 2026-10-24: cierre de QA en menú por roles y galería

- Commit verificado: [e2b54ead](https://github.com/Nicricht/APP-APP/commit/e2b54eadd215ffa97b10d7f8c6312463e9a309eb).
- [Android Kotlin QA 40087227507](https://github.com/Nicricht/APP-APP/actions/runs/40087227507) e [instrumented QA 40087227435](https://github.com/Nicricht/APP-APP/actions/runs/40087227435): ambas correctas.
- Cobertura instrumentada: **3052/3087 líneas (98,87 %)** y **950/1005 ramas (94,53 %)**.
- Se verificaron menú lateral de asegurado y liquidador y cuatro escenarios del selector de imágenes de perfil. Los huecos principales quedan en `FormularioScreenKt`, `SiniestroDbHelper` y ramas de sesión.

## 2026-10-24: BPO-03, validación de datos en SQLite

- Hallazgo P0 documentado: [issue #8](https://github.com/Nicricht/APP-APP/issues/8).
- Archivo modificado: `app/src/main/java/cl/duocuc/segurosbpo/data/SiniestroDbHelper.kt`.
- Reglas nuevas en `registrar`: fecha válida y no futura, comuna no vacía, descripción no vacía, compañía activa y confirmación explícita. La comprobación es previa a abrir la transacción y no altera bases ya existentes.
- Archivo nuevo: `app/src/androidTest/java/cl/duocuc/segurosbpo/RegistroSiniestroValidacionDbTest.kt`.
- Casos de prueba: siete formularios inválidos sin efectos persistidos en siniestros, gestiones ni avisos; uno válido con limpieza de espacios y número único; uno fuera de 30 días aceptado con advertencia de plazo.
- Fixtures actualizados para exigir confirmación también en las pruebas antiguas: `BusquedaSeguraCoberturaTest`, `DecisionesSQLiteAdicionalesTest`, `FlujoViewModelCoberturaTest`, `ImagenesSQLiteLimitesTest`.
- Validación **aprobada**: [Kotlin QA 40088035332](https://github.com/Nicricht/APP-APP/actions/runs/40088035332) e [instrumented QA 40088035341](https://github.com/Nicricht/APP-APP/actions/runs/40088035341), ambas verdes para `3789287d`. JaCoCo instrumentado: **3060/3095 líneas (98,87 %) y 957/1013 ramas (94,47 %)**. Instalación y arranque API 30 y 35 correctos. [Issue #8 cerrado](https://github.com/Nicricht/APP-APP/issues/8).
- Ningún comentario agregado al código. Las ramas `main` y `entrega-01` no se tocan.

## 2026-10-24: confirmación de posible duplicado y botón Atrás

- Archivo nuevo: `app/src/androidTest/java/cl/duocuc/segurosbpo/ConfirmacionDuplicadosFormularioTest.kt`.
- Caso SIN-006: antes de registrar un posible duplicado se abre el diálogo, se muestra el número de caso existente y se exige pulsar **Continuar de todos modos** para registrar. **Revisar datos** cierra la advertencia sin ejecutar el callback de registro.
- Caso UX: pulsar **Atrás** del sistema mientras está abierto el diálogo lo cierra sin registrar el caso. Es una prueba de interacción Android real, no una llamada aislada al ViewModel.
- Objetivo de cobertura: rutas de `FormularioScreenKt` (15 líneas y 2 ramas que estaban sin ejecutar en el informe del SHA `3789287d`).
- Estado al subir este archivo: **pendiente de su propia ejecución Kotlin e instrumentada**. No se modifica código de producción, y el test usa siniestros ficticios.
- Importante sobre APK: la comparación de certificados de dos builds distintas confirmó firmas debug diferentes; documentado con hashes SHA-256 en [issue #7](https://github.com/Nicricht/APP-APP/issues/7). Una instalación previa con firma distinta puede bloquear la actualización. No desinstalar si se necesitan sus datos locales.

## 2026-10-24: cierre de pruebas de duplicados y cobertura vigente

- Commit [550e174c](https://github.com/Nicricht/APP-APP/commit/550e174c5c83bd029f18963920a337674e39c50f) validado por [Kotlin QA 40088816862](https://github.com/Nicricht/APP-APP/actions/runs/40088816862) e [instrumented QA 40088816856](https://github.com/Nicricht/APP-APP/actions/runs/40088816856), ambos terminados con éxito.
- JaCoCo instrumentado: **3073/3095 líneas (99,29 %) y 959/1013 ramas (94,67 %)**. Restan 22 líneas y 54 ramas.
- El diálogo de reporte duplicado solicita confirmación explícita; Revisar datos y el botón Atrás no registran.
- Se descargó y analizó el XML de cobertura de ese mismo run, que ubica nueve líneas pendientes en `SiniestroDbHelper` concentradas en la creación de recordatorios de solicitudes a los quince días.

## 2026-10-24: recordatorios de quince días y cierre anticipado

- Cambio de producción: `SiniestroDbHelper.generarRecordatorios` consulta únicamente solicitudes sin entregar **de siniestros que continúan abiertos**. Un cierre anticipado por Desistido/Rechazado no debe crear avisos posteriores de documentos ya irrelevantes.
- Test nuevo `RecordatoriosPendientesPlazosTest.kt` con SQLite independiente y fechas UTC controladas:
  1. Día 14: no aviso. Día 15: un aviso y una gestión visible. Días 16/30: no duplicados, aislado del otro asegurado.
  2. Entrega comprobada del documento: no aviso a los 30 días.
  3. Cierre anticipado en evaluación manteniendo la solicitud en histórico: ningún recordatorio posterior, conservando el aviso de cierre.
- No se cambian el esquema de base, sus migraciones, las etapas ni sus resultados permitidos; solo se evita una notificación incoherente con el estado final.
- Resultado **aprobado**: [Kotlin QA 40090100375](https://github.com/Nicricht/APP-APP/actions/runs/40090100375) e [instrumented QA 40090100332](https://github.com/Nicricht/APP-APP/actions/runs/40090100332), ambas verdes para `c0e18be3`. JaCoCo instrumentado: **3082/3095 líneas (99,58 %) y 964/1013 ramas (95,16 %)**. Instalaciones/API 30 y 35 correctas.
- No se añaden comentarios al código. `main` y `entrega-01` permanecen sin modificaciones.

## 2026-10-24: sesiones, fotografías, migración v2 y clasificación por áreas

- Cambios exclusivamente en pruebas instrumentadas Android y esta bitácora; **ningún archivo de producción**.
- `SesionCuentaRealCoberturaTest.kt`: verifica que sin sesión y en perfiles de demostración no se puedan guardar fotografías. Verifica además que el límite de 1 MB rechace JPEG grandes, manteniendo en SQLite la fotografía anterior.
- `CuentaDbCoberturaRealTest.kt`: comprueba migración incremental de `cuentas` versión 2 a 3 conservando foto, perfil, teléfono y creación de los dos liquidadores de prueba.
- Nuevo `AreasYPlazosLimitesTest.kt`: comprueba la clasificación de los seis tipos en las áreas correctas, pertenencia exclusiva, equipos y cálculo UTC de 0, 14, 15 y 30 días, además de fechas futuras o inválidas.
- Justificación: tras superar el 99 % de líneas, los siguientes huecos de ramas se concentran en estados de cuenta, migraciones y plazos. Estas pruebas verifican reglas útiles y persistencia, sin aumentar cobertura mediante ejecuciones vacías.
- Validación aprobada para [d42af29d](https://github.com/Nicricht/APP-APP/commit/d42af29d5ca56d4c74a35e042fcda43a028fec9f): [Kotlin QA 40090923765](https://github.com/Nicricht/APP-APP/actions/runs/40090923765) e [instrumented QA 40090923786](https://github.com/Nicricht/APP-APP/actions/runs/40090923786), ambas **success**. JaCoCo instrumentado: **3086/3095 líneas (99,71 %) y 975/1013 ramas (96,25 %)**. Instalación y arranque en emuladores API 30/35 aprobados.
- No se añaden comentarios al código ni se modifican `main` y `entrega-01`.

## 2026-10-24: reporte de cobertura y artefacto descargable

- Último commit con cambios Kotlin verificados: [d42af29d](https://github.com/Nicricht/APP-APP/commit/d42af29d5ca56d4c74a35e042fcda43a028fec9f).
- La nueva cobertura llegó al 99,71 % de líneas y 96,25 % de ramas. Restan 9 líneas y 38 ramas en JaCoCo instrumentado; pendiente continuar sin falsificar exclusiones.
- Artefacto APK generado por el workflow de `d42af29d`: `SegurosBPO-debug-apk`, artifact de GitHub Actions `12559042677`. Se verificó la extracción directa del archivo `app-debug.apk` (15.303.602 bytes) y su SHA-256: `c979de88eeee4b68f9a801bdba5283711420308ef29453212d32e0a468c7f3ef`.
- El APK se instaló y abrió en los emuladores Android API 30 y 35; no hay evidencia de instalación en el teléfono físico particular.
- La firma `debug` puede variar entre runners: una actualización sobre la app instalada con otra firma puede fallar incluso si ambos APK son válidos. Diagnóstico y evidencias en [issue #7](https://github.com/Nicricht/APP-APP/issues/7).
- Esta entrada es solo documental; no modifica código de producción, código de pruebas, `main` ni `entrega-01`.

## 2026-10-24: pruebas de errores visibles de perfil y bandeja de liquidadores

- Base de trabajo: [5d6bf39e](https://github.com/Nicricht/APP-APP/commit/5d6bf39e3bc3a99d366f5c7d9aa4c00f7dd56925), cambio exclusivamente documental con [instrumentadas 40091920366](https://github.com/Nicricht/APP-APP/actions/runs/40091920366) **success**, mismo JaCoCo del código anterior: **3086/3095 líneas (99,71 %) y 975/1013 ramas (96,25 %)**.
- Nuevas pruebas en `PerfilFotoActividadCoberturaTest.kt`: fallos del callback de guardado tanto desde cámara como desde galería. Verifican mensaje de error y que no se muestre "Foto actualizada".
- Nueva prueba en `PerfilCuentaCoberturaRealTest.kt`: fotografías previamente persistidas con bytes no decodificables deben permitir consultar el perfil normalmente, sin mostrar imagen inválida ni provocar cierre.
- Nueva clase `BandejaLiquidadorYEstadisticasLimitesTest.kt`: bandeja con siniestro disponible y manejador de toma no suministrado muestra "No disponible" y no simula una asignación; la tarjeta estadística sin casos distingue un título/descripción alternativos; la evolución mensual ignora fechas malformadas sin alterar conteos reales.
- Fundamento: el XML JaCoCo indicó decisiones pendientes en `PerfilCuentaScreen`, `CasosLiquidadorScreen`, `ResumenGraficosCasos` y `EstadisticasCasos`. Se comprueban estados útiles al usuario, sin trampas para subir cobertura.
- Archivos de producción: **sin cambios**. Se agregan 6 tests instrumentados y esta entrada.
- Estado: pruebas nuevas **pendientes de CI de este commit**. No afirmar cobertura ni verde antes de completar las dos suites.
- Ramas `main` y `entrega-01`: no modificar. No insertar comentarios en código Kotlin.

## Pendientes sin evidencia suficiente

1. Volver a medir JaCoCo en el SHA que valida los recordatorios; continuar con ramas de CuentaSesionViewModel y otros flujos sin cubrir.
2. El escenario de dos conexiones/hilos simultáneos quedó probado en `be7d1ea7` con suite instrumentada verde. Para usuarios distintos del mismo equipo haría falta ampliar el modelo local de liquidadores.
3. Ejecutar recorrido completo de la APK en dispositivo físico: cámara, galería, PDF, permisos, cierre de sesión, reinicio y persistencia.
4. Revisar seguridad de propiedad por cuenta, experiencia accesible en pantallas pequeñas y evidencia de colaboración del equipo.
5. Registrar cualquier error en GitHub Actions con run ID y SHA y corregirlo en `qa` antes de darlo por cerrado.

Referencias: [Plan maestro](PLAN_MAESTRO_FINALIZACION.md),
[Protocolo QA](PROTOCOLO_QA_OBLIGATORIO.md),
[Matriz de aceptación](MATRIZ_ACEPTACION_Y_PRUEBAS.md).
