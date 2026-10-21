# QA de APP-APP

Esta rama conserva las pruebas adicionales y las acciones de calidad. La rama `main` contiene la aplicación y los tests básicos del ejemplo del profesor.

## Pruebas automatizadas

- JUnit: validaciones, documentos y reglas de etapa.
- AndroidJUnit4: integración real de SQLite, persistencia y separación de usuarios.
- Compose UI Test: navegación entre pantallas.
- GitHub Actions: compilación Android, pruebas unitarias, emulador y resultados.

No se usa Playwright porque esta es una aplicación Android nativa.

Para la aplicación de presentación, los archivos de QA avanzado no se copian a `main`. La rama QA se actualiza con los cambios de `main` antes de verificar una nueva versión.

Las pruebas automatizadas no sustituyen por completo una revisión visual ni una comprobación de cámara y galería en un dispositivo físico.
