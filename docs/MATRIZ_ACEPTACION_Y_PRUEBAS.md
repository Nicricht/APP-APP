# Matriz de aceptación y pruebas para finalizar Seguros BPO

**Versión:** 1.0, 23-10-2026  
**Aplicación:** Seguros BPO, rama qa.  
**Propósito:** convertir cada regla importante en evidencia verificable. Esta matriz es un plan de pruebas; las casillas vacías no significan implementación ni ejecución exitosa.  
**Referencia:** [Plan maestro](PLAN_MAESTRO_FINALIZACION.md) y [Protocolo QA](PROTOCOLO_QA_OBLIGATORIO.md).

## 1. Convención

- P0: bloqueante por seguridad, integridad de información o requisito central.
- P1: obligatorio para la experiencia final acordada o para calidad de evaluación.
- P2: mejora deseable.
- U: prueba unitaria. I: integración SQLite. A: prueba instrumentada Android/Compose. M: comprobación manual real.
- Terminado: el test verifica comportamiento y resultado persistido, pasa en qa y tiene evidencia del SHA. Una prueba definida pero no ejecutada permanece pendiente.

## 2. Bloqueo previo de QA y repositorio

| ID | Prioridad | Caso | Resultado esperado | Tipo |
|---|---|---|---|---|
| QA-001 | P0 | Examinar artefacto del run 40002987884 | Identificar nombres de tests fallidos, stack trace y causa antes de corregir | I,A |
| QA-002 | P0 | Ejecutar instrumentadas tras arreglar causas | Verde en el commit que contiene la corrección, no un commit anterior | A |
| QA-003 | P0 | Ejecutar build y tests unitarios | APK de Seguros BPO, compilar androidTest y suite local sin fallos | U,A |
| QA-004 | P0 | Comprobar SHA y ramas | Cambios solo en qa; main y entrega-01 idénticas a línea base | I |
| QA-005 | P1 | Reportar cobertura JaCoCo | Tasas de líneas y ramas y faltantes por archivo; no inventar 100% | U,A |

## 3. Cuentas, sesión y permisos

| ID | Prioridad | Escenario | Resultado esperado | Tipo |
|---|---|---|---|---|
| AUT-001 | P0 | Crear asegurado con datos ficticios válidos | Cuenta única persistida con rol ASEGURADO | U,I,A |
| AUT-002 | P0 | Correo duplicado normalizado | Error por campo, no segunda cuenta | U,I,A |
| AUT-003 | P0 | RUT ficticio inválido o repetido | Rechazo y mensaje comprensible | U,I,A |
| AUT-004 | P0 | Clave de demo vacía o confirmación distinta | Rechazo y error visual; sin guardar texto claro | U,I,A |
| AUT-005 | P0 | Crear cuenta e intentar establecer rol LIQUIDADOR | Nunca conceder privilegio desde el formulario | U,I,A |
| AUT-006 | P0 | Entrar con cuenta de liquidador preconfigurada | Panel de liquidador de su especialidad | I,A |
| AUT-007 | P0 | Credenciales inválidas | No entrar y no mostrar datos sensibles | U,A |
| AUT-008 | P0 | Cerrar sesión y pulsar Atrás | No recuperar pantalla protegida anterior | A,M |
| AUT-009 | P0 | Reiniciar aplicación | Sesión demo y pantallas conforme a política, sin exponer otras cuentas | I,A |
| AUT-010 | P0 | Abrir ID de siniestro de otro asegurado | Acceso denegado incluso fuera de navegación normal | U,I,A |
| AUT-011 | P0 | Liquidador intenta acceder a caso ajeno asignado | Acceso denegado y sin datos internos | I,A |
| AUT-012 | P0 | Asegurado intenta registrar gestión/cambiar etapa | Operación rechazada en capa de datos | U,I |
| AUT-013 | P0 | Revisión de logs y archivos persistidos | No hay claves en texto claro, tokens reales ni datos sensibles innecesarios | M |
| AUT-014 | P1 | Foto de perfil mediante cámara o galería | Foto asociada a su cuenta y persistente | I,A,M |

## 4. Registro y propiedad del siniestro

