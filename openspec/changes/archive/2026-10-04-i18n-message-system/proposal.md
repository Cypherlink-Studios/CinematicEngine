# Proposal: Sistema de Internacionalización (i18n) y Adventure MiniMessage

## Why

Actualmente, todos los mensajes de feedback de comandos, errores de sintaxis, mensajes de ayuda y resultados de ejecución en `plugin-bootstrap` están hardcodeados en español como cadenas de texto plano estáticas. Esto impide personalizar los textos del plugin, dar soporte a comunidades angloparlantes o multilingües, y aprovechar las capacidades visuales modernas de Adventure MiniMessage (colores, gradientes y formato enriquecido) estándar en servidores Paper 1.20+.

Implementar un sistema de internacionalización (i18n) modular desacopla la lógica del motor de la capa de presentación textual y permite resolver dinámicamente los mensajes según la configuración del servidor o el idioma del cliente del jugador.

## What Changes

- **Nuevo servicio de mensajería e internacionalización (`MessageService` / `TranslationManager`)**:
  - Carga y parseo de bundles YAML (`locales/messages_es.yml` y `locales/messages_en.yml`).
  - Extracción automática de bundles por defecto desde los recursos del jar al directorio de datos del plugin.
  - Cadena de resolución y fallback: `player.locale()` (si está activo) -> idioma default del servidor (`config.yml`) -> bundle base español (`es`).
  - Renderizado nativo de texto enriquecido mediante Adventure `MiniMessage` y soporte de reemplazo de tags/placeholders contextuales (`<subcommand>`, `<name>`, `<count>`, etc.).
- **Internacionalización de comandos y feedback in-game (`CommandManager` y subcomandos)**:
  - Migración de respuestas de sintaxis, permisos, comandos desconocidos y menú de ayuda a claves de i18n renderizadas con MiniMessage.
- **Internacionalización de resultados de acciones cinemáticas (`CinematicActionResult`)**:
  - Refactor de `CinematicActionResult` para soportar claves de traducción parametrizadas con preservación de compatibilidad con cadenas de texto para testing.
- **Configuración y recarga en caliente**:
  - Adición de opciones `language: "es"` y `per-player-locale: true` en `config.yml`.
  - Recarga dinámica de bundles mediante `/cine reload`.

## Capabilities

### New Capabilities
- `i18n-messaging`: Servicio de internacionalización multilingüe para CinematicEngine con bundles YAML externos, resolución de idioma (servidor y cliente) y renderizado Adventure MiniMessage para comandos y feedback del motor.

### Modified Capabilities
<!-- Ninguna especificación existente cambia sus requerimientos -->

## Impact

- **Código afectado:**
  - `plugin-bootstrap`: `CommandManager`, subcomandos (`PlaySubcommand`, `StopSubcommand`, `PauseSubcommand`, `ResumeSubcommand`, `ReloadSubcommand`, `ListSubcommand`, `HelpSubcommand`), `CinematicActionResult`, `CinematicEnginePlugin`, `config.yml`.
  - Recursos: Creación de `resources/locales/messages_es.yml` y `resources/locales/messages_en.yml`.
- **APIs y Dependencias:**
  - Utiliza las APIs nativas de Paper 1.20.6 (`net.kyori.adventure.text.minimessage.MiniMessage`, `net.kyori.adventure.text.Component`) y `snakeyaml`. No se agregan nuevas dependencias externas.
- **Módulos no afectados:**
  - `cinematic-core`, `cinematic-camera`, `cinematic-actors`, `cinematic-adapters`, `cinematic-runtime` y `cinematic-dsl` se mantienen agnósticos de la capa de presentación de Bukkit/Paper.
