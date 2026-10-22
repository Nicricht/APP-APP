# Auditoría del proyecto Android frente al material docente

Fecha: 22 de octubre de 2026. Repositorio: APP-APP. Rama de desarrollo: main. QA: qa.

## Fuentes y alcance

- MiRegistroAvatar.zip: 35 archivos Kotlin de aplicación Android, incluyendo formularios, perfil, lista/edición de usuarios, SQLite, navegación, ViewModels, cámara, galería, permisos, sesión simulada, tema y componentes.
- GraficosApp (1).zip: 9 archivos Kotlin de aplicación y gráficos Canvas con Animatable, un repositorio, un ViewModel y StateFlow.
- components (1).zip: 10 componentes Kotlin de Compose.
- iconos (1).zip: 8 recursos vectoriales Android.
- imagenes_compartir_proyecto (1).zip: 3 imágenes PNG ilustrativas.
- docs/GUIA_MAESTRA_DSY1105.md: matriz de requisitos del caso y pauta de siete criterios reconstruida a partir del material del equipo.

Estos ejemplos del profesor enseñan patrones de Android, no obligan a replicar pantallas de registro y perfil en un dominio de seguros. El caso de esta evaluación usa identificación simulada y prohíbe solicitar contraseñas. No confundir ese supuesto académico con autenticación segura de producción.

## Pauta de siete criterios

| Criterio | Máximo | Evidencia actual | Para obtener evidencia suficiente |
|---|---:|---|---|
| Interfaz y navegación | 15 | Compose, Material 3, Navigation Compose y distintas vistas funcionales | Probar todos los recorridos y retrocesos en dispositivo, texto aumentado y contraste |
| Formularios completos | 15 | Reporte, evidencia, gestión, solicitud de documentos y cierre | Probar errores, confirmaciones, estados de documentos y restricciones |
| Validadores y reactividad | 10 | ViewModels, StateFlow, validadores, SQLite y controles de etapa | Tests de negocio, migraciones, cambios de estado y persistencia |
| Animaciones útiles | 10 | Gráficos Canvas animados, aparición de resultados y estado vacío | Probar fluidez, accesibilidad y cambios de datos en dispositivo |
| Clases, herencia y polimorfismo | 15 | Modelos, clases, interfaces del framework y arquitectura modular | Demostrar herencia y polimorfismo propios del dominio, no solo clases Kotlin |
| GitHub y Trello colaborativos | 20 | Repositorio, ramas, commits y GitHub Actions | Mostrar contribuciones reales y trazables de ambos integrantes, issues, revisiones y tarjetas |
| Recursos del dispositivo | 15 | Cámara, galería y selector de PDF conectados al caso | Probar denegación, cancelación, permisos, tamaños, límites y conservación de archivos |

Ninguna puntuación está garantizada. Es la pauta, no una calificación obtenida.

## Trazabilidad funcional RF-01 a RF-12

| RF | Estado observado | Evidencia / limitación |
|---|---|---|
| RF-01 Identificación | Implementación académica local | Selector ficticio, filtro por asegurado y comprobación de propietario en consultas |
| RF-02 Reporte | Implementado local | Formulario de vehículo/hogar y validación por campo |
| RF-03 Número de siniestro | Implementado local | Identificador persistido en SQLite y mostrado al registrar |
| RF-04 Mis siniestros | Implementado, comprobar en teléfono | Lista por asegurado, búsqueda, estados, actualización y abiertos antes que cerrados |
| RF-05 Detalle / próximo paso | Implementado con mejora reciente | Etapa, liquidador, fechas y documentos pendientes orientan al siguiente paso |
| RF-06 Historial | Implementado local | Gestiones visibles ordenadas, filtradas por propietario |
| RF-07 Adjuntar evidencia | Implementado local | Cámara, imágenes y PDF con tipo obligatorio, límite de cantidad y tamaño |
| RF-08 Documentos solicitados | Implementado local; requiere prueba integral | Solicitud, pendiente, entrega por tipo y estado entregado localmente; sin confirmación externa |
| RF-09 Avisos | Parcial | Bandeja local de avisos y contador; sin notificación del sistema ni backend |
| RF-10 Gestión liquidador | Implementado local | Casos por equipo, gestión visible/interna, historial y gráficos |
| RF-11 Solicitar y avanzar | Parcial | Solicitud local y avance con control real de pendientes; faltan cierres anticipados permitidos |
| RF-12 Auditoría de eventos | Parcial | Registro local de reportes, evidencias, solicitudes y cambios; pendientes otros eventos y reglas periódicas |

