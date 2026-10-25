> **ALCANCE DE LA EVALUACIÓN ACTUAL:** En esta fase desarrollamos únicamente la aplicación Android local al estilo de **MiRegistroAvatar**, usando Kotlin, Compose, MVVM, navegación y SQLite. **Spring Boot, Retrofit, API REST y microservicios son para la tercera evaluación**, no se implementan ahora. Ver [estado de la etapa 1](docs/ETAPA_1_ANDROID.md).

# GUÍA MAESTRA DEL PROYECTO APP-APP
## DSY1105 · Desarrollo de Aplicaciones Móviles · Seguimiento de siniestros BPO

> **Documento rector del equipo.** Versión inicial: 20-10-2026. Estado de implementación: **por verificar**. El repositorio APP-APP estaba vacío al crear esta guía. Las casillas son tareas, **no afirmaciones de trabajo ya realizado**.

### Propósito de esta guía

Construir un MVP Android para **Servicios Corporativos Andes SpA** que permita a una persona asegurada seguir sus siniestros, revisar su historial, entregar evidencia y recibir avisos; y que permita al liquidador gestionar los casos asignados. Debe usar las tecnologías fijadas por el caso y la forma de organizar y programar que enseña el profesor en **MiRegistroAvatar**.

**Regla fundamental:** conservar el enfoque didáctico del profesor (Kotlin, Compose, MVVM, estados, navegación, componentes y SQLite) pero **no** limitarse a cambiar nombres en su proyecto: nuestro dominio es siniestros, no el registro de usuarios. **No reemplazar código funcional sin antes revisarlo y probarlo.**

### Fuentes y jerarquía de requisitos

1. **Caso_DSY1105_SEGUROS_BPO.pdf**, sección 5: tecnologías y límites académicos.
2. **Anexos_Seguros_BPO_Andes.docx**, versión 1.0: RF, RNF, HU, RN, perfiles, datos ficticios y alcance.
3. **Respuestas_Consultas_Seguros_BPO_Andes_Lote1.docx**, versión 1.0: aclaraciones 1 a 10 que complementan los anexos.
4. **MER_Siniestros_Completo.pdf**: modelo de referencia 3FN de **23 tablas y 37 claves foráneas**; **no obliga a implementar las 23 tablas en el MVP**.
5. **Pauta de evaluación DSY1105 aportada por el estudiante**, siete criterios, total **100 puntos**.
6. **Código del profesor** en MiRegistroAvatar.zip; MiRegistroCompleto.zip y GraficosApp.zip son material complementario.

**Criterios de interpretación:** distinguir requisito oficial, ejemplo del profesor y propuesta técnica del equipo. Ante un conflicto entre decisiones nuestras y documentos oficiales, prevalece el caso y sus aclaraciones. No se afirma que una función esté terminada hasta tener evidencia.

---

## 1. Alcance, usuarios y prioridad del MVP

### Perfiles

- **P-01 Asegurado, obligatorio:** reporta un siniestro, ve **solo** sus casos, su etapa, historial y documentos, adjunta evidencia y recibe avisos.
- **P-02 Liquidador, obligatorio:** ve **solo** los casos asignados, registra gestiones, solicita documentos, cambia etapa y registra el cierre.
- **P-03 Analista de administración, deseable RF-17:** asigna y reasigna casos; puede omitirse del MVP dejando **datos de ejemplo ya asignados** para la demostración.
- Los ejecutivos telefónicos y las compañías aseguradoras **no utilizan la aplicación** en este alcance.

**No desarrollar un login real.** La selección de usuario y perfil será simulada con datos ficticios (supuesto S-02), pero las vistas, las consultas y las acciones deben estar acotadas al perfil seleccionado. Esto **no equivale a autenticación segura de producción**.

### Prioridad oficial (Aclaración 1)

1. **P1:** consulta de siniestros, etapas, detalle e historial (RF-04, RF-05, RF-06).
2. **P2:** documentos solicitados, pendientes y entrega de evidencia (RF-07, RF-08).
3. **P3:** avisos de cambios relevantes (RF-09).
4. **P4:** funciones del liquidador: gestiones, solicitudes y cambios de etapa (RF-10, RF-11, RF-12).
5. **P5:** reporte de siniestro nuevo y número generado (RF-02, RF-03).
6. **Transversal desde el inicio:** identificación simulada y separación de datos (RF-01), almacenamiento local, integración y calidad.
7. **Solo después de cumplir RF-01 a RF-12:** deseables RF-13 a RF-18.

**Criterio de diseño:** etapa visible de inmediato, lenguaje fácil de entender, próximo paso visible y documentación que el asegurado debe entregar. No gamificación ni mensajes alarmistas.

---

## 2. Pauta de evaluación: objetivo 100/100

La tabla reproduce los **siete criterios entregados por el estudiante**. Las columnas «evidencia» y «verificación» son un plan del equipo, no contenido literal de la pauta.

