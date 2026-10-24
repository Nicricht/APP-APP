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
- Resultado del commit [`c38b0910`](https://github.com/Nicricht/APP-APP/commit/c38b0910c2a0f4072aa3911f5079464ed3cde69b): [Kotlin QA 40093887015](https://github.com/Nicricht/APP-APP/actions/runs/40093887015) e [instrumentado 40093887054](https://github.com/Nicricht/APP-APP/actions/runs/40093887054) **success**. JaCoCo: **3088/3095 líneas (99,77 %) y 980/1013 ramas (96,74 %)**, quedan siete líneas y 33 ramas. Instalación API 30/35 aprobada.
- Ramas `main` y `entrega-01`: no modificar. No insertar comentarios en código Kotlin.

## 2026-10-24: seguimiento de gestiones cada siete días, fase funcional

- Requisito del plan maestro: [issue #9](https://github.com/Nicricht/APP-APP/issues/9) y sección 10 de `PLAN_MAESTRO_FINALIZACION.md`.
- `SiniestroDbHelper.listarSeguimientosVencidos`: busca solo casos **asignados al liquidador consultado** y abiertos; examina la última gestión visible a partir del historial persistido, y devuelve los IDs cuya fecha supera o alcanza siete días corridos. Las gestiones internas no reinician el plazo. No escribe avisos ni falsifica registros de seguimiento.
- `SiniestrosViewModel`: añade `seguimientosVencidos` reactivo por liquidador. Refresca al seleccionar equipo o registrar una gestión; no permite que la consulta cruce especialidades.
- `AppNavegacion` y `CasosLiquidadorScreen`: muestran un mensaje en la lista y en el detalle cuando se requiere gestión visible. La acción "Completar con Sin novedades" llena la descripción y marca visibilidad, pero **no crea la gestión hasta pulsar "Registrar gestión"**.
- Pruebas nuevas: `SeguimientoSemanalDbTest` (6/7/14 días, privacidad por equipo, gestión interna frente a visible, caso cerrado), `SeguimientoSemanalViewModelTest` (actualización de estado tras acción) y `SeguimientoSemanalPantallaTest` (alerta en listado/detalle, estado al día y confirmación explícita de "Sin novedades").
- Alcance: comprobación al **abrir o actualizar la aplicación SQLite local**. No se garantiza programación en segundo plano, push remoto ni sincronización entre dispositivos. No se agrega un cron ni un servicio de red inexistente.
- Esquema SQLite y migraciones: sin cambios. Se agregan pruebas instrumentadas con bases exclusivas y fechas controladas.
- Estado: **pendiente de compilación, instrumentadas y medición del commit que contiene esta entrada**.
- Verificar antes de cerrar el [issue #9](https://github.com/Nicricht/APP-APP/issues/9) que el texto e interfaz muestran suficiente contexto de plazo. No declarar finalizado un requisito parcial.
- Se respeta la convención de no añadir comentarios Kotlin; solo se modifica `qa`, sin tocar `main` ni `entrega-01`.

## 2026-10-24: diagnóstico real del CI de seguimiento semanal y corrección del retroceso Android

- Commit examinado: [3e919b79](https://github.com/Nicricht/APP-APP/commit/3e919b79b9a6ed9b1ce9eaa1e834fd1e47d0bd77).
- [Kotlin QA 40094763773](https://github.com/Nicricht/APP-APP/actions/runs/40094763773): **success** con compilación, unitarias e instalación/arranque API 30 y 35.
- [Instrumented QA 40094763734](https://github.com/Nicricht/APP-APP/actions/runs/40094763734): **failure**, 276 de 277 pruebas aprobadas. Se descargó el artefacto `informes-android-instrumentado` ID `12561100899` y se revisó el XML de resultados.
- La falla se limitó a `ConfirmacionDuplicadosFormularioTest.retrocesoDeAndroidCierraDialogoSinRegistrarCaso`: `RootViewWithoutFocusException` de Espresso al llamar a `Espresso.pressBack()` sobre un diálogo que tiene el foco en otra ventana. Es una prueba antigua, no una excepción de la lógica semanal.
- Pruebas nuevas del seguimiento: `SeguimientoSemanalDbTest` **3/3**, `SeguimientoSemanalViewModelTest` **1/1** y `SeguimientoSemanalPantallaTest` **3/3**, todas aprobadas en ese mismo informe.
- Corrección aplicada a la prueba antigua: sustituir `Espresso.pressBack()` por una pulsación real `KEYCODE_BACK` inyectada a través de `InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync`, esperando primero a que Compose quede inactivo. Se mantiene la comprobación de que Atrás descarta la advertencia sin registrar el caso.
- No eliminar, ignorar ni desactivar pruebas. No modificar código productivo. Nuevo resultado instrumentado y JaCoCo **pendientes del commit con esta corrección**.
- El resultado instrumentado fallido no acredita cobertura nueva; la línea base verde anterior continúa en 3088/3095 líneas (99,77 %) y 980/1013 ramas (96,74 %).
- Solo rama `qa`, `main` y `entrega-01` intactas; sin comentarios añadidos al código.

## 2026-10-24: restauración de identidades, permisos ya concedidos y seguimiento sin historial visible

- Punto de partida verificado: [commit 0c3b8170](https://github.com/Nicricht/APP-APP/commit/0c3b817013d71a7170b0c85f294f296818fca911).
- Workflows verdes del mismo SHA: [Android Kotlin QA 40095884513](https://github.com/Nicricht/APP-APP/actions/runs/40095884513) e [instrumented QA 40095884692](https://github.com/Nicricht/APP-APP/actions/runs/40095884692), con instalación automatizada en API 30/35.
- JaCoCo instrumentado del mismo SHA: **3117/3125 líneas (99,74 %) y 992/1027 ramas (96,59 %)**. Restan ocho líneas y 35 ramas.
- Se investigó el XML real del artifact `informes-android-instrumentado` ID `12561370927` para localizar decisiones no cubiertas en sesión, perfil y SQLite.
- `PerfilFotoActividadCoberturaTest.kt`: se agregan pruebas de cámara con permiso ya otorgado, tanto foto guardada como captura cancelada. Se verifica que **no se vuelve a solicitar autorización**, y que el guardado se ejecuta solo cuando hay imagen.
- `SesionRestauracionTest.kt`: recuperación real del asegurado demo y del liquidador de Vehículos tras reiniciar el ViewModel, con mantenimiento de identidad, rol, especialidad y cierre de sesión.
- `SeguimientoSemanalDbTest.kt`: cuando todas las gestiones son internas, la regla de siete días se calcula desde el reporte original, sin reiniciar el plazo por acciones privadas ni filtrar casos de otra especialidad.
- `ContratosModelosVisualesTest.kt`: restauración de todos los asegurados conocidos, reconocimiento de cuenta local `USR_` y rechazo de códigos desconocidos o de liquidadores tratados como asegurados.
- Solo se modifican **cuatro archivos de pruebas instrumentadas y esta bitácora**. Sin cambios de código de producción, SQLite ni permisos Android, sin comentarios nuevos.
- Estado verificado para [5d02cfef](https://github.com/Nicricht/APP-APP/commit/5d02cfef8b07c462fd64f5db10a1501823c117a6): [Android Kotlin QA 40102197681](https://github.com/Nicricht/APP-APP/actions/runs/40102197681) e [instrumented QA 40102197652](https://github.com/Nicricht/APP-APP/actions/runs/40102197652), ambas **success**. La instalación/arranque pasó en API 30 y 35. JaCoCo instrumentado: **3119/3125 líneas (99,81 %) y 995/1027 ramas (96,88 %)**; faltan **6 líneas y 32 ramas**.
- `main` y `entrega-01` permanecen intocadas.

## 2026-10-24: cierre formal del commit 5d02cfef y APK

- Código de producción sin modificaciones respecto de [0c3b8170](https://github.com/Nicricht/APP-APP/commit/0c3b817013d71a7170b0c85f294f296818fca911); solo pruebas e instrumentación.
- Las seis pruebas nuevas pasaron y se confirmó JaCoCo instrumentado **3119/3125 líneas (99,81 %)** y **995/1027 ramas (96,88 %)**. Faltan seis líneas y 32 ramas para la meta interna.
- Artifact `SegurosBPO-debug-apk` de [Android Kotlin QA 40102197681](https://github.com/Nicricht/APP-APP/actions/runs/40102197681), ID `12563183155`. Archivo extraído: `app-debug.apk`, SHA-256 `c6afc0e0015419f8143fe003f2903b717138c12d609c4826dd3534c15714e1ab`. Se verificó el contenedor ZIP, `AndroidManifest.xml`, `resources.arsc` y DEX.
- Evidencia de instalación: trabajos `instalar-android (30)` e `instalar-android (35)` del mismo SHA completados correctamente. **No equivale a una prueba física en el dispositivo del equipo**.
- Diagnóstico de APK: permanece [issue #7](https://github.com/Nicricht/APP-APP/issues/7) por firma de debug variable entre runners. **No desinstalar una app con datos locales sin respaldo**.
- Seguimiento semanal local documentado como completado en [issue #9](https://github.com/Nicricht/APP-APP/issues/9); continúa el [issue #6](https://github.com/Nicricht/APP-APP/issues/6) de cobertura.
- Cambio de esta entrada: **solo documentación**, sin Kotlin ni alteración de `main` o `entrega-01`. Una nueva suite instrumentada de GitHub puede ejecutarse por dispararse en cualquier push a `qa`.

## 2026-10-24: regresión de gráficos, perfiles y plazos con pruebas funcionales

- Base de comparación: [5d02cfef](https://github.com/Nicricht/APP-APP/commit/5d02cfef8b07c462fd64f5db10a1501823c117a6), [Kotlin QA 40102197681](https://github.com/Nicricht/APP-APP/actions/runs/40102197681) e [instrumentado 40102197652](https://github.com/Nicricht/APP-APP/actions/runs/40102197652), ambas verdes. JaCoCo: **3119/3125 líneas (99,81 %) y 995/1027 ramas (96,88 %)**.
- El commit documental [bd3de296](https://github.com/Nicricht/APP-APP/commit/bd3de296ff833b2abb35ab96bebd57cf71fe1d27) solo cambió esta bitácora y disparó [instrumentación 40103276895](https://github.com/Nicricht/APP-APP/actions/runs/40103276895), que terminó **success** antes de publicar estas pruebas, manteniendo la cobertura inicial.
- Archivo nuevo `ResumenGraficosEstadosRealesTest.kt`: cuatro pruebas instrumentadas verifican gráfica vacía sin cifras inventadas, total de tres casos en distintas etapas y tipos, conjunto totalmente cerrado con cero abiertos y funciones de distribución/evolución sin datos.
- Ampliación `SesionCuentaRealCoberturaTest.kt`: dos pruebas sobre cuenta local eliminada después de iniciar sesión y credenciales rechazadas. Comprueban que el ViewModel no confirme modificación de teléfono/fotografía cuando no existe la cuenta, ni reemplace sesión válida con contraseña incorrecta.
- Ampliación `AreasYPlazosLimitesTest.kt`: comprobar comportamiento de `ControlPlazos.diasCorridos` con fecha actual por defecto, fecha antigua, fecha futura e inválida.
- Motivo: nuevas verificaciones de UI y persistencia para el informe JaCoCo actual, especialmente gráficos, errores del ViewModel y cálculo de plazos; no añadir tests únicamente para ejecutar bytecode irrelevante.
- **Sin cambios en el código de producción**, dependencias, permisos ni datos persistidos por la app. Tests sobre registros ficticios.
- Resultado real de [29e1c571](https://github.com/Nicricht/APP-APP/commit/29e1c57153dd8b8946a58f5c34f594c63d6d3c9e): [Android instrumented QA 40104316078](https://github.com/Nicricht/APP-APP/actions/runs/40104316078) **success** con todas las pruebas, JaCoCo **3120/3125 líneas (99,84 %) y 998/1027 ramas (97,18 %)**. [Kotlin QA 40104316083](https://github.com/Nicricht/APP-APP/actions/runs/40104316083) **failure** por el verificador de reinicio de Android 15, aunque compilación/unitarias e instalación API 30 aprobaron. Es incorrecto declarar este SHA completamente verde.
- Respetar ramas: cambios únicamente en `qa`. `main` y `entrega-01` intactas; ningún comentario nuevo en Kotlin.

## 2026-10-24: estabilizar verificación de instalación Android 15

- El commit `29e1c571` tiene pruebas instrumentadas verdes ([run 40104316078](https://github.com/Nicricht/APP-APP/actions/runs/40104316078)), con **3120/3125 líneas (99,84 %) y 998/1027 ramas (97,18 %)**. Faltan **5 líneas y 29 ramas**.
- Su [Kotlin QA 40104316083](https://github.com/Nicricht/APP-APP/actions/runs/40104316083) compiló y ejecutó unitarias correctamente, generó APK, e instaló/arrancó API 30 (**success**), pero falló la comprobación de segunda apertura en API 35. Job `120903539247`: `ERROR: null root node returned by UiTestAutomationBridge`, después `adb: error: failed to stat remote object '/sdcard/ventana-reinicio.xml'`. La lectura inicial del XML de acceso ya había aprobado.
- Cambio en `.github/scripts/verificar-instalacion.sh`: reintentar hasta cinco veces el volcado XML de la segunda apertura; borrar antes el volcado anterior, exigir `Correo electrónico` en el XML nuevo y verificar que el proceso de la app continúa vivo. No se acepta un XML viejo ni se oculta un cierre de la aplicación.
- Cambio en `.github/workflows/android-qa.yml`: disparar compilación e instalaciones al modificar los scripts de verificación `.github/scripts/**`, evitando que el arreglo de instalación quede sin pruebas.
- Cambio en `.github/workflows/android-instrumented.yml`: conservar activación manual e iniciar instrumentación automática para código, Gradle, scripts y workflows, no para commits exclusivamente documentales. Esto evita cancelar/gastar pruebas instrumentadas con cambios que no afectan a la APK.
- Código Android de producción, lógica SQLite y tests Kotlin: **sin cambios** en el arreglo de CI; no se excluyen clases del informe ni se baja el rigor de las verificaciones.
- Resultado de [c59d12a9](https://github.com/Nicricht/APP-APP/commit/c59d12a979855ccf4fbb47dfdd96592cb76f4c04): [Android Kotlin QA 40105437720](https://github.com/Nicricht/APP-APP/actions/runs/40105437720) **success**, incluida instalación/reinicio API 30 y 35; [Android instrumented QA 40105437664](https://github.com/Nicricht/APP-APP/actions/runs/40105437664) **success**. JaCoCo: **3120/3125 líneas (99,84 %) y 998/1027 ramas (97,18 %)**.
- Sin comentarios añadidos al código y sin alteraciones de `main` ni `entrega-01`.

## 2026-10-24: restauración segura de sesiones con datos SQLite no válidos

- Revisión de la cobertura instrumentada de `c59d12a9`: **99,84 % de líneas y 97,18 % de ramas**. El XML de JaCoCo muestra faltantes en restauración de sesión, cuentas SQLite, selección de estados y navegación. No se excluyen clases ni se modifican umbrales para aparentar el 100 %.
- Cambio de producción en `CuentaSesionViewModel`: al restaurar una cuenta real desde SQLite, capturar de forma controlada errores de lectura o conversión de rol y rechazar perfiles con rol `LIQUIDADOR` si el equipo no corresponde a una especialidad reconocida. Ante información dañada no se concede acceso ni se presenta como liquidador una identidad incompleta.
- Nuevo test instrumentado `RestauracionSesionCorruptaTest.kt` con cuatro casos: rol desconocido, liquidador sin especialidad, especialidad inexistente y liquidador real válido. Comprueba que las cuentas dañadas no restauran privilegios y que la recuperación legítima no se rompe.
- Ampliación de `CuentaDbCoberturaRealTest.kt`: migración idempotente desde versión actual sin borrar cuentas ni reiniciar contraseñas; validación exacta de ocho caracteres de teléfono y conservación del valor previo tras intento inválido.
- Los datos de prueba se identifican como `QA_SESION_CORRUPTA` y se limpian antes y después; no se modifican cuentas reales ni se suben claves o fotografías privadas.
- No se alteran esquemas ni migraciones existentes, permisos Android, reglas de siniestros o dependencias. Sin comentarios en código Kotlin. Modificaciones exclusivamente en rama `qa`; `main` y `entrega-01` permanecen intactas.
- **Estado:** pendiente de pruebas de compilación, instalación API 30/35 e instrumentadas para el commit que introduce este cambio. Registrar aquí los resultados medidos después de CI.

## Pendientes sin evidencia suficiente

1. Seguir midiendo JaCoCo por SHA completo y cubrir únicamente rutas reales en CuentaSesionViewModel, gráficos, perfiles y navegación.
2. El escenario de dos conexiones/hilos simultáneos quedó probado en `be7d1ea7` con suite instrumentada verde. Para usuarios distintos del mismo equipo haría falta ampliar el modelo local de liquidadores.
3. Ejecutar recorrido completo de la APK en dispositivo físico: cámara, galería, PDF, permisos, cierre de sesión, reinicio y persistencia.
4. Revisar seguridad de propiedad por cuenta, experiencia accesible en pantallas pequeñas y evidencia de colaboración del equipo.
5. Registrar cualquier error en GitHub Actions con run ID y SHA y corregirlo en `qa` antes de darlo por cerrado.

Referencias: [Plan maestro](PLAN_MAESTRO_FINALIZACION.md),
[Protocolo QA](PROTOCOLO_QA_OBLIGATORIO.md),
[Matriz de aceptación](MATRIZ_ACEPTACION_Y_PRUEBAS.md).
