# Auditoría de la pauta DSY1105 — Seguros BPO Android

**Fecha:** 24 de octubre de 2026 · **Base examinada:** rama `qa`, commit
[`64a6e885`](https://github.com/Nicricht/APP-APP/commit/64a6e8850aed81a518ae7c697f886c1c263906f4)
· **Puntaje:** no asignado; la evaluación le corresponde al docente.

Esta auditoría complementa [la guía maestra](GUIA_MAESTRA_DSY1105.md), cuyo
enfoque vigente es la **app Android local en Kotlin/Compose/MVVM/SQLite**, sin
backend Spring Boot, Retrofit, Java nuevo ni servicios remotos para esta etapa.
Se contrastaron archivos de producción, pruebas, historial de CI y límites de
lo que efectivamente está demostrado. No confundir presencia de código o
cobertura con un recorrido usable en un teléfono real.

| Criterio de la pauta | Máximo | Evidencia encontrada | Riesgo o verificación pendiente |
|---|---:|---|---|
| Interfaz, jerarquía y navegación | 15 | [`AppNavegacion.kt`](../app/src/main/java/cl/duocuc/segurosbpo/navigation/AppNavegacion.kt) tiene rutas para perfil, casos, reporte, detalle, historial, documentos y liquidador; [`ui/screens/`](../app/src/main/java/cl/duocuc/segurosbpo/ui/screens/) usa componentes Material 3. | Recorrer navegación y botón Atrás en dispositivo, probar texto ampliado y pantallas pequeñas; no hay evidencia de usabilidad real adjunta a esta auditoría. |
| Formularios completos | 15 | [`FormularioScreen.kt`](../app/src/main/java/cl/duocuc/segurosbpo/ui/screens/FormularioScreen.kt) muestra errores junto a descripción, fecha y comuna, con confirmación; [`CasosLiquidadorScreen.kt`](../app/src/main/java/cl/duocuc/segurosbpo/ui/screens/CasosLiquidadorScreen.kt) cubre gestiones, solicitudes y cierres y bloquea acciones inválidas. | En la base auditada, [`AccesoScreen.kt`](../app/src/main/java/cl/duocuc/segurosbpo/ui/screens/AccesoScreen.kt) muestra errores al pie, no junto a cada campo. Algunos formularios del liquidador deshabilitan botones sin un mensaje junto al campo. Se inició una corrección acotada del registro en la PR [#11](https://github.com/Nicricht/APP-APP/pull/11); no darla por aprobada sin su CI. |
| Validación lógica y reactividad | 10 | [`ValidadorSiniestro.kt`](../app/src/main/java/cl/duocuc/segurosbpo/model/ValidadorSiniestro.kt), [`ReglasEtapa.kt`](../app/src/main/java/cl/duocuc/segurosbpo/model/ReglasEtapa.kt), [`FormularioViewModel.kt`](../app/src/main/java/cl/duocuc/segurosbpo/viewmodel/FormularioViewModel.kt) y [`SiniestrosViewModel.kt`](../app/src/main/java/cl/duocuc/segurosbpo/viewmodel/SiniestrosViewModel.kt) concentran reglas, estado y persistencia SQLite. | Verificar cambios de estado en recorrido completo con ambos perfiles, especialmente datos precargados/migrados y cambios sucesivos de sesión. |
| Animaciones | 10 | `AnimatedVisibility` en [`FormularioScreen.kt`](../app/src/main/java/cl/duocuc/segurosbpo/ui/screens/FormularioScreen.kt) y [`MisSiniestrosScreen.kt`](../app/src/main/java/cl/duocuc/segurosbpo/ui/screens/MisSiniestrosScreen.kt), progreso animado en [`ResumenGraficosCasos.kt`](../app/src/main/java/cl/duocuc/segurosbpo/ui/components/ResumenGraficosCasos.kt). | Comprobar fluidez y movimiento reducido/accesibilidad en emulador/teléfono. No afirmar que todas las transiciones de navegación están animadas. |
| Clases, herencia y polimorfismo | 15 | Modelos de dominio Kotlin, `SQLiteOpenHelper`, ViewModels y una interfaz de negocio real: [`ClasificadorArea`](../app/src/main/java/cl/duocuc/segurosbpo/model/AreasSiniestro.kt) implementada por Vehículos y Hogar; [`SiniestroDbHelper.kt`](../app/src/main/java/cl/duocuc/segurosbpo/data/SiniestroDbHelper.kt) utiliza la especialidad para filtrar casos. | Presentar en la defensa un caso práctico del despacho por interfaz; no confundir herencia de Android (`AndroidViewModel`, `SQLiteOpenHelper`) con toda la evidencia de POO. |
| GitHub, Trello y colaboración | 20 | Rama `qa`, guía maestra, bitácora, issues, PR y workflows verificables; tablero de seguimiento: [Trello Desarrollo de Aplicaciones](https://trello.com/b/ypOaHwPf/desarrollo-de-aplicaciiones). | **Crítico para puntaje:** en el historial de commits inspeccionado predominó una única identidad autora; no se comprobó un commit/PR auténtico del segundo integrante. Tampoco se confirmó asignación o cierre de las tarjetas de colaboración. No falsificar autorías ni marcar colaboración demostrada sin aportes reales. |
| Dos o más recursos nativos | 15 | [`EvidenciasScreen.kt`](../app/src/main/java/cl/duocuc/segurosbpo/ui/screens/EvidenciasScreen.kt) usa `TakePicturePreview` y `GetContent` para imágenes y PDF; [`PerfilCuentaScreen.kt`](../app/src/main/java/cl/duocuc/segurosbpo/ui/screens/PerfilCuentaScreen.kt) también usa cámara/galería. [`ProcesadorEvidencia.kt`](../app/src/main/java/cl/duocuc/segurosbpo/data/ProcesadorEvidencia.kt) limita formatos/tamaño antes de SQLite. | Cámara física, denegación de permiso, cancelación de selector, PDF real y persistencia después de cerrar/reabrir app necesitan verificación manual; `TakePicturePreview` no garantiza resolución original. |

## Evidencia automatizada ya comprobada en la base

- [Android Kotlin QA #40136075333](https://github.com/Nicricht/APP-APP/actions/runs/40136075333): ejecución verde para `eeb8e3c4` (padre del commit documental auditado), incluyendo tests unitarios, compilación e instalación/reinicio de APK debug en emuladores API 30 y 35.
- [Android instrumented QA #40136075394](https://github.com/Nicricht/APP-APP/actions/runs/40136075394): ejecución verde sobre el mismo código. Informe de esa ejecución: **3137/3140 líneas (99,90 %) y 1038/1057 ramas (98,20 %)**.
- La cobertura alta **no** sustituye controles visuales, accesibilidad, hardware real, colaboración de dos integrantes ni nota del docente. Tampoco equivale al 100 % de cobertura; quedan tres líneas y diecinueve ramas sin cubrir en esa ejecución.

## Riesgos de alcance respecto del documento rector

1. La guía indica **identidad simulada sin login real** y RNF-08 **no guardar contraseñas**. El código vigente incluye un flujo opcional de creación/inicio de cuenta con contraseña de demostración local en `AccesoScreen`/`CuentaDbHelper`. Hay que explicitar esta desviación en la defensa y decidir con el equipo si se mantiene como acceso *solo local* o se elimina antes de entregar. Esta auditoría **no autoriza** introducir un servidor ni complejidad de autenticación.
2. `docs/ETAPA_1_ANDROID.md` y `docs/CONTROL_FINAL_100_PUNTOS.md` contienen afirmaciones históricas que ya no describen todo el código de `qa`. Para reportar el estado actual, usar esta inspección y las ejecuciones CI vinculadas a SHA, no las casillas viejas como veredicto.
3. La puntuación máxima de colaboración **depende de acciones del compañero**. Un bot o un commit firmado por una sola persona no reemplaza aportes independientes.

## Siguiente evidencia que debe reunir el equipo

1. Cerrar la PR #11 solo si sus tests de interfaz y unitarios pasan; anexar captura del campo con error y tras corregirlo.
2. Grabar recorrido en emulador **y dispositivo real**: asegurado → caso → evidencia → liquidador → etapa → aviso/historial; incluir errores, permisos y accesibilidad.
3. Solicitar al segundo integrante un cambio genuino, con rama, commit propio, revisión en PR y tarjeta Trello vinculada; no fabricar evidencias de colaboración.
4. Mantener `main` y `entrega-01` intactas hasta aprobación expresa de la entrega.