| Criterio | Máximo | Muy buen desempeño (qué conseguir) | Evidencia objetiva antes de entregar |
|---|---:|---|---|
| Interfaz, jerarquía y navegación | **15** | UI estructurada, elementos bien distribuidos, navegación fluida, coherencia visual y usabilidad | Recorrido sin callejones sin salida; capturas y video de pantallas clave |
| Formularios completos | **15** | **Todos** los formularios necesarios, validación visual por campo, iconografía, mensajes claros y accesibilidad | Casos válidos e inválidos de reporte, adjunto, gestión, solicitud, cambio de etapa y cierre |
| Validación lógica y reactividad | **10** | Reglas centralizadas fuera de la UI; cambios de estado reflejados correctamente; arquitectura desacoplada | ViewModels + estados, tests de validadores y actualización inmediata en Compose |
| Animaciones | **10** | Animaciones funcionales que aporten fluidez y retroalimentación, sin distraer | Transiciones de navegación, cambio de etapa, carga/éxito/error y estados vacíos |
| Clases, herencia y polimorfismo | **15** | Uso integral y efectivo de clases, herencia y polimorfismo | Modelos, interfaces y tipos especializados con uso real; arquitectura modular y persistencia |
| GitHub, Trello y colaboración | **20** | Commits de **ambos integrantes**, issues resueltos, tareas colaborativas y planificación trazable | Historial auténtico de contribución, PR revisados, issues relacionados y Trello con evidencias |
| Dos o más recursos del dispositivo | **15** | Recursos nativos usados de forma segura y funcional, integrados al flujo de la app | Demostración de **cámara + selección de imágenes/archivos**; permisos o autorizaciones según API, cancelación y denegación |

**Puntaje máximo: 100 puntos.** Según la pauta suministrada: criterios de 15 puntos (15/9/4,5/0); de 10 (10/6/3/0); colaboración (20/12/6/0). La descripción de ciertos niveles «incipiente» no fue entregada; no inventarla. **Ningún puntaje está garantizado:** depende de evaluación docente y evidencia.

**Importante:** el caso exige pruebas unitarias del backend, pero **la pauta compartida no fija JaCoCo 100 % de líneas y ramas**. Si el equipo decide proponerse esa meta como control interno, no atribuirla al profesor ni al caso sin otra fuente.

### Lista de control para aspirar al máximo

- [ ] **15/15 UI:** Material 3, tipografía legible, navegación real, estados vacío/cargando/error y retroceso correcto.
- [ ] **15/15 formularios:** campos requeridos, errores inline, mensajes de éxito, confirmaciones y prevención de duplicados.
- [ ] **10/10 validaciones:** ViewModel y funciones reutilizables; nada de reglas críticas solo en botones.
- [ ] **10/10 animaciones:** útiles, discretas, accesibles y probadas en teléfono/emulador.
- [ ] **15/15 POO:** relaciones e interfaces justificadas, sin herencia artificial; persistencia real.
- [ ] **20/20 colaboración:** dos personas con aportes verificables, Trello, GitHub Issues y PR.
- [ ] **15/15 recursos:** cámara y selector de galería/documentos con flujo completo, resultados persistidos y errores gestionados.

---

## 3. Los 12 requisitos funcionales obligatorios (RF)

**Regla de entrega:** un RF se marca completo solo si funciona en UI, ViewModel, persistencia/API según corresponda y tiene evidencia de prueba.

| ID | Requisito oficial resumido | Implementación que debemos mostrar | Estado |
|---|---|---|---|
| **RF-01** | Identificar al asegurado sin login real; permitirle ver y gestionar solo lo propio | Selector de usuario ficticio; filtros y controles por propietario/perfil | ☐ |
| **RF-02** | Reportar siniestro de vehículo u hogar con compañía, tipo, fecha de ocurrencia, comuna y descripción | Formulario completo, validado, con guardado y envío | ☐ |
| **RF-03** | Entregar número de siniestro al finalizar | Identificador único visible en confirmación y detalle | ☐ |
| **RF-04** | Mostrar todos los siniestros del asegurado, etapa y última actualización | Lista ordenada, abiertos primero, con estado vacío | ☐ |
| **RF-05** | Detalle: número, compañía, tipo, fechas, etapa, asignado y próximo paso | Pantalla de detalle clara y actualizada | ☐ |
| **RF-06** | Historial cronológico en lenguaje simple | Línea de tiempo con gestiones visibles y fecha/hora | ☐ |
| **RF-07** | Adjuntar imagen o PDF, indicando tipo de documento | Cámara y selector de archivo; validación, envío y asociación al caso | ☐ |
| **RF-08** | Mostrar documentos solicitados, entregados y pendientes | Sección de documentos con estado y fecha de solicitud | ☐ |
| **RF-09** | Avisar cambios relevantes sin exigir consulta manual | Notificación observable al producirse el evento + pantalla de avisos | ☐ |
| **RF-10** | Liquidador ve solo asignados y registra gestiones | Bandeja propia y formulario de gestión visible/interna | ☐ |
| **RF-11** | Liquidador avanza etapas y solicita documentos | Acciones con validaciones de RN y confirmación | ☐ |
| **RF-12** | Registrar acciones relevantes con fecha, hora y responsable | Historial/auditoría al reportar, asignar, cambiar etapa, solicitar/recibir documento y cerrar | ☐ |

### Funciones deseables, NO obligatorias antes de terminar RF-01 a RF-12

| ID | Deseable | Estado |
|---|---|---|
| RF-13 | Asegurado desiste antes de liquidación y explica motivo | ☐ |
| RF-14 | Consultas escritas y respuestas dentro del siniestro | ☐ |
| RF-15 | Plazo estimado y días transcurridos en etapa | ☐ |
| RF-16 | Documentos habituales según tipo de siniestro antes de solicitarlos | ☐ |
| RF-17 | Analista asigna/reasigna considerando equipos y carga de trabajo | ☐ |
| RF-18 | Evaluación final de experiencia (1 a 5 y comentario opcional) | ☐ |

