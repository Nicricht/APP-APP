# Acceso simulado sin contraseñas — diseño

## Motivo y fundamento

El caso Duoc indica en **FA-01** que quedan fuera de alcance el registro, la autenticación real y la recuperación de claves; **S-02** admite perfiles simulados para RF-01, y **RNF-08** prohíbe solicitar o almacenar claves. La pantalla de registro e inicio de sesión actual contradice esas restricciones.

## Enfoques

1. **Elegir perfiles ficticios preconfigurados (elegido):** ocho asegurados del conjunto de pruebas y dos liquidadores de las áreas Vehículos y Hogar. Un toque abre la app. Compatible con S-02 sin pedir información.
2. Mantener cuentas con contraseñas: fuera de alcance y contrario a RNF-08.
3. Solicitar solamente RUT: reduce la fricción, pero expone identificación innecesaria y se presta a ingresar RUT de una persona real.

## Alcance

- Pantalla inicial con selector de rol y perfil ficticio, sin formulario ni credenciales.
- Persistencia de la selección entre aperturas y opción visible para cambiar perfil.
- Mantener perfiles locales anteriores sin claves para no perder acceso a sus expedientes; no permitir crear otros nuevos.
- Migrar SQLite de cuentas eliminando las columnas que almacenan sal y hash; conservar datos de perfil sin contraseña, fotos y teléfonos.
- Reutilizar la misma base de siniestros por dispositivo y restringir consultas al perfil/equipo seleccionado; no afirmar autenticación real ni privacidad entre personas que comparten el dispositivo.
- Mantener foto y teléfono editables para evaluar recursos nativos de cámara y galería. Mostrar solo RUT parcial en perfil.
- Actualizar pruebas y documentación para enfatizar RNF-08, FA-01 y S-02.

## Fuera de alcance

- Autenticación real, sincronización entre dispositivos y recuperación de cuentas.
- Eliminar siniestros, gestiones, evidencias o datos históricos de expediente.

La selección de roles es **solo académica**, destinada a datos ficticios y a realizar pruebas en el mismo dispositivo.
