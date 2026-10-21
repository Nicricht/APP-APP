# Guía de defensa oral: Seguros BPO

**Objetivo:** poder explicar la aplicación Android con lenguaje de estudiante, mostrando código real en Android Studio y relacionándolo con **MiRegistroAvatar**. No memorizar frases sin entenderlas ni afirmar que algo existe cuando no está implementado.

## 1. Explicación breve del proyecto (30 a 45 segundos)

«Desarrollamos una aplicación Android para registrar y consultar siniestros ficticios de seguros. El asegurado puede ver sus casos, registrar uno, consultar su etapa, revisar el historial y adjuntar imágenes o documentos. También hay una vista local de liquidador para los casos de demostración. Usamos Kotlin y Jetpack Compose para las pantallas, ViewModel y StateFlow para actualizar la información y SQLite para guardarla en el teléfono. Seguimos el ejemplo MiRegistroAvatar del profesor y cambiamos su registro de usuarios por un registro de siniestros. Todavía no se conecta a servidores porque eso corresponde a otra evaluación».

## 2. Cómo se relaciona cada archivo con el profesor

| Nuestro archivo o paquete | Equivalente en `MiRegistroAvatar` | Qué debes ser capaz de decir |
|---|---|---|
| `MainActivity.kt` | `MainActivity.kt` | «Inicia la aplicación y carga el tema y las pantallas de Compose». |
| `navigation/AppNavegacion.kt` | `navigation/AppNavegacion.kt` | «Contiene `NavHost`, que muestra una pantalla según la ruta». |
| `navigation/Rutas.kt` | `navigation/Rutas.kt` | «Define los nombres de las rutas. El ID identifica el caso que quiero abrir». |
| `model/FormularioUiState.kt` | `model/FormularioUiState.kt` | «Agrupa datos del formulario, errores y estado de éxito». |
| `viewmodel/FormularioViewModel.kt` | `viewmodel/FormularioViewModel.kt` | «Recibe cambios, valida y modifica el estado sin mezclar toda la lógica en la pantalla». |
| `model/Siniestro.kt` | `model/Usuario.kt` | «La `data class` representa un siniestro, igual que `Usuario` representa una persona». |
| `viewmodel/SiniestrosViewModel.kt` | `viewmodel/UsuariosViewModel.kt` | «Conecta listas, búsqueda, selección y SQLite con las pantallas». |
| `data/SiniestroDbHelper.kt` | `data/UsuarioDbHelper.kt` | «Crea y actualiza tablas SQLite. En nuestro caso son siniestros, gestiones y evidencias». |
| `ui/screens/MisSiniestrosScreen.kt` | `ui/screens/UsuariosScreen.kt` | «Lista registros, permite buscar y abre el detalle». |
| `ui/screens/EditarSiniestroScreen.kt` | `ui/screens/EditarScreen.kt` | «Permite modificar un dato y guardarlo». |
| `ui/screens/EvidenciasScreen.kt` | `ui/screens/PerfilScreen.kt` | «Usa la cámara y un selector de archivos; el profesor los usa para el avatar». |
| `data/ProcesadorEvidencia.kt` | `data/ConvertidorImagen.kt` | «Prepara bytes de la fotografía; nuestra versión también admite PDF». |
| `ui/components/`, `ui/theme/` | Los mismos paquetes del profesor | «Guardamos estilos y controles reutilizables para no repetirlos en cada pantalla». |

### Adaptaciones propias del caso, no copiadas del profesor

- `model/ReglasEtapa.kt`: indica qué etapa puede seguir y qué exige el cierre.
- `ui/screens/CasosLiquidadorScreen.kt`: bandeja ficticia de casos asignados.
- `ui/screens/HistorialSiniestroScreen.kt`: enseña cambios y gestiones.
- `model/Gestion.kt`, `model/Evidencia.kt`, `model/Liquidador.kt`: describen información propia de los siniestros.
- Archivos de `qa`: pruebas nuevas para nuestra aplicación, además de los dos tests de ejemplo del profesor.

**Regla para la exposición:** di «nos basamos en las técnicas del profesor y las adaptamos», **no** «todo el código lo hizo el profesor» ni «todas estas clases vienen del ZIP».

## 3. El recorrido de los datos que debes saber dibujar

### Al reportar un siniestro