| ID | Prioridad | Escenario | Resultado esperado | Tipo |
|---|---|---|---|---|
| SIN-001 | P0 | Crear reporte correcto de vehículo | Número SIN-AA-NNNNNN único, etapa RECIBIDO, área VEHICULOS | U,I,A |
| SIN-002 | P0 | Crear reporte correcto de hogar | Área HOGAR, sin liquidador | U,I,A |
| SIN-003 | P0 | Fecha inexistente o futura | Bloquear formulario con error visible | U,A |
| SIN-004 | P0 | Reporte con más de 30 días | Permitir registro y advertir fuera de plazo | U,I,A |
| SIN-005 | P0 | Compañía inactiva | Rechazar reporte nuevo, conservar históricos | U,I |
| SIN-006 | P0 | Mismo propietario/compañía/tipo/fecha y caso abierto | Mostrar posible duplicado y permitir confirmación de otro caso | U,I,A |
| SIN-007 | P0 | Registros concurrentes | Dos números distintos, nunca duplicar clave única | I |
| SIN-008 | P0 | Buscar y abrir caso | Solo propios, listado ordenado y detalle consistente | I,A |
| SIN-009 | P1 | Corregir descripción RECIBIDO | Cambio permitido, auditado y actualizado | I,A |
| SIN-010 | P0 | Corregir descripción en etapa posterior/cerrada | Rechazado | I,A |

## 5. Bandeja por área y asignación individual

| ID | Prioridad | Escenario | Resultado esperado | Tipo |
|---|---|---|---|---|
| ASI-001 | P0 | Reporte vehicular nuevo | Visible en Disponibles del equipo Vehículos | I,A |
| ASI-002 | P0 | Liquidador Hogar busca reporte de Vehículos | No aparece ni puede tomarlo | I,A |
| ASI-003 | P0 | Tomar caso recibido de su área | Transacción asigna persona, cambia a EVALUACION, historial y aviso | I,A |
| ASI-004 | P0 | Dos liquidadores toman mismo caso | Exactamente uno asignado; el otro recibe caso ya tomado | I |
| ASI-005 | P0 | Intentar tomar caso ya asignado/cerrado | Rechazar, sin sobrescribir ni crear evento falso | U,I |
| ASI-006 | P0 | Fallo forzado entre asignación y auditoría | Rollback completo; sin asignación parcial | I |
| ASI-007 | P0 | Cambiar sesión de liquidador | Mis casos solo muestra los del nuevo ID | I,A |
| ASI-008 | P1 | Asegurado consulta después de asignación | Muestra EVALUACION, nombre permitido, próximo paso y aviso | I,A |

## 6. Estados, gestiones y cierres

| ID | Prioridad | Escenario | Resultado esperado | Tipo |
|---|---|---|---|---|
| EST-001 | P0 | RECIBIDO a EVALUACION por asignación | Transición permitida y auditada | U,I |
| EST-002 | P0 | EVALUACION a LIQUIDACION sin pendientes | Transición permitida | U,I,A |
| EST-003 | P0 | EVALUACION a LIQUIDACION con un pendiente | Bloqueo sin cambio de etapa | U,I,A |
| EST-004 | P0 | Saltar o retroceder etapas | Rechazo | U,I |
| EST-005 | P0 | Cerrar desde RECIBIDO | Solo Desistido y mensaje obligatorio | U,I |
| EST-006 | P0 | Cerrar desde EVALUACION | Solo Rechazado o Desistido y mensaje obligatorio | U,I |
| EST-007 | P0 | Cerrar desde LIQUIDACION | Solo Aprobado o Rechazado y mensaje obligatorio | U,I |
| EST-008 | P0 | Modificar CERRADO o reabrir | Rechazado | U,I,A |
| EST-009 | P0 | Registrar gestión interna | Liquidador autorizado la ve; asegurado no | U,I,A |
| EST-010 | P0 | Registrar gestión visible | Aparece en historial de asegurado con fecha y actor | I,A |
| EST-011 | P1 | Siete días sin gestión visible de caso abierto | Registrar/advertir acción periódica según regla sin duplicados | U,I |
| EST-012 | P0 | Cierre | Resultado y motivo visibles donde corresponde; sin afirmar que app decidió cobertura | I,A |

## 7. Documentos, evidencia y recordatorios

