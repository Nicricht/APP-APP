# Plan maestro para terminar Seguros BPO

**Repositorio:** Nicricht/APP-APP  
**Rama autorizada para desarrollo y documentacion:** qa  
**Fecha de corte:** 23-10-2026  
**Linea base auditada:** qa = 3b9cd9bc45241fc0b4a099c889008a42ea266b60  
**Estado:** plan aprobado para comenzar; NO implica funcionalidades implementadas ni pruebas verdes.

## 1. Objetivo y alcance

Entregar una aplicacion Android academica de gestion de siniestros de Vehiculos y Hogar, funcional de principio a fin, inspirada tecnicamente en MiRegistroAvatar del profesor. Solo existen dos roles: ASEGURADO y LIQUIDADOR. Habra crear cuenta para asegurados, inicio y cierre de sesion, perfil, fotografia, seguimiento, evidencias, avisos y estadisticas diferenciadas; el liquidador tomara casos de su propia area y los gestionara hasta cierre.

**Fase actual:** Kotlin, Jetpack Compose, Material 3, Navigation Compose, MVVM, StateFlow, SQLiteOpenHelper y recursos nativos Android. Backend Spring Boot, Retrofit, API REST, sincronizacion multi-dispositivo, push remoto y autenticacion centralizada quedan para la evaluacion de servicios. No presentaremos cuentas locales como autenticacion real entre telefonos.

**Jerarquia:** las especificaciones oficiales del caso y anexos tienen prioridad sobre ejemplos del profesor y mejoras propuestas. Esta hoja de ruta distingue desviaciones voluntarias del MVP oficial. Cualquier nueva regla incompatible debe validarse antes de declararla obligatoria.

## 2. Estado real de la base existente

**Existe en codigo qa:** formulario de siniestros con validaciones, numeros SIN-AA-NNNNNN, busqueda y lista por asegurado ficticio, detalle y correccion condicionada, historial, SQLite version 5, carga de fotos por camara y galeria, adjunto PDF, solicitudes de documentos, avisos locales, liquidador ficticio por area, registro de gestiones, transiciones y cierres, componentes de graficos, Jetpack Compose y numerosas pruebas unitarias e instrumentadas.

**No existe todavia:** registro de cuentas de asegurado, login basado en cuentas, sesion autenticada por rol, foto de perfil editable, multiple liquidadores reales por especialidad, bandeja de casos sin asignar, boton Tomar caso y asignacion atomica, perfil con operaciones completas, estadisticas personales por cuenta, interfaz definitiva y verificacion fisica completa de APK.

