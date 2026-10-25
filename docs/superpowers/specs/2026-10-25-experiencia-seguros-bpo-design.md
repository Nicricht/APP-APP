# Seguros BPO: rediseño y cierre de flujos

## Intención

Convertir las pantallas existentes de la app Android en una experiencia clara y coherente para asegurados y liquidadores, tomando las nueve maquetas visuales acordadas como referencia. Corregir las interacciones que dificultan registrar un siniestro, adjuntar pruebas y consultar el expediente.

## Objetivos

- Identidad visual consistente: azul marino, turquesa, superficies claras, buena jerarquía y contraste.
- Formularios utilizables con teclado abierto, RUT ejemplificado, contraseña visible bajo demanda y confirmación accesible.
- Compañía y tipo de siniestro sin selección inicial; comuna editable según el caso.
- Documentos con tipos elegibles, comentario opcional, sin mensajes técnicos; consulta de imágenes/PDF para asegurado y liquidador con autorización por caso.
- Mejorar perfil y avisos con acciones concretas; dejar claro cómo tomar casos desde la cuenta liquidadora.
- Conservar historial, requisitos de negocio y datos actuales durante las migraciones.

## Límites y decisiones

- Kotlin, Jetpack Compose Material 3, MVVM y SQLite existentes; no cambiar de plataforma.
- El repositorio actual no tiene backend: la sincronización entre dispositivos queda fuera de este incremento. La información debe circular entre roles en un mismo dispositivo.
- No agregar pantallas obligatorias de región o ciudad; solo comuna.
- Se mantiene temporalmente el acceso por contraseña de la rama para evitar romper las cuentas existentes, **pendiente la decisión de cumplimiento literal de RNF-08** (no solicitar claves).
- No reemplazar adjuntos reales con imágenes generadas ni confundir maquetas con pantallas funcionales.

## Alternativas consideradas

1. Rehacer la app y su capa de datos: alto riesgo de perder flujos y persistencia existentes.
2. Rediseñar sobre Compose/SQLite actual, reparando comportamientos de forma incremental: **seleccionada por compatibilidad, menor riesgo y pruebas aislables**.
3. Mantener UI actual y cambiar solo colores: insuficiente para las quejas del usuario.

## Resultado esperado y verificación

Cada flujo debe tener mensajes claros, controles accesibles, acciones navegables y verificaciones por rol. Compilar, ejecutar pruebas unitarias/instrumentadas cuando el entorno lo permita y confirmar los recorridos en un teléfono Android; documentar cualquier prueba bloqueada por el entorno.