| ID | Prioridad | Escenario | Resultado esperado | Tipo |
|---|---|---|---|---|
| DOC-001 | P0 | Liquidador solicita documento | Solicitud Pendiente con fecha, caso y aviso | U,I,A |
| DOC-002 | P0 | Solicitud igual ya pendiente | Rechazar duplicado | U,I |
| DOC-003 | P0 | Asegurado adjunta JPG/PNG/WEBP/PDF válido | Archivo persistido, tipo correcto y vinculado al caso | U,I,A,M |
| DOC-004 | P0 | Archivo 0 bytes, >5MB o MIME no permitido | Rechazo claro, sin archivo huérfano | U,I,A |
| DOC-005 | P0 | Archivo 15 y 16 | Se admite hasta el máximo; el 16 se rechaza | U,I |
| DOC-006 | P0 | Documento solicitado correctamente entregado | Solicitud actualizada y evidencia vinculada de forma transaccional | I,A |
| DOC-007 | P0 | Asegurado intenta adjuntar a caso cerrado | Rechazo | U,I,A |
| DOC-008 | P0 | Selector de galería/PDF cancelado | Vuelve sin cierre inesperado ni registros fantasma | A,M |
| DOC-009 | P0 | Cámara con permiso denegado | Error útil, se puede seguir usando la app | A,M |
| DOC-010 | P0 | Cámara concede permiso y captura real | Vista previa y evidencia sobreviven reinicio | A,M |
| DOC-011 | P1 | Solicitud pendiente 15 días | Recordatorio una vez según periodo definido; sin avisos repetidos | U,I |
| DOC-012 | P1 | Solicitud pendiente 30 días | Permite evaluación de desistimiento, nunca auto-cierre | U,I |
| DOC-013 | P1 | Se entrega nueva versión de un adjunto | Conserva original e historial | I,A |
| DOC-014 | P0 | Guardado local correcto | UI dice Entregado localmente; no confirmar recepción de servidor | I,A |

## 8. Avisos y estadísticas

| ID | Prioridad | Escenario | Resultado esperado | Tipo |
|---|---|---|---|---|
| AVI-001 | P0 | Asignar, cambiar etapa, solicitar documento o cerrar | Aviso con número de siniestro, fecha y mensaje correcto | I,A |
| AVI-002 | P0 | Gestión interna | Nunca genera aviso visible al asegurado | I,A |
| AVI-003 | P1 | Marcar aviso leído | Persiste estado al salir y volver | I,A |
| AVI-004 | P0 | Cambiar cuenta | Avisos de otro asegurado invisibles | I,A |
| GRA-001 | P1 | Asegurado sin siniestros | Cero/estado vacío sin divisiones por cero ni datos de ejemplo falsos | U,A |
| GRA-002 | P1 | Asegurado con casos propios | KPI, torta/barras y filtros reflejan solo sus casos | U,I,A |
| GRA-003 | P1 | Liquidador con sus casos | KPI, gráficos por etapa/tipo/cierre solo asignados | U,I,A |
| GRA-004 | P1 | Caso tomado y luego cerrado | Recuentos se recalculan correctamente y no se duplican | U,I,A |
| GRA-005 | P1 | Gráficos con escala de texto grande | Etiquetas legibles y cifras disponibles como texto | A,M |

## 9. Migración, rendimiento, accesibilidad y APK

| ID | Prioridad | Escenario | Resultado esperado | Tipo |
|---|---|---|---|---|
| BD-001 | P0 | Instalación nueva | Esquema coherente, datos de demostración válidos | I,A |
| BD-002 | P0 | Actualizar desde SQLite v5 | Mantener siniestros, números, imágenes, gestiones, solicitudes y avisos | I,A |
| BD-003 | P0 | Actualizar desde cada versión anterior soportada | Migraciones incrementales sin destruir registros | I |
| BD-004 | P0 | Fixture enum a usuario con ID | Todos los casos preservan propietario correcto | I |
| BD-005 | P0 | Índices, claves foráneas y transacciones | Integridad referencial y rollback garantizados | I |
| UX-001 | P1 | Reporte persona sin experiencia | Completar en menos de cinco minutos en prueba de usabilidad | M |
| UX-002 | P1 | Consultar etapa desde abrir app | Encontrar estado en menos de treinta segundos | M |
| UX-003 | P1 | Pantalla pequeña, fuente grande y teclado | No perder botones, inputs ni navegación | A,M |
| UX-004 | P1 | Animaciones y mensajes | Funcionan sin dificultar acceso o legibilidad | A,M |
| UX-005 | P0 | Offline tras reinicio | Consulta local consistente con fecha de última actualización; sin inventar sincronización | I,A,M |
| UX-006 | P1 | Guardar borrador e interrumpir formulario | Información recuperable sin crear reportes duplicados | I,A |
| APK-001 | P0 | Compilar Seguros BPO desde qa | APK debug generado por workflow propio de Seguros BPO | A |
| APK-002 | P0 | Instalar Android 11/API 30 | Instalación correcta, launch activity, icono propio, sin testOnly | M |
| APK-003 | P0 | Instalar otra API Android 35 | Mismas rutas esenciales sin cierre inesperado | M |
| APK-004 | P0 | Reiniciar y repetir recorrido completo | Sesión demo, DB, documentos, avisos y estados persisten según política | A,M |
| EQU-001 | P0 | Evidencia de trabajo de ambos | Commits/PR/issue/Trello auténticos, tareas y verificaciones demostrables | M |

