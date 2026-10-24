# Ejecutar Seguros BPO y probar su APK Android

**Rama para desarrollar y validar:** `qa`  
**Aplicación:** `cl.duocuc.segurosbpo` (APK `debug`: `cl.duocuc.segurosbpo.prueba`)  
**Corte:** 24-10-2026

Esta guía corresponde al código vigente de `qa`. La rama `main` se mantiene para presentación hasta que el equipo apruebe una promoción. No confundir el APK de Seguros BPO con el APK de `MiRegistroAvatar` del profesor. El código de ejemplo se utiliza como referencia, no como sustituto de la aplicación.

## 1. Abrir el código en Android Studio

1. Abrir [APP-APP](https://github.com/Nicricht/APP-APP/tree/qa) y seleccionar la rama `qa`.
2. Clonar o descargar los archivos de esa rama. No abrir el ZIP `Proyecto_Seguros_BPO-main.zip` incluido en el repositorio, porque es material de referencia antiguo.
3. En Android Studio, abrir la carpeta que contiene `settings.gradle.kts`, `gradlew` y `app/`.
4. Esperar Gradle Sync. El workflow de CI usa Java 25 y Android SDK 37.2.
5. Conectar un teléfono con depuración USB habilitada, o iniciar un emulador. La app declara `minSdk = 24` (Android 7.0), pero las instalaciones automatizadas del equipo se prueban específicamente en API 30 y API 35.
6. Seleccionar el módulo `app` y ejecutar la variante `debug`.

## 2. Recorrido del asegurado

1. Abrir Seguros BPO. El punto de entrada es **Iniciar sesión**.
2. Para una cuenta nueva, pulsar **Crear cuenta de asegurado**. Completar nombre, RUT ficticio con dígito verificador correcto, correo de prueba, teléfono y contraseña de demostración de al menos ocho caracteres; confirmar contraseña.
3. Tras registrar la cuenta, se inicia la sesión de ese asegurado. También existe **Continuar como Camila (demo)** para navegar sin crear una cuenta real de prueba.
4. En **Mis siniestros**, consultar la lista, buscar por número, compañía, tipo, comuna o etapa, y abrir un caso.
5. Pulsar **Reportar**, elegir compañía activa y tipo de siniestro, añadir fecha de ocurrencia en formato `dd-MM-yyyy`, comuna y descripción y confirmar los datos. Guardar y comprobar el identificador `SIN-AA-NNNNNN`.
6. Entrar al detalle para comprobar etapa, historial, próxima acción y documentos pendientes. En un siniestro permitido se pueden adjuntar fotos de cámara, imágenes de galería o archivos PDF; todo queda almacenado en SQLite local.
7. En **Mi perfil**, la cuenta creada puede actualizar teléfono y foto. La cámara requiere permiso; cancelar una captura o selección no debe guardar una imagen.
8. Consultar **Centro de avisos** y **Ver estadísticas** para ver solo información de la cuenta.
9. Cerrar sesión, volver a entrar con ese correo y contraseña de prueba, y comprobar que los datos locales siguen disponibles.

## 3. Recorrido del liquidador

Las cuentas de ensayo instaladas localmente se describen en [Cuentas demo QA](CUENTAS_DEMO_QA.md). Sus credenciales ficticias permiten entrar desde el formulario normal de inicio de sesión. También existen accesos directos a los dos liquidadores de demostración.

1. Iniciar sesión como liquidador de **Vehículos** o **Hogar**.
2. Revisar **Disponibles en [área]**: solo aparecen siniestros nuevos de la especialidad correspondiente.
3. Pulsar **Tomar caso**. La app cambia la etapa a **En evaluación**, asigna al liquidador y genera un movimiento histórico y un aviso para el asegurado.
4. Comprobar que el caso ya no aparece como disponible y que no puede tomarse otra vez.
5. Abrir **Mis casos asignados**. Registrar una gestión visible o interna, solicitar documentación y pasar de etapa solo cuando las reglas permitan hacerlo.
6. Una solicitud documental pendiente impide avanzar de evaluación a liquidación. Los cierres exigen resultado permitido y motivo.
7. Revisar gráficos de gestión y perfil. Cerrar sesión.

Estos flujos usan dos perfiles de liquidador asociados a enumeraciones locales. Todavía no hay una API remota ni liquidadores independientes y sincronizados en distintos dispositivos.

## 4. Obtener e instalar la APK de QA en el teléfono

1. Abrir [GitHub Actions](https://github.com/Nicricht/APP-APP/actions) y elegir una ejecución **Android Kotlin QA** de la rama `qa` con resultado **success**. Anotar el SHA; las pruebas instrumentadas deben estar verdes para ese mismo SHA.
2. En **Artifacts**, descargar **SegurosBPO-debug-apk**. El artefacto descargado es un archivo ZIP; extraer `app-debug.apk`. **No intentar instalar el ZIP**.
3. Copiar `app-debug.apk` al teléfono. Abrirlo desde Archivos y, si Android lo solicita, permitir instalar aplicaciones de esa fuente. Esta es una APK de desarrollo, no una versión publicada en Play Store.
4. Comprobar que el instalador muestra Seguros BPO, que se abre la pantalla de inicio de sesión y que el icono no es el Android predeterminado.
5. Ejecutar los recorridos de los apartados 2 y 3; probar permisos, cámara, galería, PDF, cierre y reinicio en el teléfono real.

Los artifacts de Actions de esta rama tienen retención de 14 días. Obtener siempre la APK del SHA que se está auditando.

## 5. Diagnosticar «No se instaló» sin perder datos innecesariamente

Si hay un computador con Android Platform Tools y depuración USB, abrir una terminal en la carpeta de la APK:

~~~powershell
adb devices
adb install -r app-debug.apk
~~~

El mensaje concreto de ADB indica la causa:

- `INSTALL_FAILED_UPDATE_INCOMPATIBLE`: ya existe una app con **el mismo paquete** pero firma incompatible. Las builds `debug` realizadas en runners temporales de GitHub pueden usar claves diferentes. **No desinstalar directamente si importa preservar los registros locales**. La desinstalación elimina normalmente los datos SQLite de esa aplicación.
- `INSTALL_FAILED_VERSION_DOWNGRADE`: versión instalada con `versionCode` mayor. Revisar cuál versión debe mantenerse.
- `INSTALL_FAILED_INSUFFICIENT_STORAGE`: liberar espacio.
- `INSTALL_PARSE_FAILED_*` o «archivo no válido»: comprobar que realmente se extrajo `app-debug.apk` y que la transferencia no corrompió el archivo.

**Solo si los datos de la instalación anterior son descartables** y se ha confirmado el error de firma, se puede eliminar la versión `debug` e instalar de nuevo:

~~~powershell
adb uninstall cl.duocuc.segurosbpo.prueba
adb install app-debug.apk
~~~

La variante `debug` usa el paquete `cl.duocuc.segurosbpo.prueba`; no ejecutar este comando contra otro paquete. Para comprobar inicio desde la terminal:

~~~powershell
adb shell am start -n cl.duocuc.segurosbpo.prueba/cl.duocuc.segurosbpo.MainActivity
adb shell pidof cl.duocuc.segurosbpo.prueba
~~~

Una prueba en emulador no demuestra que el teléfono particular del equipo esté validado. Registrar en la [Bitácora QA](BITACORA_AVANCES_QA.md) el modelo de Android, la versión del sistema, el SHA, resultado de instalación y evidencias sin capturar datos personales.

## 6. Pruebas del repositorio

Para revisar la versión de `qa` localmente:

~~~bash
git fetch origin
git switch qa
git pull --ff-only origin qa
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest :app:createDebugUnitTestCoverageReport --no-daemon
~~~

Con emulador operativo:

~~~bash
./gradlew :app:createDebugAndroidTestCoverageReport --no-daemon
~~~

Revisar los artefactos **cobertura-unitaria-lineas-ramas**, **informes-android-instrumentado**, **seguros-bpo-instalacion-api-30** y **seguros-bpo-instalacion-api-35** de la ejecución correspondiente. Una compilación verde no equivale a 100 % de líneas y ramas.

## 7. Limitaciones actuales

- SQLite guarda los datos en **el teléfono**. No hay sincronización multiusuario o multi-dispositivo ni backend Spring Boot/REST en esta fase.
- La captura de cámara usa `TakePicturePreview`, no una foto a resolución completa.
- Los archivos PDF se adjuntan localmente y no existe un visor PDF integrado.
- Los accesos directos y credenciales demo son para evaluación académica, no para información real.
- Faltan evidencias de uso completo en teléfono físico, revisión de accesibilidad y cobertura total de las ramas pendientes.

Consultar también el [Protocolo QA](PROTOCOLO_QA_OBLIGATORIO.md) y la [Matriz de aceptación](MATRIZ_ACEPTACION_Y_PRUEBAS.md).
