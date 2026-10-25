# Perfiles ficticios para evaluar Seguros BPO

La aplicación **no tiene registro ni inicio de sesión real**. Al abrirla por primera vez aparece "¿Cómo quieres ingresar?", con dos pestañas: **Asegurado** y **Liquidador**. Basta con pulsar "Entrar como…".

Esto sigue el caso del curso: FA-01 excluye crear cuentas y claves; S-02 permite identificar usuarios de forma simulada y RNF-08 prohíbe solicitar y almacenar contraseñas.

## Personas aseguradas de demostración

El selector incluye ocho personas ficticias del enunciado: Camila Rojas, Jorge Pizarro, Valentina Soto, Héctor Araya, Daniela Vergara, Ricardo Espinoza, Francisca Morales y Luis Carrasco.

Varias tienen siniestros de ejemplo; también pueden registrar nuevos siniestros y adjuntar imágenes o PDF ficticios.

## Equipo de liquidación

| Nombre | Especialidad | Acceso |
|---|---|---|
| María P. | Vehículos | Seleccionar pestaña **Liquidador** y tocar su perfil |
| Pedro C. | Hogar | Seleccionar pestaña **Liquidador** y tocar su perfil |

Para revisar un siniestro recién reportado, el liquidador entra a los **casos disponibles** de su especialidad y pulsa **Tomar caso**. Luego puede abrir sus documentos, solicitar antecedentes y registrar gestiones. Para volver al asegurado, usar **Cambiar perfil**.

## Migración y límites

- Una actualización de la versión previa convierte las cuentas locales antiguas en perfiles elegibles **sin sus hashes ni sales de contraseña**; mantiene fotos, teléfonos e identificadores de casos.
- Cada cambio de perfil usa la **misma base SQLite del dispositivo**. En otro teléfono los casos creados localmente no aparecerán automáticamente.
- No es un mecanismo de seguridad real: usar exclusivamente identidades, fotos y documentos **inventados** para las pruebas académicas.