**Historias de usuario oficiales:** HU-01 Reportar, HU-02 Mis siniestros, HU-03 Detalle/próximo paso, HU-04 Historial, HU-05 Adjuntar, HU-06 Documentos pendientes, HU-07 Avisos, HU-08 Sin conexión, HU-09 Gestión, HU-10 Avanzar etapa, HU-11 Solicitar documento, HU-12 Asignación (deseable). Utilizar sus criterios de aceptación del anexo como pruebas manuales.

---

## 4. Reglas de negocio: RN-01 a RN-18

Todas las reglas aplicables deben validarse **también en el backend**, no únicamente en Compose.

| Código | Regla y prueba imprescindible |
|---|---|
| **RN-01** | Secuencia **Recibido → En evaluación → En liquidación → Cerrado**. No retroceder ni saltar etapas, salvo cierres anticipados expresamente permitidos. |
| **RN-02** | Cierre con resultado **Aprobado / Rechazado / Desistido** y mensaje de cierre obligatorio. |
| **RN-03** | Validar dígito verificador del RUT ficticio del asegurado. No solicitar datos reales. |
| **RN-04** | Fecha de ocurrencia no puede ser posterior al reporte ni a la fecha actual. |
| **RN-05** | Más de **30 días corridos** hasta el reporte: aceptar pero marcar **«Reporte fuera de plazo»**; NO rechazar automáticamente. |
| **RN-06** | Número único con formato **SIN-AA-NNNNNN**, sin reutilización, por ejemplo SIN-26-004801. |
| **RN-07** | Nuevo siniestro = «Recibido» sin liquidador; al asignarlo pasa a «En evaluación». |
| **RN-08** | Casos Vehículos solo a liquidador del equipo Vehículos; Hogar solo a equipo Hogar. |
| **RN-09** | Impedir «En liquidación» cuando hay documentos solicitados «Pendiente». |
| **RN-10** | Adjuntos solo **imagen o PDF**, **hasta 5 MB por archivo**, **máximo 15 adjuntos por siniestro**, con tipo de documento. |
| **RN-11** | Caso cerrado: no acepta adjuntos. Un adjunto enviado no se borra por el asegurado; puede entregar una nueva versión con comentario. |
| **RN-12** | Desistimiento del asegurado (deseable RF-13): solo en «Recibido» o «En evaluación», con motivo. |
| **RN-13** | Toda gestión con fecha, hora, responsable, descripción y bandera visible/interna; **nunca** mostrar gestiones internas al asegurado. |
| **RN-14** | Mostrar al asegurado únicamente nombre de pila + inicial del liquidador y equipo. Nunca correo ni teléfono personal. |
| **RN-15** | Documento pendiente a los **15 días corridos**: recordatorio e historial; a los **30 días**, el liquidador **puede** cerrar como Desistido (no automáticamente). |
| **RN-16** | Al menos una gestión visible cada **7 días corridos** por siniestro abierto, aunque sea «Sin novedades». |
| **RN-17** | Días en etapa: corridos desde último cambio. Plazo de evaluación: definido por la compañía en **días hábiles**. |
| **RN-18** | Compañía inactiva: sus casos se consultan, pero no se aceptan nuevos reportes de esa compañía. |

### Transiciones y permisos

| Etapa actual | Transiciones permitidas |
|---|---|
| Recibido | En evaluación; Cerrado **solo Desistido** |
| En evaluación | En liquidación; Cerrado **Rechazado o Desistido** |
| En liquidación | Cerrado **Aprobado o Rechazado** |
| Cerrado | Ninguna; no se reabre en el MVP |

| Acción | Asegurado | Liquidador | Analista deseable |
|---|---|---|---|
| Reportar | Sí, propio | No | No |
| Ver casos | Solo propios | Solo asignados | Todos |
| Ver gestiones internas | No | Sí | Sí |
| Adjuntar | Sí, si abierto | Sí | No |
| Solicitar documentos | No | Sí | No |
| Registrar gestiones | No | Sí | Solo internas |
| Cambiar etapa o cerrar | Solo desistimiento si RF-13 | Sí | No |
| Asignar/reasignar | No | No | Sí |

**Decisión real sobre la resolución:** la compañía aseguradora decide fuera de la app; el liquidador **registra** el cierre y su mensaje cuando esa decisión existe (Aclaración 7). No crear flujo adicional de aprobación.

---

## 5. Requisitos no funcionales: RNF-01 a RNF-10

| Código | Obligación | Cómo comprobarla |
|---|---|---|
| RNF-01 | Reporte en **menos de 5 minutos** para persona sin experiencia digital | Prueba de usabilidad cronometrada |
| RNF-02 | Ver etapa del caso en **menos de 30 segundos** desde abrir la app | Medición del recorrido de usuario |
| RNF-03 | Textos simples, cordiales, sin jerga de seguros | Revisión de pantallas y mensajes |
| RNF-04 | Legible y accesible para mayores/baja visión | Contraste, tamaños táctiles, escalado de fuente, no solo colores |
| RNF-05 | Sin Internet, ver última información conocida y fecha/hora de actualización | Modo avión tras sincronizar |
| RNF-06 | Si se corta Internet, conservar datos escritos en reporte o adjunto | Corte simulado y recuperación del borrador |
| RNF-07 | Jamás mostrar siniestros/documentos/datos de otros asegurados | Casos de prueba cruzados por usuario y verificaciones de API |
| RNF-08 | No solicitar ni almacenar datos médicos, bancarios ni contraseñas | Revisión de formularios, BD y payloads |
| RNF-09 | Estado e historial consistentes entre asegurado y liquidador | Recorrido integrado entre ambos perfiles |
| RNF-10 | Minimizar datos personales en pantalla, por ejemplo RUT parcialmente oculto | Inspección de vistas, notificaciones y capturas |