## Reglas de negocio y riesgos que siguen abiertos

1. RN-02: faltan alternativas de cierre anticipado y estado Desistido conforme a las transiciones autorizadas.
2. RN-15: falta recordatorio de documentos pendientes a los 15 días y opción controlada de desistimiento a los 30 días.
3. RN-16: falta verificar el seguimiento de gestión visible al menos cada siete días.
4. RNF-04: hace falta comprobar contraste en modo claro/oscuro y accesibilidad con letra grande y lector de pantalla.
5. RNF-06: verificar recuperación de borradores tras reiniciar o interrumpir una operación.
6. RNF-07: aunque hay comprobaciones de propietario y separación de perfiles, sigue siendo una simulación sin login de producción.
7. Los nuevos casos quedan sin liquidador; la asignación administrativa integral es deseable, no obligatoria en el MVP según la guía.
8. La APK académica almacena datos en SQLite del teléfono. No equivale a un envío externo ni a aceptación de una aseguradora.
9. La migración SQLite de versión 4 a 5 debe probarse manteniendo evidencias y siniestros existentes.
10. La cobertura de QA de interfaz y recursos debe confirmarse con ejecuciones exitosas, no solo por existencia de tests.

## Hallazgos de experiencia de usuario y correcciones

- Capturas reales anteriores mostraron opciones y datos invisibles por contraste. Se corrigieron temas y opciones; comprobar la APK instalada, no una versión anterior.
- El selector de compañía y tipo reemplaza listas verticales extensas de círculos.
- El formulario advierte fecha de más de 30 días sin rechazar automáticamente el reporte.
- Se mejoró contraste de confirmación de éxito y se sustituyó el ícono predeterminado.
- Los gráficos del liquidador aparecen directamente y representan casos asociados a su equipo.
- Al seleccionar un caso en Liquidador, la gestión reemplaza temporalmente la lista y tiene una acción explícita para regresar.
- El listado del asegurado muestra primero los abiertos.
- El detalle señala documentos pendientes y permite pasar a la pantalla de evidencias para prepararlos.
- La pantalla inicial indica que los registros se almacenan en el teléfono; la de avisos distingue claramente avisos locales de notificaciones reales.

## Pruebas de aceptación obligatorias antes de presentar

1. Instalación y actualización de APK en Motorola con conservación de SQLite.
2. Registro válido y error visible por fecha, descripción, comuna, compañía inactiva y confirmación.
3. Reporte antiguo marcado fuera de plazo, pero guardado.
4. Búsqueda por asegurado, aislamiento cruzado y orden abierto/cerrado.
5. Captura de cámara real, selección de galería, PDF, cancelación, denegación de permiso, 5 MB y 15 evidencias.
6. Solicitud del liquidador, aviso local, pendiente, adjunto coincidente, entrega local y desbloqueo de etapa.
7. Intento fallido de avanzar con documento pendiente.
8. Cierre permitido, historial visible sin gestiones internas y lectura de avisos.
9. Reinicio, modo avión, accesibilidad, rotación de pantalla y letra del sistema aumentada.
10. Evidencia de autoría verificable de ambos estudiantes en GitHub y Trello.

## Normas de integración

- No añadir comentarios al código Kotlin, XML o Gradle modificado.
- Mantener las ramas main y qa para integración y verificación; no adelantar cambios a entrega-01 sin coordinación del equipo.
- No atribuir implementaciones docentes o autoría a otra persona.
- No llamar requisito cumplido a una funcionalidad sin prueba de compilación, pruebas automatizadas y validación manual cuando corresponde.