```text
FormularioScreen
     ↓ cambios del usuario
FormularioViewModel + FormularioUiState
     ↓ valida campos
SiniestrosViewModel.registrar(...)
     ↓ guarda datos
SiniestroDbHelper → SQLite
     ↓ número de siniestro y lista actualizada
StateFlow → MisSiniestrosScreen / DetalleSiniestroScreen
```

Frase corta: «La pantalla recoge datos, el ViewModel valida, la base de datos guarda y StateFlow actualiza lo que ve el usuario».

### Al adjuntar una foto

```text
EvidenciasScreen
     ↓ cámara o selector
ProcesadorEvidencia
     ↓ convierte y valida bytes
SiniestrosViewModel.guardarFoto / guardarArchivo
     ↓
SiniestroDbHelper.guardarEvidencia
     ↓
Tabla evidencias de SQLite → lista actualizada
```

Frase corta: «No enviamos la foto a Internet. La asociamos al ID del siniestro en la base de datos local».

### Al cambiar una etapa

```text
CasosLiquidadorScreen
     ↓
SiniestrosViewModel.avanzarLiquidador
     ↓
SiniestroDbHelper.avanzarAsignado
     ↓ verifica ReglasEtapa y asignación
SQLite: cambia etapa + crea una gestión de historial
     ↓
StateFlow actualiza la lista
```

## 4. Preguntas probables del docente

**¿Qué es Jetpack Compose?**  
Una biblioteca para construir las pantallas de Android usando funciones Kotlin marcadas con `@Composable`.

**¿Qué es MVVM?**  
Es una forma de organizar el proyecto: el *Model* representa los datos; la *View* muestra pantallas; el *ViewModel* coordina estados y acciones.

**¿Qué significa `StateFlow`?**  
Es un valor observable. Cuando cambia la lista o el estado, la pantalla que lo observa con `collectAsStateWithLifecycle` se vuelve a mostrar con los datos nuevos.

**¿Para qué sirve `data class`?**  
Para representar información, por ejemplo `Siniestro` con número, compañía, tipo y estado. Kotlin facilita comparar y copiar estos datos.

**¿Qué hace `SQLiteOpenHelper`?**  
Crea una base de datos SQLite local y permite actualizar su estructura mediante `onCreate` y `onUpgrade`. El archivo actual llega a la versión 4 y migra tablas sin tener que borrar los siniestros.

**¿Qué es CRUD?**  
Crear, consultar, actualizar y eliminar. El proyecto del profesor muestra CRUD de usuarios. Nuestra app registra, consulta y corrige siniestros; **no ofrece eliminación libre de siniestros**, porque su historial de seguimiento debe conservarse.

**¿Qué es herencia en nuestro proyecto?**  
Una clase aprovecha una clase base: `SiniestroDbHelper : SQLiteOpenHelper` y `SiniestrosViewModel : AndroidViewModel`. Se usan métodos como `onUpgrade` o `onCleared` mediante `override`.

**¿Y el polimorfismo?**  
En Android, los métodos sobrescritos son llamados a través de contratos de clases base. **No presentamos todavía una jerarquía explícita de tipos de siniestro con comportamiento polimórfico propio**. Como la pauta lo menciona, este es un punto pendiente de validar o desarrollar de manera justificada.

**¿Por qué dos ViewModels?**  
`FormularioViewModel` controla los campos y errores del registro. `SiniestrosViewModel` controla la información compartida entre pantallas y la persistencia. Es una separación parecida a la del profesor.

**¿Por qué hay cámara y galería?**  
Para adjuntar fotografías de daños. La cámara usa `TakePicturePreview` y solicita permiso; la galería usa `GetContent` para elegir una imagen sin permitir acceso a todas las fotos.

**¿Qué pasa sin Internet?**  
Los datos se guardan en SQLite en el teléfono. Esta versión es local: no sincroniza información entre dispositivos.

**¿Se puede entrar con un usuario real?**  
No. La selección de asegurado y liquidador es ficticia, para demostrar los recorridos, sin contraseñas ni autenticación de producción.

**¿Qué pruebas realizaron?**  
En `main` quedaron los ejemplos básicos de JUnit y AndroidJUnit4. En `qa` agregamos pruebas de validación, integración SQLite y recorridos Compose. GitHub Actions ha aprobado compilación y la suite instrumentada, pero no certifica automáticamente toda interacción física con cámara y galería.

**¿Qué es Playwright y por qué no lo usamos?**  
Es una herramienta habitual para automatizar interfaces web. Nuestra aplicación es Android nativa; las pruebas UI se hacen con Compose Test y AndroidJUnit4.

