# Validación visual por campo en acceso/registro (24-10-2026)

## Intención y límites

Cerrar una brecha de la pauta DSY1105 (formularios: 15 puntos): la pantalla
`AccesoScreen` hoy presenta errores de registro únicamente al pie. Mostrar el
motivo junto a cada campo inválido, con estado visual Material 3, y actualizarlo
cuando la persona corrija su entrada.

Se conserva estrictamente el proyecto Android local: Kotlin, Jetpack Compose,
Material 3, MVVM/StateFlow, SQLite, sin Spring Boot, Retrofit ni Java nuevo.
No implementar autenticación remota ni extender los datos personales pedidos.
No cambiar las ramas de entrega.

## Enfoque

- **Elegido para ejecución:** estructurar los errores actuales en
  `ValidadorCuenta`, mantener `errorRegistro()` compatible con su contrato
  existente y consumir los errores en `AccesoScreen` con `isError` y
  `supportingText`. La devolución del callback sigue siendo visible para errores
  de persistencia (como cuenta duplicada).
- **Descartado:** interpretar textos devueltos por SQLite en la UI para decidir
  qué campo iluminar; eso acopla presentación y persistencia a frases literales.
- **Descartado:** reescribir la pantalla con un ViewModel adicional; sobredimensiona
  un ajuste pequeño y puede alterar el flujo de demostración existente.

## Casos a proteger

1. Varios campos inválidos informan todos sus errores en una sola revisión.
2. Al corregir un campo, desaparece su error sin ocultar los restantes.
3. Un registro válido no genera errores locales.
4. La validación de dominio continúa priorizando el primer error como antes.
5. Login y botones demo mantienen su funcionamiento y mensajes.

Verificar con pruebas unitarias del validador y Compose instrumentadas. El
emulador y GitHub Actions son evidencia técnica, no certificación en teléfono.
