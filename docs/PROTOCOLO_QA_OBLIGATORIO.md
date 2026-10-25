# Protocolo QA obligatorio de Seguros BPO

**Vigencia:** desde 23-10-2026.  
**Repositorio:** Nicricht/APP-APP.  
**Rama unica autorizada para cambios de desarrollo:** qa.  
**Ramas protegidas por acuerdo del equipo:** main y entrega-01. No actualizarlas, borrarlas ni fusionar sin revision y autorizacion explicita.

## 1. Regla no negociable

Todo cambio funcional se realiza, prueba y documenta en qa antes de considerar su promocion. La rama qa es el entorno obligatorio de integracion y verificacion. Ninguna falla se considera resuelta sin un nuevo resultado verificable sobre el commit exacto. Una APK generada o un porcentaje alto de cobertura no sustituyen una ejecucion funcional Android.

**Estado constatado en linea base qa 3b9cd9bc:** [Android instrumented QA run 40002987884](https://github.com/Nicricht/APP-APP/actions/runs/40002987884) fallo dentro de la tarea :app:connectedDebugAndroidTest, con pruebas fallidas. El job completo incluyo SDK y emulador arrancados. Descargar informes-android-instrumentado de este run, identificar nombres de test, stack traces y causa raiz. No declarar QA verde hasta repetir.

La APK del proyecto de referencia del profesor es un workflow DIFERENTE: no demuestra que Seguros BPO sea instalable ni funcione. La anterior validacion verde de otra revision tampoco certifica el HEAD actual.

## 2. Secuencia obligatoria por funcionalidad

1. Leer [Plan maestro](PLAN_MAESTRO_FINALIZACION.md), caso y codigo vigente de qa. Identificar backlog ID, riesgo, modelos, pantallas y pruebas afectadas.
2. Confirmar SHA inicial y estado reciente de los workflows. Si hay rojo, clasificar fallo existente antes de mezclar nuevos cambios.
3. Trabajar sobre qa: conservar main y entrega-01. Si se requiere feature branch, basarla en qa y fusionar primero a qa, nunca directo a main.
4. Hacer cambio pequeno y verificable, manteniendo convenciones del profesor (Kotlin, Compose, MVVM, SQLite), sin comentarios en codigo fuente.
5. Añadir o actualizar tests que ejerciten caminos positivos, negativos, bordes y reglas de roles; revisar migracion si hay cambios de SQLite.
6. Ejecutar validacion local cuando sea posible y revisar logs. No ocultar excepciones ni introducir pruebas triviales para inflar cobertura.
7. Commit en qa con descripcion precisa y autor real; comprobar SHA tras subir.
8. Revisar Android Kotlin QA y Android instrumented QA, asociados al SHA exacto. Ante falla, diagnosticar y corregir EN qa; repetir.
9. Registrar evidencia Android fisica/emulador del recorrido y validar instalacion APK cuando aplique.
10. Actualizar matriz de aceptacion y estado del backlog. No marcar terminado sin pruebas reproducibles.
11. Solo despues de todas las verificaciones verdes y revision de ambos integrantes proponer avance a main. entrega-01 solo cambia en una entrega autorizada.

## 3. Comandos de validacion del repositorio

Revisar que la version instalada de Android SDK/JDK sea compatible con la configuracion Gradle vigente. El workflow Android Kotlin QA de la rama qa usa Java 25 y plataforma android-37.2.

~~~bash
git fetch origin
git switch qa
git pull --ff-only origin qa
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest :app:createDebugUnitTestCoverageReport --no-daemon
~~~

Para instrumentadas, con emulador arrancado y API requerida:

~~~bash
./gradlew :app:createDebugAndroidTestCoverageReport --no-daemon
~~~

Para rastrear fallas:
- Revisar app/build/reports/tests/testDebugUnitTest y app/build/reports/androidTests/connected.
- Revisar informes de JaCoCo en app/build/reports/coverage/test/debug y app/build/reports/coverage/androidTest.
- Revisar archivos XML de resultados y stack traces, no solo la linea BUILD FAILED.
- Capturar ID de run, SHA, cantidad de tests, errores, lineas/ramas cubiertas, y pruebas manuales.

Los workflows vigentes de qa son:
- .github/workflows/android-qa.yml: compilacion de Seguros BPO, test unitario, compilacion androidTest, cobertura y APK.
- .github/workflows/android-instrumented.yml: emulador Android y cobertura instrumentada.
- .github/workflows/mi-registro-profesor-apk.yml: proyecto del profesor; NO forma parte de la validacion de nuestra funcionalidad.

**Advertencia:** el workflow android-qa.yml filtra paths y puede no ejecutarse ante un cambio exclusivo de documentos. El workflow instrumentado corre en push qa. Un push de documentacion no certifica codigo ni obliga a declarar verde lo que no lo esta.

## 4. Gates de aceptacion

| Gate | Condicion | Si falla |
|---|---|---|
| G0 Integridad Git | Cambio presente en qa, SHA identificado, main y entrega-01 intactas | Detener promocion |
| G1 Compilacion | assembleDebug y assembleDebugAndroidTest correctos | Corregir compilacion |
| G2 Reglas | Unit tests validadores/etapas/permisos correctos | Corregir dominio |
| G3 Persistencia | SQLite tests, migraciones y transacciones correctas | Bloquear cambios |
| G4 Instrumentadas | Tests Compose/navigation/SQLite Android verdes en HEAD | Analizar reportes, corregir, repetir |
| G5 Cobertura | Informes de lineas y ramas medibles revisados; objetivo interno 100% cuando sea viable | Anotar huecos, añadir tests utiles, no falsear |
| G6 Seguridad | Propiedad, rol, especialidad, sesion, gestiones internas y fugas comprobadas | Bloqueante |
| G7 Dispositivo | Camara, galeria, PDF, permisos, reinicio y UI comprobados donde aplique | Pendiente, no terminado |
| G8 APK | Instala, abre, no es testOnly, icono propio, rutas funcionales API 30 y 35 | No entregar |
| G9 Equipo | Evidencia real de issue/Trello/autor y revision del trabajo | No declarar colaboracion |
| G10 Entrega | Solo cambios aprobados desde qa, con nota de cambios y plan de reversion | No promover |

## 5. Cobertura 100%: meta interna, nunca sustituto de calidad

El equipo busca 100% lineas y 100% ramas del codigo propio de negocio donde sea tecnicamente medible, mediante JaCoCo. La pauta documentada de 100 puntos NO pide expresamente esa cifra: es una meta voluntaria.

Para medir: distinguir tests locales y tests instrumentados y definir alcance auditable; informar cubiertas/perdidas por clase y modulo. No prometer 100% por capturar una pantalla, ni sumar porcentajes incompatibles. No excluir codigo dificil solo para subir porcentaje y no modificar produccion con caminos artificiales. Los componentes Compose generados y mediciones de rama pueden requerir explicaciones y exclusiones justificadas por herramienta; documentar siempre metodologia.

Se exige tambien prueba de comportamiento: ejecutar acciones reales, verificar efectos persistidos, resultados y permisos. Un test que solo invoca una funcion sin aserciones no prueba negocio.

## 6. Matriz minima que debe ejecutarse por tipo de cambio

| Tipo | Pruebas minimas |
|---|---|
| Usuario, cuenta, login | Alta, duplicado, datos invalidos, sesion, logout, privilegio, otros usuarios |
| Asignacion | Area, Recibido, caso tomado por otro, doble toma, transaccion rollback |
| Siniestro | Validez, duplicado advertido, fuera de plazo, numero unico, propietario |
| Etapas | Normal, cierre anticipado legal, transicion ilegal, pendiente bloqueante, cerrado |
| Evidencias | JPG/PNG/WEBP/PDF, MIME, 0/5MB, 15/16, permiso, cancelacion, cerrado |
| Avisos | Eventos correctos, privacidad, leido/no leido, no gestion interna |
| Estadisticas | Recuento real, cero, filtro por usuario y liquidador, consistencia tras cambio |
| SQLite | Fresh install, cada version migrable, sin perdida, FK, atomicidad |
| UI | Pantalla pequena, teclado, accesibilidad, scroll, retroceso, animaciones |
| APK | Package, manifest, firma, no testOnly, instalar/lanzar/reabrir, Android 11 |

Referencia detallada: [Matriz de pruebas](MATRIZ_ACEPTACION_Y_PRUEBAS.md).

## 7. Proteccion de ramas y coordinacion

Actualmente qa, main y entrega-01 no aparecen protegidas en el estado consultado el 23-10-2026. Este protocolo es una politica del equipo, NO una branch protection que GitHub ya imponga. Si existen permisos, configurar reglas reales en GitHub para main/entrega-01 con PR, revision y status checks de Seguros BPO; nunca asumirlas activadas.

Commits autenticos por ambos integrantes y actividades Trello reales. No inventar autorias. Preferir entregas pequenas y commits identificables. No hacer force push, reset destructivo, borrados de datos ni alteracion de ramas de entrega sin autorizacion.

## 8. Evidencia final minima

Guardar y referenciar: commit SHA; workflows de ese SHA; resumen JaCoCo; test report y casos E2E; pruebas de crear cuenta/iniciar sesion/roles; asignacion por area; documento solicitado, enviado y bloqueo; historial; dos paneles de graficos; capturas de camara/galeria; instalacion APK en Android 11 y otra API moderna; capturas de GitHub y Trello de contribuciones reales.

**Checklist antes de cualquier promocion:**
- [ ] Ultima ejecucion Android Kotlin QA verde para el SHA de codigo.
- [ ] Ultima ejecucion Android instrumented QA verde para el mismo SHA.
- [ ] Sin fallos de roles, privacidad, BD o migraciones.
- [ ] Reporte de cobertura y huecos claros.
- [ ] Pruebas de dispositivo y de APK aportadas.
- [ ] Evidencias colaborativas autenticadas.
- [ ] Revision y autorizacion expresa para tocar main.
- [ ] entrega-01 sin cambios accidentales.

**Situacion actual:** este protocolo es documentacion; por si solo NO arregla el run rojo ni demuestra 100% de cobertura.