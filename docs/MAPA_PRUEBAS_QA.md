# Mapa de pruebas y riesgos de evaluación

Rama **`qa`**. Aplicación base copiada de **`main`**, con pruebas adicionales que no se incluyen en la entrega Android del profesor.

## Evidencias reproducibles

| Área | Prueba o verificación | Qué demuestra | Qué NO demuestra |
|---|---|---|---|
| Compilación | `.github/workflows/android-qa.yml` con `assembleDebug` | La app se compila y se puede generar APK | Que todas las pantallas sean cómodas o bonitas |
| JUnit validaciones | `ValidadorSiniestroTest.kt` | Fechas, compañías y campos obligatorios | Que cada error se vea bien en todas las pantallas |
| JUnit evidencias | `ValidadorEvidenciaTest.kt` | MIME, límite de 5 MB, 15 archivos | Que cámara o galería del teléfono abran correctamente |
| JUnit etapas | `ReglasEtapaTest.kt` | Secuencia, cierre, mensajes | Solicitudes de documentos pendientes de extremo a extremo |
| Integración SQLite | `SiniestroDbIntegrationTest.kt` | Persistencia, propietario, gestión interna, avance, cierre, migración v1→v4, conversión/lectura de foto | Calidad visual, permisos del dispositivo |
| E2E básico | `NavegacionE2ETest.kt` | Formulario y perfil de liquidador se abren y regresan | Recorrido completo de todos los formularios |
| UI y errores | `RecorridosPresentacionTest.kt` | Errores de reporte, acceso al historial, tipo de documento obligatorio | Captura física y selección real de imágenes |
| Instrumentación | `.github/workflows/android-instrumented.yml` con `connectedDebugAndroidTest` | Las pruebas Android se ejecutan en un emulador en GitHub | Que un teléfono del alumno tenga iguales permisos y comportamiento |
| Colaboración | Historial GitHub + Trello | Evidencia de dos aportes humanos distintos, si existen | La ejecución de CI no demuestra contribuciones individuales |

## Últimos resultados verificados antes de la sincronización documental

- **QA Android/JUnit aprobado:** [ejecución #39626365190](https://github.com/Nicricht/APP-APP/actions/runs/39626365190).
- **QA instrumentado en emulador aprobado:** [ejecución #39626365414](https://github.com/Nicricht/APP-APP/actions/runs/39626365414).
- **Compilación de la app de presentación `main` aprobada:** [ejecución #39623835586](https://github.com/Nicricht/APP-APP/actions/runs/39623835586).
- Cuando se actualice esta rama, verificar **las ejecuciones nuevas**: [GitHub Actions](https://github.com/Nicricht/APP-APP/actions).

## Checklist de aceptación QA

- [x] Tests y herramientas avanzadas separados de la aplicación `main`.
- [x] Pruebas de integración y UI ejecutadas correctamente en una versión de `qa` equivalente a la aplicación auditada.
- [ ] Confirmar último `main` y último `qa` verdes tras cualquier nuevo cambio de código.
- [ ] Automatizar o documentar prueba real de apertura/cancelación de cámara y galería; el test actual cubre el guardado de una imagen generada, no el uso físico de cámara.
- [ ] Evaluar legibilidad en pantallas pequeñas y accesibilidad.
- [ ] Corregir reglas del caso pendientes antes de reclamar su implementación.
- [ ] Adjuntar evidencia auténtica de GitHub/Trello de los dos integrantes.

**Sin Playwright, sin JaCoCo 100 % atribuido al profesor y sin microservicios**. El criterio que rige la calidad es la pauta oficial y el funcionamiento Android demostrado.

La lista general está en [Control de los 100 puntos](CONTROL_FINAL_100_PUNTOS.md). La guía de explicación está en [Guía de defensa oral](GUIA_DEFENSA_ORAL.md).
