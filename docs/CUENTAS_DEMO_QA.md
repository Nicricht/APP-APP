# Cuentas académicas de Seguros BPO

La rama `qa` incluye dos cuentas preconfiguradas de **liquidadores ficticios** con acceso mediante el mismo formulario de correo y contraseña que utilizan los asegurados.

| Rol | Correo | Contraseña de demostración | Área |
|---|---|---|---|
| Liquidador | `vehiculos@segurosbpo.demo` | `Demo2026!` | Vehículos |
| Liquidador | `hogar@segurosbpo.demo` | `Demo2026!` | Hogar |

Para evaluar como asegurado, utilizar Crear cuenta y un RUT **ficticio con dígito verificador válido**. Los accesos directos a Camila y a liquidadores siguen disponibles exclusivamente como atajos didácticos en esta versión de pruebas.

Estas cuentas son locales, no se sincronizan entre teléfonos y **no deben utilizarse con datos verdaderos ni claves reales**. Las contraseñas almacenadas se verifican mediante PBKDF2 con sal, pero los valores de prueba publicados no son secretos ni constituyen autenticación productiva.

El registro público solo puede crear usuarios con rol `ASEGURADO`. La cuenta de `LIQUIDADOR` se provisiona desde la migración local de SQLite (versión 3), preservando las cuentas existentes.