**Offline es requisito, no un extra.** Cargar información desde Room/SQLite y señalar «última actualización». Guardar borradores locales y, cuando proceda, operaciones pendientes con estado **pendiente de envío / enviado / rechazado**. Un documento **solo** pasa a «Entregado» después de que el backend confirme su recepción. No presentar datos en caché como si fueran «en tiempo real».

---

## 6. Las 10 aclaraciones oficiales de la empresa

1. **Prioridad:** consulta → documentación → avisos → liquidador → reporte; deseables al final.
2. **Próximo paso:** mostrar un mensaje específico según etapa y documentos pendientes.
3. **Posible duplicado:** si el mismo asegurado reporta igual compañía, tipo y fecha de un caso no cerrado, advertir con número existente; **permitir continuar** si confirma.
4. **Campos opcionales de reporte:** referencia del lugar, hora aproximada, terceros involucrados en vehículo, denuncia policial; si corresponde, anticipar solicitud de constancia.
5. **Avisos sí:** asignación, cambio de etapa, solicitud, recordatorio, cierre, respuesta a consulta si RF-14 existe. **No:** gestión visible rutinaria. **Nunca:** gestión interna. Aviso siempre indica siniestro.
6. **Prohibida la gamificación:** sin puntos, insignias, premios ni juegos.
7. **Cierre:** decisión externa de aseguradora; la registra el liquidador. Desistimiento con motivo cuando corresponda.
8. **Deseable:** destacar casos con más de 7 días sin gestión visible.
9. **Confirmación de recepción:** distinguir archivo pendiente de enviar y documento realmente recibido; si el caso cierra antes de llegar, rechazar e informar.
10. **Identidad visual:** no usar nombres/logos/colores de aseguradoras reales ni logo de Andes; cada caso debe mostrar su **compañía ficticia**; diseño sobrio, claro, estado comprensible también sin color.

**Texto de «próximo paso», según Aclaración 2:**
- Recibido sin asignado: «Estamos revisando tu reporte para asignarlo a un liquidador. No necesitas hacer nada por ahora».
- En evaluación con pendientes: «Necesitamos que nos envíes los documentos pendientes para continuar con tu caso».
- En evaluación sin pendientes: «Tu liquidador está revisando tu caso. Si necesitamos algo más, te avisaremos».
- En liquidación: «Tenemos todo lo necesario. Estamos definiendo la resolución con tu compañía de seguros».
- Cerrado: «Tu caso terminó. Revisa el resultado y el mensaje de cierre».

---

## 7. Pantallas y formularios que debemos desarrollar

| Pantalla / flujo | Perfil | Campos o contenido | Obligaciones vinculadas |
|---|---|---|---|
| Selección de usuario ficticio | Ambos | Seleccionar asegurado/liquidador de fixtures; indicador de sesión simulada | RF-01, RNF-07 |
| **Mis siniestros** | Asegurado | Tarjetas con número, compañía, tipo, etapa, última actualización; abiertos primero | RF-04, HU-02, RNF-02 |
| **Detalle del siniestro** | Ambos según permisos | Datos, etapa prominente, próximo paso, responsable en formato permitido | RF-05, RN-14 |
| **Historial** | Asegurado/liquidador | Gestiones ordenadas, fechas, responsables; filtrar internas para asegurado | RF-06, RF-12, RN-13 |
| **Documentos** | Asegurado | Pendientes, entregados, estado de envío, fecha de solicitud | RF-08, aclaración 9 |
| **Adjuntar evidencia** | Asegurado | Cámara, fotos y PDF; tipo obligatorio, tamaño y cantidad; confirmación | RF-07, RN-10, RN-11 |
| **Avisos** | Asegurado | Bandeja con siniestro, motivo, fecha, leído/no leído; navegación al detalle | RF-09, aclaración 5 |
| **Reportar siniestro** | Asegurado | Compañía, tipo, fecha, comuna, descripción; opcionales de aclaración 4 | RF-02, RF-03, RN-04 a RN-06, RN-18 |
| **Mis casos asignados** | Liquidador | Lista filtrada; estado, pendientes y acceso a gestionar | RF-10 |
| **Registrar gestión** | Liquidador | Descripción, visible/interna, fecha y responsable automáticos | RF-10, RF-12, RN-13 |
| **Solicitar documento** | Liquidador | Tipo, comentario, caso; genera pendiente y aviso | RF-11, RF-12 |
| **Avanzar etapa / cerrar** | Liquidador | Siguiente etapa válida, resultado y mensaje de cierre cuando aplique | RF-11, RN-01, RN-02, RN-09 |
| **Analista y asignación** | Deseable | Sin asignar, liquidador/equipo, carga | RF-17, RN-07, RN-08 |

### Validaciones visuales por formulario

- [ ] Etiquetas claras y campos obligatorios identificados antes de enviar.
- [ ] Errores **junto al campo** concreto, no solo mensajes genéricos.
- [ ] Acción bloqueada o confirmación adecuada cuando una regla no se cumple.
- [ ] Mientras guarda/envía: indicar carga, impedir doble envío y conservar estado.
- [ ] Mensaje claro al completar, rechazar, cancelar o perder conexión.
- [ ] Rechazo de futuro y fechas inconsistentes, RUT ficticio inválido, compañía inactiva, falta de descripción, archivo inválido, etapa prohibida, cierre sin resultado/mensaje.
- [ ] Aviso de posible duplicado sin impedir reporte si el usuario confirma.
- [ ] Accesibilidad: etiquetas semánticas, foco de errores, íconos con texto, suficiente contraste.
- [ ] No solicitar contraseñas ni construir formularios de login real.

