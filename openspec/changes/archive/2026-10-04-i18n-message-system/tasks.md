# Tasks: Sistema de Internacionalización (i18n) y Adventure MiniMessage

## 1. Bundles de Idioma y Configuración

- [x] 1.1 Crear `resources/locales/messages_es.yml` con el inventario completo de claves de Capa 1 y Capa 2 en formato MiniMessage y verificar sintaxis válida
- [x] 1.2 Crear `resources/locales/messages_en.yml` en inglés asegurando paridad exacta de claves con el bundle español
- [x] 1.3 Actualizar `plugin-bootstrap/src/main/resources/config.yml` con las secciones de configuración de idioma (`locale.default: "es"` y `locale.per-player: true`)

## 2. Core del Servicio de Mensajería (i18n)

- [x] 2.1 Diseñar e implementar la interfaz `MessageService` y la clase `DefaultMessageService` en `com.darkbladedev.cinematic.bootstrap.i18n` con carga de archivos YAML
- [x] 2.2 Implementar en `DefaultMessageService` la resolución jerárquica de idiomas (`player.locale()` -> config server -> base embebido) y renderizado con Adventure MiniMessage
- [x] 2.3 Implementar la extracción automática de bundles embebidos hacia `plugins/CinematicEngine/locales/` y soporte de recarga en caliente (`reload()`)
- [x] 2.4 Crear pruebas unitarias `DefaultMessageServiceTest` verificando resolución de claves, fallbacks, sustitución de placeholders y renderizado de componentes

## 3. Adaptación de `CinematicActionResult`

- [x] 3.1 Extender `CinematicActionResult` para admitir `messageKey` y mapa de argumentos contextuales conservando compatibilidad con el método `message()`
- [x] 3.2 Refactorizar `DemoCinematicOrchestrator` para emitir resultados con claves de traducción semánticas (`cinematic.play.*`, `cinematic.stop.*`, etc.)
- [x] 3.3 Crear y actualizar pruebas unitarias para `CinematicActionResult` y los flujos de estado en `DemoCinematicOrchestrator`

## 4. Internacionalización de Comandos y Subcomandos

- [x] 4.1 Inyectar `MessageService` en `CommandManager` y migrar feedback de error, permisos, subcomando desconocido y listado de ayuda a componentes MiniMessage
- [x] 4.2 Inyectar `MessageService` en todos los subcomandos (`Play`, `Stop`, `Pause`, `Resume`, `Reload`, `List`, `Help`) y traducir descripciones, sintaxis y respuestas
- [x] 4.3 Conectar la recarga del `MessageService` en `ReloadSubcommand` y la inicialización en `CinematicEnginePlugin.onEnable()`
- [x] 4.4 Actualizar la suite de pruebas unitarias de subcomandos (`HelpSubcommandTest`, `ListSubcommandTest`, etc.) inyectando un `MessageService` de test y asegurando paso exitoso

## 5. Verificación Integral y Cobertura

- [x] 5.1 Ejecutar build completo del proyecto `./gradlew test jacocoTestReport jacocoTestCoverageVerification` y verificar cobertura >= 80%
- [x] 5.2 Validar la conformidad del cambio con `openspec validate i18n-message-system --json`
