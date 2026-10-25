# Seguros BPO — interfaz profesional y usabilidad

Fecha: 25 de octubre de 2026.

## Propósito

Aplicar a la entrega Android las observaciones del usuario sin modificar
la arquitectura Kotlin/Jetpack Compose/MVVM/SQLite ni los roles y reglas de negocio.
Rama de trabajo: feature/ux-profesional-20261025, creada desde
entrega-final-android-seguros-bpo (a42452a).

## Decisiones de diseño

- **Marca:** conservar el logo actual mi_siniestro_logo.png. No crear
  otro logo ni incorporar imágenes generadas. Reutilizar vectores existentes
  y añadir los iconos que faltan en los recursos de la app.
- **Menú:** navegación lateral accesible desde el icono de
  hamburguesa en las dos pantallas principales. Las pantallas secundarias
  conservan su retroceso. Eliminar botones redundantes del inicio.
- **Evidencias:** captura o selección de archivo → borrador visible →
  confirmación «Guardar documento» → persistencia SQLite local. Se puede
  cancelar o corregir el tipo antes de guardar. Permanecen los límites
  de 15 archivos y 5 MB, restricciones por rol y por caso cerrado.
  El borrador permanece al girar el dispositivo y el guardado continúa
  a través del ViewModel aunque se recree la pantalla.
- **Reporte:** selector de fecha del dispositivo restringido a días no
  futuros y buscador de comunas chilenas, con escritura libre para comunas
  no listadas. La fecha se sigue guardando como dd-MM-yyyy.
- **Liquidador:** cuatro pestañas: Resumen, Mis casos, Gestiones y Documentos.
  Las acciones anteriores y sus validaciones permanecen, pero los formularios
  se muestran únicamente al seleccionar el caso correspondiente.
  Los borradores de gestión y solicitud se conservan entre pestañas.
- **Mensajes:** textos profesionales. Los avisos y expedientes son
  **locales**; no se atribuyen entregas ni autenticación remota a una
  aseguradora. La migración SQLite 5→6 corrige textos heredados de los casos
  precargados sin eliminar información. Las claves internas persistidas de
  perfiles de acceso rápido permanecen compatibles.

## Enfoques considerados

1. Rediseñar todas las rutas alrededor de un encabezado global:
   coherencia visual, pero refactorización y riesgo innecesarios.
2. Mantener rutas actuales e introducir menú en pantallas raíz, iconografía
   y pestañas especializadas: **elegido**, menor riesgo y conserva retrocesos.
3. Usar ilustraciones rasterizadas para iconos: descartado; los vectores son
   más legibles, ligeros y accesibles.

## Verificación de aceptación

- Ingresar como asegurado y liquidador; abrir/cerrar menú y cambiar de pantalla.
- Reportar un siniestro usando calendario, comuna sugerida y nombre libre.
- Seleccionar fotografía/cámara/PDF, cancelar una selección, guardar otra y
  comprobar en el expediente local que aparece exactamente una evidencia.
- Rechazar adjuntos en caso cerrado y superar límites de archivos.
- Comprobar avisos locales y la ausencia de lenguaje académico en pantalla.
- Revisar cada pestaña del liquidador y registrar una gestión y solicitud real
  en SQLite local, con validación de avance y cierre.
- Ejecutar pruebas unitarias, compilar APK y verificar arranque en Android 30/35.

Los resultados de CI se informan por SHA; no se confunde la ejecución de
pruebas unitarias con la cobertura instrumental completa mantenida en QA.