**¿Por qué no hay Spring Boot o Retrofit?**  
Porque el alcance acordado de esta evaluación es la app Android local. Su conexión a servicios se deja para la tercera evaluación.

**¿Qué falta realmente?**  
Solicitud/estado de documentos pendientes, avisos automáticos y asignación ficticia de nuevos casos. También se requieren evidencias de dos recursos físicos y colaboración de ambos integrantes. No afirmar que esas partes están terminadas.

## 5. Ejercicio de exposición con Android Studio

En este orden, abre cada archivo y explica solo tres cosas: **qué recibe, qué hace y qué devuelve o actualiza**.

1. `MainActivity.kt`: recibe el inicio de Android y muestra `AppNavegacion`.
2. `AppNavegacion.kt`: recibe rutas y selecciona la pantalla.
3. `FormularioUiState.kt`: contiene campos y mensajes de validación.
4. `FormularioViewModel.kt`: recibe lo escrito y decide si es válido.
5. `SiniestrosViewModel.kt`: recibe las operaciones y refresca listas.
6. `SiniestroDbHelper.kt`: recibe datos y los guarda o consulta en SQLite.
7. `EvidenciasScreen.kt`: usa cámara y galería para elegir archivos.

Si te preguntan por un método concreto, localiza el nombre y explica una línea significativa. Por ejemplo, `db.registrar(estado)` significa «guardar el formulario validado»; `navController.navigate(...)` significa «abrir otra pantalla»; `_siniestros.value = ...` significa «actualizar la lista observable».

## 6. Qué no debes afirmar en la defensa

- Que todas las funcionalidades del caso están listas.
- Que el profesor desarrolló nuestro liquidador, historial o pruebas adicionales.
- Que existe backend o sincronización por Internet.
- Que hay autenticación segura real.
- Que tenemos 100 % de cobertura JaCoCo o 100/100 de la pauta.
- Que las pruebas del emulador sustituyen la demostración de permisos, cámara y galería.
- Que ambos trabajaron por igual sin poder mostrar sus aportaciones auténticas.

## 7. Preparación mínima de los dos integrantes

- [ ] Cada uno explica sin leer un ViewModel, una pantalla y al menos una operación SQLite.
- [ ] Cada uno demuestra la funcionalidad que realmente construyó o revisó y señala su contribución en GitHub/Trello.
- [ ] Ambos pueden responder qué se tomó de `MiRegistroAvatar` y qué se cambió para Seguros BPO.
- [ ] Ensayan navegación, error de formulario, registro, historial y recursos de dispositivo.
- [ ] Tienen a mano la [lista final de la pauta](CONTROL_FINAL_100_PUNTOS.md) y el enlace a [GitHub Actions](https://github.com/Nicricht/APP-APP/actions).

La mejor defensa es mostrar una función funcionando y explicar con sencillez por qué ese archivo la hace posible.


## 8. POO y animaciones, ejemplos concretos

**Herencia y polimorfismo ya existentes:** abre `data/SiniestroDbHelper.kt` y muestra `class SiniestroDbHelper(...) : SQLiteOpenHelper(...)`. Los métodos `override fun onCreate` y `override fun onUpgrade` son implementaciones propias de métodos definidos por la clase base. Cuando Android abre o actualiza la base de datos, utiliza la implementación de nuestro helper. Esto es sobrescritura de métodos y despacho polimórfico del framework. Otro ejemplo es `SiniestrosViewModel : AndroidViewModel` y su `override fun onCleared()`.

**Límite que debes reconocer:** la aplicación usa herencia y sobrescritura de las clases de Android. No tiene aún una jerarquía específica de siniestros con métodos polimórficos propios; si la pauta exige ese caso particular, debe explicitarse y programarse solo si aporta una función real.

**Primera animación:** en `FormularioScreen.kt`, al registrar un siniestro válido aparece la confirmación mediante `AnimatedVisibility` con entrada gradual y expansión vertical.

**Segunda animación:** en `MisSiniestrosScreen.kt`, si escribes una búsqueda que no encuentra casos, aparece gradualmente el mensaje de lista vacía usando `AnimatedVisibility`, `fadeIn` y `fadeOut`. No hay nuevas pantallas ni bibliotecas externas.

**Prueba de la segunda animación:** busca un número inexistente, observa el aviso, borra el filtro y comprueba que vuelve a aparecer la lista; mostrar el comportamiento en el emulador es mejor que describirlo únicamente.
