# POO y animaciones para presentar Seguros BPO

Esta explicación corresponde al código real de la rama `main` y a la referencia `MiRegistroAvatar` del profesor. No añade tecnologías ni funcionalidades de otra evaluación.

## POO que ya usamos

| Concepto | Archivo | Ejemplo real |
|---|---|---|
| Clase y objeto | `model/Siniestro.kt` | `data class Siniestro` agrupa información de cada caso |
| Herencia | `data/SiniestroDbHelper.kt` | `SiniestroDbHelper : SQLiteOpenHelper` |
| Sobrescritura | `data/SiniestroDbHelper.kt` | `override fun onCreate` y `override fun onUpgrade` |
| Polimorfismo del framework | `data/SiniestroDbHelper.kt` | Android invoca la versión sobrescrita de los métodos del helper |
| Herencia y ciclo de vida | `viewmodel/SiniestrosViewModel.kt` | `SiniestrosViewModel : AndroidViewModel` y `override fun onCleared()` |
| Estados del negocio | `model/Siniestro.kt` | `enum class EtapaSiniestro`, con cuatro etapas |

**Precisión para la defensa:** esto demuestra herencia, métodos sobrescritos y polimorfismo a través de Android. No demuestra todavía una jerarquía de clases de negocio con métodos virtuales propios. Si el criterio exige polimorfismo de dominio, queda por resolver, en lugar de inventar clases innecesarias.

### Cómo explicarlo con palabras sencillas

`SQLiteOpenHelper` ya sabe abrir y actualizar una base de datos, pero necesita que indiquemos qué tablas crear o cómo modificarlas. Por eso nuestra clase hereda de él y redefine `onCreate` y `onUpgrade`. Android llama estos métodos cuando corresponde.

## Animaciones funcionales

| Dónde | Cuándo ocurre | Código |
|---|---|---|
| `FormularioScreen.kt` | Al guardar correctamente un siniestro | `AnimatedVisibility`, `fadeIn`, `expandVertically` |
| `MisSiniestrosScreen.kt` | Cuando la búsqueda no encuentra siniestros | `AnimatedVisibility`, `fadeIn`, `fadeOut` |

Ambas usan Jetpack Compose. Su función es señalar un cambio al usuario, no decorar la pantalla.

## Demostración recomendada (2 minutos)

1. Abre **Mis siniestros** y escribe un número inexistente en la búsqueda. Comprueba que aparece suavemente el mensaje de ausencia de casos.
2. Borra la búsqueda y entra en **Reportar**. Muestra una fecha inválida y el mensaje de error.
3. Completa el formulario y regístralo. Observa la animación de confirmación.
4. Abre en Android Studio `SiniestroDbHelper.kt`, muestra `SQLiteOpenHelper` y explica qué hacen `onCreate` y `onUpgrade`.
5. Relaciona `FormularioUiState` y `FormularioViewModel` con las mismas ideas del profesor.

**No afirmar** que por mostrar dos animaciones ya se obtuvo el puntaje máximo, ni que el profesor enseñó todas nuestras reglas de siniestros. La nota dependerá de la pauta y de la demostración real.