**Bloqueo QA constatado al 23-10-2026:** la ejecucion instrumentada [40002987884](https://github.com/Nicricht/APP-APP/actions/runs/40002987884) en qa fallo en :app:connectedDebugAndroidTest; los informes estan como artefacto informes-android-instrumentado. Es necesario identificar los tests concretos, repararlos y ejecutar nuevamente. La ejecucion exitosa de APK del profesor [40002984166](https://github.com/Nicricht/APP-APP/actions/runs/40002984166) NO valida nuestra app.

**Cuidado con documentos anteriores:** CONTROL_FINAL_100_PUNTOS.md, MAPA_PRUEBAS_QA.md y ETAPA_1_ANDROID.md contienen puntos de corte antiguos; antes de reportar progreso prevalece el codigo del commit y el CI del ultimo SHA. Este plan complementa, no reescribe, el caso oficial.

## 3. Rol ASEGURADO

Puede crear una cuenta de demostracion, iniciar/cerrar sesion, ver solo sus siniestros, reportar, buscar y filtrar, abrir detalle, ver estado y proximo paso, consultar historial visible, corregir la descripcion en Recibido, entregar evidencias, consultar documentos pendientes y entregados, recibir avisos, editar datos personales permitidos, cambiar foto mediante camara/galeria y revisar estadisticas propias.

No puede tomar casos, cambiar etapas, solicitar documentos como liquidador, consultar gestiones internas, ver datos de otros usuarios ni asignarse permisos elevados.

**Pantallas:** Registro, Login, Inicio, Mis siniestros, Reportar, Detalle y seguimiento, Historial, Documentos y evidencias, Avisos, Estadisticas y Mi perfil. El menu es navegacion compartida y no una entidad de negocio.

## 4. Rol LIQUIDADOR

Tiene cuenta preconfigurada por el equipo para la demostracion. Pertenece a una sola especialidad: VEHICULOS u HOGAR. Puede iniciar/cerrar sesion, ver bandeja de casos disponibles limitada a su area, tomar un caso elegible, consultar sus casos asignados, revisar antecedentes, solicitar documentos, registrar gestiones visibles o internas, cambiar etapa respetando las reglas, registrar resultados y mensaje de cierre, consultar sus estadisticas y actualizar perfil.

No puede inscribirse como liquidador desde Crear cuenta, adjudicarse otra especialidad, acceder a casos privados asignados a otro liquidador, cambiar el propietario, inventar resoluciones de aseguradoras o reabrir cerrados.

**Pantallas:** Login, Inicio del liquidador, Disponibles de mi area, Mis casos, Detalle y evaluacion, Solicitar documentos, Gestiones y transiciones, Resolucion y cierre, Estadisticas y Mi perfil. Reutilizar componentes de detalle, historial, documentos y menu cuando corresponda.

## 5. Cuentas, identidad y privacidad: ampliacion acordada

1. Registro publico crea EXCLUSIVAMENTE rol ASEGURADO; campos propuestos: nombre, apellidos, RUT ficticio valido, correo, telefono, clave de DEMOSTRACION y confirmacion.
2. Correo normalizado e identificador de usuario unico. Rechazar correo o RUT duplicado, formatos invalidos y campos obligatorios vacios.
3. Cuentas de LIQUIDADOR provisionadas desde datos ficticios de desarrollo, con equipo definido por configuracion. No permitir seleccionar rol privilegiado en el formulario.
4. Separar sesion, identidad y filtros de consulta: TODOS los accesos a datos reciben el ID de la cuenta y validan propietario o asignado en la base, no solo en botones.
5. No guardar contraseñas en claro ni reutilizar claves reales. El alcance oficial original RNF-08 dice no solicitar/almacenar contraseñas; la mejora de login requiere acuerdo explicito sobre una excepcion didactica. Alternativa conservadora: cuentas ficticias y acceso local simulado; si se autorizan credenciales de prueba, guardar solo un verificador derivado con sal y algoritmo apropiado, nunca el secreto ni logs con credenciales. No prometer seguridad productiva.
6. Recordar la sesion local de demostracion; Cerrar sesion limpia identidad en memoria, rutas restringidas y cualquier vista sensible anterior. La preferencia de sesion no es autoridad para desbloquear casos ajenos.
7. Respetar minimizacion de datos: RUT parcialmente oculto en pantalla, datos sinteticos y sin uso de nombres/logos de aseguradoras reales.

## 6. Reporte y clasificacion por area

- El asegurado selecciona compania ficticia activa, tipo Vehiculo u Hogar, fecha de ocurrencia no futura, comuna y descripcion, con confirmacion. Opcionales: referencia, hora, terceros en vehiculos y constancia.
- Rechazar fecha invalida o futura; reporte realizado despues de 30 dias corridos se acepta con aviso Fuera de plazo.
- Para misma cuenta + compania + tipo + fecha y caso abierto, advertir posible duplicado, permitir continuar con confirmacion.
- Generar identificador unico SIN-AA-NNNNNN; guardar responsable/fecha/evento inicial, etapa RECIBIDO y asignado nulo.
- Clasificar automaticamente el AREA en VEHICULOS para CHOQUE, ROBO_VEHICULO y CRISTALES; HOGAR para DANO_AGUA, ROBO_HOGAR e INCENDIO. Area NO significa asignacion automatica a una persona.
- Caso nuevo se muestra en la bandeja Disponible de liquidadores de su propia area con los datos minimos necesarios para decidir tomarlo.

## 7. Asignacion propuesta sin tercer rol

La clasificacion por area es automatica; la asignacion personal ocurre cuando un LIQUIDADOR pulsa Tomar caso. Esta funcionalidad es una **ampliacion acordada**: en el caso original la asignacion/reasignacion por analista era deseable RF-17. No atribuir este mecanismo al requisito oficial.

Condiciones para Tomar caso:
- Sesion de LIQUIDADOR valida; especialidad coincide con el tipo; siniestro en RECIBIDO, abierto, sin responsable.
- Actualizar de modo atomico: WHERE id = ? AND etapa = RECIBIDO AND liquidador_id IS NULL AND area = areaDelUsuario. Debe afectar exactamente una fila.
- Dentro de la MISMA transaccion escribir responsable, etapa EVALUACION, marca temporal, evento de historial visible y aviso al asegurado. Si falla alguna escritura, revertir todas.
- En concurrencia solo gana una transaccion; el resto obtiene mensaje El caso ya fue tomado. Refrescar lista.
- No permitir tomas desde EVALUACION, LIQUIDACION o CERRADO, ni asignaciones cruzadas VEHICULOS/HOGAR.
- No hay reasignacion libre en el MVP. Si es imprescindible por ausencia del titular, solo mediante procedimiento excepcional trazable definido y probado, nunca editando directamente el campo desde UI.
- Si no se toma, RECIBIDO conserva proximo paso Estamos revisando tu reporte para asignarlo a un liquidador.

## 8. Etapas y resolucion: reglas oficiales

Secuencia normal: RECIBIDO -> EVALUACION -> LIQUIDACION -> CERRADO. Sin saltos ni retrocesos.

| Etapa origen | Destino permitido | Resultados posibles |
|---|---|---|
| Recibido | Evaluacion o cierre anticipado | Si cierra, solo Desistido |
| Evaluacion | Liquidacion o cierre anticipado | Si cierra, Rechazado o Desistido |
| Liquidacion | Cerrado | Aprobado o Rechazado |
| Cerrado | Ninguno | No reabrir |

Para cierre es obligatorio resultado permitido y mensaje de cierre no vacio. La aseguradora decide fuera de la aplicacion; el liquidador registra la decision comunicada. Nunca aprobar pagos automaticamente. RN-09: NO avanzar de Evaluacion a Liquidacion mientras haya solicitudes documentales pendientes.

Al mostrar un caso indicar siempre etapa, ultima actualizacion y proximo paso claro. Al asegurado mostrar del responsable solamente nombre de pila + inicial y equipo.

## 9. Solicitudes, adjuntos y plazos

- El liquidador asignado puede solicitar un documento con tipo, instrucciones opcionales, fecha y estado Pendiente. No crear duplicado pendiente del mismo tipo/caso. Generar evento y aviso.
- El asegurado propietario entrega foto de camara, imagen de galeria o PDF con tipo obligatorio. MIME admitidos image/jpeg, image/png, image/webp y application/pdf, maximo 5 MB por archivo y 15 adjuntos por caso.
- Guardar archivo y vincularlo a solicitud concreta; exito solo tras confirmar transaccion. Nunca permitir adjuntar a CERRADO ni borrar adjunto enviado por el asegurado; nueva version como otro adjunto documentado.
- Distinguir Pendiente, Pendiente de envio (futura sincronizacion), Entregado localmente, Recibido por servidor (futuro); no simular confirmacion remota inexistente.
- Recordatorio a los 15 dias corridos de solicitud sin entrega; a los 30 dias el liquidador puede registrar Desistido si aplican las reglas, nunca cierre automatico. Evitar recordatorios duplicados y registrar eventos.
- Antes de pasar a LIQUIDACION contar solicitudes pendientes mediante consulta persistida, no solo estado local de pantalla.

## 10. Gestiones, auditoria y notificaciones

Cada gestion guarda ID de siniestro, autor, fecha/hora, descripcion y visibilidad. Interna solo para liquidador autorizado; visible aparece en historial del asegurado. Al menos una gestion visible por siete dias corridos mientras siga abierto; si no hay novedades registrar Sin novedades segun flujo definido. Historial cronologico completo, sin eliminar evidencias anteriores.

Generar avisos por asignacion, cambios de etapa, documento solicitado, recordatorio y cierre. No avisar por gestion interna ni exposicion de informacion confidencial. Avisos con caso, fecha, leido/no leido, navegacion al detalle y controles de propietario. En la etapa local son avisos dentro de app, no push entre dispositivos.

## 11. Estadisticas obligatorias del producto acordado

**Asegurado:** cantidad total, abiertos, cerrados, por etapa, por tipo, documentos pendientes y evolucion temporal, filtrado SIEMPRE por su ID.

**Liquidador:** casos actualmente asignados, en evaluacion, en liquidacion, cerrados, resultados Aprobado/Rechazado/Desistido, documentos pendientes, tendencia temporal y carga de casos; consultar solo sus casos actuales. Para historico de resultados y reasignaciones futuras definir metricas y alcance antes de implementarlas.

Generar KPI y graficos con consultas SQLite reales o agregaciones derivadas de resultados reales. No cifras fijas de demostracion dentro del producto, no mezclar casos de otros usuarios ni confundir casos cerrados acumulados con pendientes actuales. Estados vacios y mensajes accesibles; graficos comprensibles tambien sin colores.

## 12. Arquitectura y migraciones

Mantener capas UI (Compose)/Navigation -> ViewModel(StateFlow) -> Repository o acceso encapsulado -> SQLiteOpenHelper; reglas fuera de la UI; componentes reutilizables del profesor adaptados, nunca copiar su dominio de estudiantes, seguidores o intereses sin razon.

SQLite actual en qa: **version 5** (verificar nuevamente antes de codificar). Planear version siguiente con migracion incremental, sin DROP de tablas ni perdida de adjuntos/casos. Entidades/relaciones minimas:

| Entidad | Campos clave |
|---|---|
| usuarios | id, nombre, rut_ficticio, correo_unico, telefono, rol, foto, activo |
| credenciales_demo | usuario_id, verificador, sal, version_algoritmo, solo si excepcion academica aprobada |
| liquidadores | usuario_id, especialidad, activo |
| siniestros | id, numero_unico, asegurado_id, area, compania, tipo, fechas, etapa, liquidador_id nullable, cierre |
| asignaciones | id, siniestro_id, liquidador_id, fecha, accion, motivo |
| gestiones | id, siniestro_id, usuario_autor_id, visible_asegurado, fecha, descripcion |
| solicitudes_documentos | id, siniestro_id, tipo, comentario, fecha_solicitud, fecha_entrega, evidencia_id |
| evidencias | id, siniestro_id, tipo, mime, contenido/referencia, tamano, fecha |
| avisos | id, siniestro_id, descripcion, fecha, leido |
| borradores | usuario_id, formulario, fecha_actualizacion |

**Migracion de fixtures:** convertir asegurados enum existentes a cuentas de demostracion estables y mapear todos los siniestros al nuevo asegurado_id preservando numeros, archivos, avisos y movimientos. Convertir Liquidador.VEHICULOS/HOGAR a cuentas demo de especialidad, manteniendo relaciones y cierres. Probar apertura de BD desde versiones anteriores y compatibilidad de consultas. Nunca migrar fixtures como credenciales reales.

**POO evaluable:** clases de dominio, interfaz de repositorio y al menos un caso real y pequeno de polimorfismo justificado, como estrategia de validacion segun tipo VEHICULO/HOGAR, sin jerarquias decorativas.

## 13. Experiencia visual y recursos nativos

Aplicar Material 3, tipografia legible, contraste, estados vacio/carga/error, retroceso correcto, teclado y scroll adecuados, tarjetas consistentes, animaciones breves de exito y estados, menu por rol, fotos de perfil, icono/logo propio, textos simples. Reutilizar patrones del proyecto MiRegistroAvatar: pantallas, ViewModels, SQLite, validaciones por campo, AlertDialog y seleccion de imagenes, adaptados al caso.

Recursos nativos: camara (permiso rechazado, cancelacion, foto real, archivo persistente) y selector de galeria/documentos (sin pedir permisos amplios), con pruebas en Android API 30 y 35 al menos. Verificar APK instalable, identificador de paquete, firma debug, testOnly, arranque, persistencia tras reiniciar y que se vea icono propio.

## 14. Backlog ordenado para desarrollo

| ID | Prioridad | Entrega | Criterio de terminado |
|---|---|---|---|
| BPO-00 | BLOQUEANTE | Recuperar QA instrumentado | Diagnosticar tests fallidos de run 40002987884, arreglar causa y obtener run verde en SHA actual |
| BPO-01 | P0 | Modelos y migracion de usuarios | v5 -> nueva version; sin perdida de datos; pruebas de migracion y restricciones |
| BPO-02 | P0 | Registro e inicio de sesion demo | Crear asegurado, validaciones, login, logout, rol fijo, acceso restringido |
| BPO-03 | P0 | Seguridad de consultas por ID | Asegurado solo propios, liquidador solo asignados y bandeja limitada al area |
| BPO-04 | P0 | Bandeja disponible y Tomar caso | Asignacion atomica, mismo equipo, una fila, evento y aviso, prueba de competencia |
| BPO-05 | P0 | Flujo documental completo | Solicitar, entregar, consultar pendiente, bloquear liquidacion, limites y cierres |
| BPO-06 | P1 | Historial, avisos y plazos | Visibilidad interna, recordatorios de 15/30 dias y gestion periodica de 7 dias |
| BPO-07 | P1 | Paneles y navegacion por rol | Inicio, menu, detalle con proximo paso, filtros, perfil foto |
| BPO-08 | P1 | Estadisticas de los dos roles | Agregaciones reales, graficos, filtros y privacidad |
| BPO-09 | P1 | POO y animaciones | Polimorfismo util, transiciones funcionales y pruebas |
| BPO-10 | P0 | Regresion, accesibilidad y APK | CI verde, API 30/35, recursos reales, instalacion y recorridos E2E |
| BPO-11 | P0 | Evidencia de colaboracion | Trello + issues/PR + commits autenticos de ambos integrantes |
| BPO-12 | P0 | Entrega controlada | Revision de aceptacion, snapshots, recien entonces propuesta de promocion a main |

**Orden:** BPO-00 -> BPO-01 -> BPO-02/03 -> BPO-04 -> BPO-05/06 -> BPO-07/08/09 -> BPO-10/11 -> BPO-12. No avanzar si falla integridad de datos, privacidad o CI de la etapa anterior.

## 15. Definicion de terminado

Una entrega NO esta terminada si solo existe en Compose. Requiere:
1. Vista/navegacion y estados validos e invalidos.
2. Regla validada tambien fuera de UI.
3. Persistencia correcta y migracion segura.
4. Aislamiento de datos por rol.
5. Tests unitarios, integracion y UI agregados en qa, sin regresion.
6. Compilacion y Android instrumentado verdes en el SHA de la funcionalidad.
7. Evidencia manual en dispositivo/emulador cuando haya recurso nativo, UX o instalacion.
8. Documentacion de cambio, tarjeta/issue y autor real verificables.
9. Ningun comentario nuevo dentro del codigo fuente, respetando la convencion acordada del equipo.
10. Ningun cambio directo en main ni entrega-01.

## 16. Documentos vinculados

- [Protocolo QA obligatorio](PROTOCOLO_QA_OBLIGATORIO.md)
- [Matriz de pruebas y criterios de aceptacion](MATRIZ_ACEPTACION_Y_PRUEBAS.md)
- [Guia maestra del caso y pauta](GUIA_MAESTRA_DSY1105.md)
- [Control evaluacion 100 puntos](CONTROL_FINAL_100_PUNTOS.md)
- [Mapa historico de pruebas](MAPA_PRUEBAS_QA.md)

**Regla final:** primero corregir QA, despues desarrollar sobre qa y promover solo cuando exista evidencia fresca y completa.