---

## 8. Referencia directa al proyecto del profesor: MiRegistroAvatar

### Patrones reales localizados en el ZIP

| Archivo real del profesor (bajo app/src/main/java/cl/duocuc/miregistro/) | Qué enseña | Cómo lo adaptamos a Seguros BPO |
|---|---|---|
| **MainActivity.kt** | Punto de entrada de Compose y tema | Lanzar nuestro tema y navegación |
| **model/Usuario.kt**, **model/Rol.kt** | Modelos de datos con data class | Modelos de Asegurado, Siniestro, Liquidador, Etapa, Documento, Gestión, Aviso |
| **model/FormularioUiState.kt** | Estado de formulario, campos y errores | ReporteSiniestroUiState, AdjuntarUiState, GestionUiState |
| **viewmodel/FormularioViewModel.kt** | MutableStateFlow, actualizaciones y validación | ViewModel de reporte y validadores reutilizables |
| **viewmodel/UsuariosViewModel.kt** | Estado de lista, filtros, CRUD y persistencia | SiniestrosViewModel y casos de uso; no conservar CRUD de usuarios irrelevante |
| **viewmodel/PerfilViewModel.kt** | Estado de presentación | Estado de detalle y próximo paso |
| **navigation/AppNavegacion.kt**, **navigation/Rutas.kt** | NavHost, rutas y argumentos | MisSiniestros, Detalle(id), Historial(id), Documentos(id), Reportar, Liquidador |
| **data/UsuarioDbHelper.kt** | SQLiteOpenHelper, tablas, CRUD y migración | Tablas locales para siniestros, historial, documentos, avisos y borradores |
| **data/SesionPreferencias.kt** | Preferencias locales de usuario | Recordar perfil ficticio seleccionado; no tratarlo como login real |
| **data/ConvertidorImagen.kt** | Lectura y conversión de imágenes | Preparar evidencia, sin ocultar el tamaño real que se validará |
| **permisos/Permisos.kt** | Declarar/verificar/solicitar permisos y reaccionar a denegación | Cámara y selección de medios según versión Android y API utilizada |
| **ui/screens/FormularioScreen.kt** | Composición del formulario | ReportarSiniestroScreen |
| **ui/screens/PerfilScreen.kt** | Cámara/galería y retroalimentación | AdjuntarEvidenciaScreen |
| **ui/screens/UsuariosScreen.kt** | Lista, búsqueda y acciones | MisSiniestrosScreen o MisCasosLiquidadorScreen |
| **ui/screens/EditarScreen.kt** | Edición con UI y callbacks | Formulario de gestión/solicitud (no edición indiscriminada del siniestro) |
| **ui/components/** | Botones, campos, tarjetas, selección y avatar | Componentes compartidos para campos, tarjetas de caso, chips de etapa, alertas |
| **ui/styles/** y **ui/theme/** | Estilos, colores, dimensiones, tipografía | Sistema de diseño Material 3 sobrio, sin logos reales |

**Observaciones del ejemplo:** usa **StateFlow**, **collectAsStateWithLifecycle**, **Navigation Compose**, **SQLiteOpenHelper**, **SharedPreferences**, **cámara**, **galería** y **migración** de BD para un avatar BLOB. Sus tests incluidos son mayormente de ejemplo: **no copian ni sustituyen los tests de nuestro proyecto**. La gestión de roles de ejemplo **no implementa un sistema de autorización real**. No trasladar la contraseña del formulario didáctico a Seguros BPO, pues RNF-08 lo impide.

**Material complementario:** MiRegistroCompleto permite estudiar formularios/componentes; GraficosApp permite estudiar gráficos Canvas, animaciones y repositorios. **Los gráficos no son requisito obligatorio del caso**; no distraerse creando dashboards antes de cumplir los RF.

### Estilo que debemos respetar

- Código Kotlin legible, nombres descriptivos y comentarios solo cuando aclaren una decisión.
- Cada pantalla composable recibe estado y callbacks; lógica en ViewModel/casos de uso.
- Modelos y validadores separados de pantallas.
- Componentes de UI reutilizables; estilos centralizados.
- Presentación reactiva mediante StateFlow; no consultar SQLite o la red directamente desde composables.
- Mantener un grado de complejidad académico explicable por los dos integrantes.

### POO de forma útil para la pauta

- **Clases:** data class Siniestro, Gestion, Documento, Asegurado, Liquidador.
- **Herencia y polimorfismo:** por ejemplo una **interfaz RepositorioSiniestros** implementada por repositorio real y falso de pruebas; una jerarquía **sealed** de eventos (CambioEtapa, DocumentoSolicitado, DocumentoRecibido, GestionRegistrada) con tratamiento polimórfico real.
- Evitar clases vacías o herencia artificial solo para mostrar la palabra «herencia». Mostrar el comportamiento mediante pruebas y una explicación sencilla.

---

## 9. Arquitectura técnica exigida y propuesta de implementación

**Obligatorio por el caso oficial:**
- **Frontend Android:** Kotlin + Android Studio + Jetpack Compose + Material Design 3 + **MVVM**.
- **Persistencia local:** **Room o SQLite** (el profesor usa SQLiteOpenHelper, por lo que es una opción fiel al ejemplo).
- **Backend:** **microservicios Spring Boot** + **API REST** + **pruebas unitarias**.
- **Integración:** **Retrofit** entre aplicación y backend.
- **Solo datos ficticios/sintéticos/anonimizados**, sin accesos reales a aseguradoras.

**Propuesta del equipo, NO estructura impuesta por el caso:**

- Android **ui/screens**, **ui/components**, **ui/theme** → pantallas y recursos visuales.
- Android **model** → entidades y estados de UI.
- Android **viewmodel** → acciones, validaciones y StateFlow.
- Android **data/local** → SQLite/Room, caché, borradores y colas pendientes.
- Android **data/remote** → interfaces Retrofit y DTO.
- Android **data/repository** → fuente de verdad, sincronización, manejo de errores.
- Android **navigation** → rutas y parámetros.
- Android **permisos** → cámara, galería/selector y notificaciones cuando corresponda.
- Backend **siniestros-service** → casos, etapas, documentos, gestiones, historial, usuarios ficticios y reglas de negocio.
- Backend **notificaciones-service** → creación y consulta de avisos; integrar mecanismo de notificación visible fuera de la app para RF-09.
- Para demostrar microservicios, definir **separación real de responsabilidades**, ejecución y llamadas entre servicios si se requieren; no basta renombrar carpetas.
- Base persistente del backend y datos de prueba reproducibles, sin información real.

**Endpoints ilustrativos propuestos** (no son direcciones oficiales de la empresa; se ajustan al backend acordado):

| Método / ruta propuesta | Propósito | RF |
|---|---|---|
| GET /api/asegurados/{id}/siniestros | Casos propios | 01, 04 |
| GET /api/siniestros/{id} | Detalle y etapa | 05 |
| GET /api/siniestros/{id}/gestiones | Historial visible según rol | 06, 12 |
| GET /api/siniestros/{id}/documentos | Solicitudes y entregas | 08 |
| POST /api/siniestros/{id}/documentos | Adjunto + tipo + comprobación de recepción | 07 |
| GET /api/asegurados/{id}/avisos | Avisos del asegurado | 09 |
| GET /api/liquidadores/{id}/siniestros | Casos asignados | 10 |
| POST /api/siniestros/{id}/gestiones | Registrar gestión | 10, 12 |
| POST /api/siniestros/{id}/solicitudes | Solicitar documento | 11, 12 |
| POST /api/siniestros/{id}/transiciones | Avanzar etapa o cerrar | 11, 12 |
| POST /api/siniestros | Reportar y devolver número único | 02, 03 |

**Restricción importante:** los ID mostrados en rutas de ejemplo **no constituyen controles de acceso**. Con usuarios simulados, comprobar correspondencia de propietario/asignación en los controladores y servicios, y documentar que no es seguridad productiva.

### Datos a guardar (modelo mínimo viable)

- Asegurado ficticio: ID, nombre, RUT ficticio, datos mínimos pertinentes.
- Compañía: código, nombre ficticio, líneas disponibles, estado activo, plazo.
- Tipo de siniestro: código, nombre, línea (Vehículos/Hogar).
- Siniestro: número, propietario, compañía, tipo, fechas, comuna, descripción, etapa, asignado, actualización, próxima acción, alerta fuera de plazo, cierre.
- Colaborador/liquidador: ID, nombre, equipo, perfil.
- Gestión: siniestro, evento, fecha/hora, responsable, descripción, visibilidad.
- Solicitud de documento: tipo, fecha, comentario, Pendiente/Entregado, recepción.
- Adjunto: siniestro, tipo, ruta/identificador seguro, tamaño, formato, comentario, estado de subida.
- Aviso: siniestro, evento, fecha, mensaje, leído.
- Caché/borrador/pendiente de sincronización local.

**MER oficial:** usarlo como referencia de relaciones y normalización; implementar únicamente lo necesario para las funciones reales. No copiar 23 tablas sin justificación.

### Actualización y desconexión

1. Con Internet: Retrofit trae la información; repositorio valida/sincroniza; se almacena la copia local.
2. Sin Internet: mostrar la copia SQLite/Room con **fecha/hora de última sincronización** claramente visible.
3. Al reportar o adjuntar: conservar borrador mientras se completa y si se corta la conexión.
4. Marcar explícitamente archivo **«Pendiente de envío»** hasta respuesta positiva del backend.
5. Al volver conexión: reintentar sin duplicar operaciones (identificador de solicitud/idempotencia propuesto); si el siniestro ya está cerrado, informar rechazo y no marcar como entregado.
6. Cuando el liquidador modifica etapa/historial: el backend actualiza de modo coherente y los dos perfiles ven datos sincronizados.
7. Avisos: bandeja persistente + **notificación que llegue sin abrir la app**. Para la demo puede evaluarse FCM u otro mecanismo push real, con prueba documentada de recepción y permisos de Android; una simple Snackbar dentro de la app no satisface por sí sola el propósito de RF-09.

---

## 10. Animaciones, usabilidad y recursos del teléfono

### Recursos nativos elegidos para obtener el máximo en pauta

- **Cámara:** capturar foto de evidencia para un siniestro abierto; previa, aceptación, asociación al caso, permisos cuando aplique, cancelación/denegación gestionadas.
- **Galería / selector del sistema:** seleccionar foto existente, y **selector de documentos** para PDF; verificar tipo MIME, tamaño y máximo de adjuntos. Usar contratos ActivityResult adecuados al nivel API y no pedir permisos innecesarios.
- **Notificaciones del sistema (adicional):** cambios relevantes con vínculo al siniestro, evitando revelar datos sensibles.

El profesor usa cámara y galería para el **avatar**. En nuestra app esos recursos **sirven a un proceso del negocio**, no a una foto de perfil decorativa.

### Animaciones con propósito

- [ ] Cambio de pantalla o expansión de detalle con transición suave.
- [ ] Cambio de etapa con realce breve + texto, nunca solo color.
- [ ] Indicadores de carga y éxito/fracaso en envío o reporte.
- [ ] Aparición animada de estado vacío o documentos pendientes cuando ayude.
- [ ] Evitar parpadeo y animaciones excesivas; respetar accesibilidad y preferencias del sistema cuando sea viable.

---

## 11. Plan de pruebas y calidad

**No basta con que compile.** Debe poder demostrarse el recorrido entero, conectando UI, backend, persistencia local y recursos del teléfono.

### Pruebas unitarias mínimas por dominio

- [ ] RUT ficticio: dígito correcto e incorrecto.
- [ ] Fechas: futura, posterior al reporte, mismo día, fuera de plazo mayor a 30 días.
- [ ] Número de siniestro: formato, unicidad, concurrencia/doble envío.
- [ ] Transiciones: válidas, inválidas, excepciones y cierre con resultado/mensaje.
- [ ] Documento pendiente: bloquea liquidación; documento recibido desbloquea.
- [ ] Adjuntos: imagen/PDF permitido; archivo extraño, mayor a 5 MB, decimosexto y caso cerrado rechazados.
- [ ] Propiedad: asegurado A no consulta casos ni documentos de B; liquidador no opera caso no asignado.
- [ ] Historia: gestión interna jamás visible al asegurado; evento conserva responsable y fecha/hora.
- [ ] Avisos: solo eventos permitidos; identificar siniestro y no exponer datos privados.
- [ ] Recordatorios: día 15, opción de desistimiento en día 30 sin cierre automático; control de gestión visible cada 7 días.
- [ ] Compañía inactiva: lectura sí, reporte no.
- [ ] Validadores de ViewModels y respuesta visual a errores.
- [ ] Caché offline, borrador, actualización, envío pendiente y rechazo tras cierre.

### Pruebas de integración / manuales

- [ ] **HU-01 a HU-11:** evidenciar cada criterio de aceptación obligatorio del anexo; HU-12 si se realiza RF-17.
- [ ] Instalar APK y abrir desde cero en emulador/dispositivo.
- [ ] Arrancar microservicios y comprobar health, endpoints y base de datos.
- [ ] Consulta desde Android mediante Retrofit y cambio de etapa visto en ambos perfiles.
- [ ] Foto real, archivo de galería y PDF; cancelación y permisos rechazados.
- [ ] Modo avión: consulta con fecha, borrador conservado, reintento.
- [ ] Notificación visible con app en segundo plano o cerrada, si se implementa el mecanismo push.
- [ ] Navegación sin pantallas inaccesibles ni bloqueos.
- [ ] Contraste, texto aumentado, errores por campo y tiempo RNF-01/RNF-02.
- [ ] Comandos de compilación y pruebas documentados en el README o script de desarrollo.
- [ ] Capturas de resultados y salidas de test; no afirmar cobertura no medida.

**Puerta de calidad por PR:** compila, pruebas relevantes pasan, cumple RF/RN afectados, no rompe flujos anteriores, revisión por compañero y evidencia adjunta. Cobertura JaCoCo puede incorporarse como herramienta de control adicional; **no se promete 100 % de cobertura sin medición real**.

---

## 12. GitHub + Trello: colaboración real por dos integrantes

**20 puntos de la pauta dependen de participación demostrable.** No fabricar commits ni usar la cuenta del compañero; cada autor trabaja y confirma sus propios cambios.

### Convenciones sugeridas

- Rama principal: **main** estable.
- Ramas por trabajo: **feature/RF-04-mis-siniestros**, **feature/RF-07-adjuntos**, **feature/backend-siniestros**, **test/RN-09-transiciones**, etc.
- Commits claros y atómicos, por ejemplo «feat: muestra estado e historial del siniestro» o «test: valida cambio de etapa con pendientes».
- Issues por requisito con ID RF/RN/RNF, responsable, criterios de aceptación, pruebas y evidencia.
- PR con referencia a issue, descripción, capturas cuando cambie la UI y resultado de tests.
- Tablero Trello: **Pendiente → En desarrollo → En revisión → En pruebas → Terminado**. Cada tarjeta enlaza issue/PR/commit.
- Evitar trabajos paralelos sobre el mismo archivo sin acordar integración.

### Distribución de ejemplo (ajustable por los dos integrantes)

| Área | Integrante A | Integrante B | Integración conjunta |
|---|---|---|---|
| Frontend | Navegación, Mis siniestros, Detalle, Historial | Reporte, Adjuntos, Avisos, pantallas liquidador | UI coherente, componentes compartidos y pruebas en dispositivo |
| Backend | Modelo de siniestro, consultas, gestión/etapas | Documentación, avisos, persistencia y pruebas | Contratos Retrofit, endpoints, semillas y tests |
| QA | Casos RF-01/04/05/06/10/11 | Casos RF-02/03/07/08/09/12 | RN, RNF, cobertura real, demo y revisión cruzada |
| GitHub/Trello | Issues y PR propios | Issues y PR propios | Historial auténtico y tareas cerradas por evidencia |

La tabla **no asigna unilateralmente** trabajo ya acordado. El reparto definitivo debe reflejar la participación efectiva de ambos y minimizar bloqueos.

### Backlog recomendado en orden

- [ ] **T00** Configuración Android, backend, ramas, Trello, definición de hechos y dataset sintético.
- [ ] **T01** Modelos, caché SQLite/Room, Retrofit y servicios mínimos.
- [ ] **T02** RF-01 identificación simulada + restricciones por perfil.
- [ ] **T03** RF-04/05/06 consulta, detalle, historial + RNF-02/03/04.
- [ ] **T04** RF-07/08 evidencia, solicitudes, pendientes, recepción + RN-10/11.
- [ ] **T05** RF-09 avisos relevantes + aclaraciones 5/9.
- [ ] **T06** RF-10/11/12 bandeja liquidador, gestiones, etapas y auditoría.
- [ ] **T07** RF-02/03 reporte, número único y validaciones.
- [ ] **T08** RNF-05/06 offline, fecha de caché, borradores y pendientes.
- [ ] **T09** Animaciones útiles, accesibilidad y pruebas de dos recursos nativos.
- [ ] **T10** Pruebas unitarias backend y validadores; integración, regresión y demo.
- [ ] **T11** Capturas, documentación de ejecución, evidencia GitHub/Trello, APK y presentación.
- [ ] **T12 opcional** RF-13 a RF-18 solo si todo lo obligatorio ya funciona.

---

## 13. Fuera de alcance: NO malgastar tiempo

Según los anexos y el caso académico, no desarrollar en esta entrega:

- Login productivo, credenciales reales ni integración con identidad de aseguradoras.
- Sistemas core de pólizas, datos reales, pagos, facturación o liquidación económica productiva.
- Motor productivo de normalización/distribución automática de casos.
- Aprobación de resolución por la compañía dentro de la app.
- Todos los flujos alternativos de una solución comercial o publicación en tienda.
- Dashboards ejecutivos, gamificación, logos de empresas reales o funciones decorativas no solicitadas.
- Las 23 tablas del MER si el modelo mínimo viable cumple el caso y puede explicarse.

**Precaución:** cumplir RNF-07 y RNF-08 con usuarios sintéticos, filtros, validaciones, mínimos datos y seguridad básica del MVP. No presentar el selector de usuario simulado como control de acceso listo para producción.

---

## 14. Criterios de «terminado» para cada historia y para el MVP

### Una tarjeta o requisito está terminado solo cuando

- [ ] Funcionalidad visible y navegable.
- [ ] Lógica en ViewModel/dominio, datos persistidos correctamente.
- [ ] Validaciones y reglas verificadas en backend cuando corresponda.
- [ ] Errores y estados de carga claros.
- [ ] Tests relevantes ejecutados y aprobados.
- [ ] No expone datos de otro perfil.
- [ ] Issue/Trello con evidencias y PR revisado.
- [ ] Aceptado por ambos integrantes sin romper funcionalidades anteriores.

### El MVP está listo para presentar cuando

- [ ] Se pueden seleccionar un asegurado y un liquidador ficticios.
- [ ] El asegurado consulta varios siniestros propios, incluidos cerrados, con etapa y último cambio.
- [ ] Detalle muestra compañía ficticia, próximo paso e historial sin gestiones internas.
- [ ] Documentos solicitados y recibidos se distinguen correctamente.
- [ ] Se captura una foto, se selecciona una imagen y se adjunta un PDF con validaciones.
- [ ] El liquidador registra gestión, pide documentos, avanza de etapa y cierra con resultado/mensaje.
- [ ] Los cambios generan historial y avisos adecuados y sincronizados.
- [ ] Se reporta caso nuevo, se valida y se entrega número con formato correcto.
- [ ] En modo avión se consulta caché con fecha y no se pierde un borrador.
- [ ] Android usa MVVM/Compose/Material 3; Room/SQLite; Retrofit; backend Spring Boot en microservicios.
- [ ] Todas las pruebas unitarias de backend comprometidas se ejecutan y pasan.
- [ ] Hay evidencia de animaciones y de **dos recursos nativos** funcionales.
- [ ] Cada uno de los siete criterios de la pauta tiene evidencia.
- [ ] GitHub y Trello muestran contribuciones auténticas de ambos participantes.
- [ ] Se entrega procedimiento reproducible para levantar backend, ejecutar Android, cargar datos ficticios y probar endpoints.
- [ ] APK/debug o instalación demostrable y recorrido grabado o presencial validado en dispositivo.
- [ ] No quedan funciones marcadas como hechas sin prueba; las deseables pendientes se declaran claramente.

---

## 15. Instrucciones de continuidad para el equipo

1. **Leer esta guía antes de implementar** y comprobar la rama/commits actuales.
2. **No reiniciar ni sustituir trabajo del compañero.** Primero inspeccionar, luego proponer cambio mínimo.
3. Mantener las convenciones del profesor cuando sean compatibles con la especificación; no introducir complejidad innecesaria.
4. Para cada decisión, anotar si se debe a **pauta / RF / RN / RNF / aclaración / propuesta propia**.
5. Priorizar funcionamiento completo de los doce RF y no perseguir funciones extra antes de cerrar lo obligatorio.
6. Vincular implementación → requisito → prueba → evidencia GitHub/Trello.
7. Documentar claramente el estado **No iniciado / En progreso / Implementado sin probar / Probado** y no asignar porcentajes sin auditoría.
8. Verificar nuevamente la guía si se recibe una nueva aclaración oficial del docente o de la empresa.

**Resultado esperado:** un MVP serio, accesible, demostrable y explicable como proyecto de estudiantes, técnicamente correcto, trazable frente al caso y optimizado para alcanzar el nivel «Muy buen desempeño» en los 7 criterios de la evaluación.