## 10. Trazabilidad requisito -> grupos de pruebas

| Requisito | Pruebas principales |
|---|---|
| RF-01 identificación/separación | AUT-001 a AUT-013, SIN-008, AVI-004 |
| RF-02/03 reporte y número | SIN-001 a SIN-007 |
| RF-04/05 casos y detalle | SIN-008, ASI-008, UX-002 |
| RF-06 historial | EST-009, EST-010, EST-012 |
| RF-07 evidencia | DOC-003 a DOC-010, DOC-013 |
| RF-08 documentación pendiente | DOC-001, DOC-002, DOC-006, DOC-011, DOC-012 |
| RF-09 avisos | AVI-001 a AVI-004 |
| RF-10/11 gestión de liquidador | ASI-001 a ASI-008, EST-001 a EST-012 |
| RF-12 auditoría | ASI-003, EST-010, EST-012, DOC-006 |
| RN-01/02 etapa/cierre | EST-001 a EST-008, EST-012 |
| RN-03 a 08 RUT/reporte/asignación | AUT-003, SIN-001 a SIN-007, ASI-001 a ASI-008 |
| RN-09 a RN-11 documentos | EST-003, DOC-001 a DOC-014 |
| RN-12 a RN-18 desistimiento/visibilidad/plazos/inactivos | EST-005 a EST-012, DOC-011/012, SIN-005, ASI-008 |
| Pauta 15 UI | UX-001 a UX-004, recorridos instrumentados |
| Pauta 15 formularios | AUT, SIN, DOC, EST |
| Pauta 10 validación/reactividad | U/I de AUT, SIN, ASI, EST, DOC y GRA |
| Pauta 10 animaciones | UX-004 |
| Pauta 15 POO | Evidencia de interfaces, herencia y polimorfismo de negocio usado; tests de dominio |
| Pauta 20 colaboración | EQU-001 |
| Pauta 15 recursos nativos | DOC-008 a DOC-010, AUT-014 |

## 11. Secuencia E2E que debe grabarse antes de entrega

1. Crear cuenta demo de asegurado, iniciar sesión y verificar perfil.
2. Reportar siniestro de Vehículos, capturar número generado y etapa Recibido.
3. Cerrar sesión y entrar como liquidador de Hogar; demostrar que no puede tomar ese caso.
4. Entrar como liquidador de Vehículos; abrir Disponibles y tomar el caso.
5. Ver el caso ahora en Mis casos; generar gestión visible y otra interna.
6. Solicitar documento y comprobar Pendiente.
7. Volver al asegurado: ver aviso, etapa, gestión visible y documento solicitado, sin ver la interna.
8. Capturar foto o seleccionar archivo válido; entregar evidencia vinculada.
9. Regresar al liquidador; comprobar recepción local y avanzar a Liquidación conforme a validaciones.
10. Registrar decisión de la aseguradora, resultado y mensaje de cierre.
11. Volver al asegurado: consultar cierre, fecha, mensaje y gráficos actualizados.
12. Reiniciar Android: comprobar persistencia y permisos; documentar enlaces a ejecución QA y commit.

## 12. Control de ejecución

Una matriz documentada NO significa que los casos ya pasaron. Registrar para cada ejecución el SHA, fecha, emulador/dispositivo/API, resultado, URL de GitHub Actions, captura/artefacto y causa si falla. Repetir la suite afectada y la regresión tras cada corrección en qa.

**No promover cambios a main mientras exista fallo bloqueante ni atribuir nota/cobertura no demostrada al profesor.**