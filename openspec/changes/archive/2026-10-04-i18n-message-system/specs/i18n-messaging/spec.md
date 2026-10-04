# Spec Delta: i18n-messaging

## Purpose

Proporciona un sistema integral de internacionalización (i18n) para CinematicEngine que permite cargar paquetes de idiomas externos en formato YAML, resolver traducciones según el idioma configurado en el servidor o el cliente del jugador, y renderizar mensajes enriquecidos mediante Adventure MiniMessage para comandos y feedback del motor.

## ADDED Requirements

### Requirement: Configurable Language and Client Locale Resolution
The system SHALL allow configuring the default server language (`language`) and enabling or disabling client-based locale resolution (`per-player-locale`) in `config.yml`. When enabled and the receiver is a player, the system SHALL attempt to use the player's client locale.

#### Scenario: Server default locale used for console
- **WHEN** a command or feedback message is sent to a console command sender
- **THEN** the system resolves messages using the language configured in `config.yml`.

#### Scenario: Client locale used for players when enabled
- **WHEN** `per-player-locale` is true and a player with client locale `en_us` triggers a command
- **THEN** the system resolves messages using the English bundle (`en`).

#### Scenario: Server locale enforced when per-player resolution is disabled
- **WHEN** `per-player-locale` is false in `config.yml`
- **THEN** all players receive messages in the server's configured default language regardless of client locale.

### Requirement: External YAML Language Bundles with Fallback Chain
The system SHALL load language files from `locales/messages_<lang>.yml`. If a message file is missing on disk, the system SHALL extract embedded default bundles (`es` and `en`) from plugin resources. When resolving a message key, if the key is missing in the target locale, the system SHALL fall back to the server default language, and ultimately to the base embedded Spanish bundle (`es`).

#### Scenario: Extract default bundles on startup
- **WHEN** the plugin starts and `locales/messages_es.yml` does not exist on disk
- **THEN** the system extracts the embedded `messages_es.yml` and `messages_en.yml` to the plugin's locales folder.

#### Scenario: Fallback resolution for untranslated keys
- **WHEN** a message key is requested for an English locale but is not defined in `messages_en.yml`
- **THEN** the system retrieves and returns the corresponding translation from the default Spanish bundle.

#### Scenario: Hot reload of message bundles
- **WHEN** an administrator modifies a message YAML file and executes `/cine reload`
- **THEN** the system reloads all locale files from disk without requiring a server restart.

### Requirement: MiniMessage Rich Text Rendering and Placeholder Substitution
The system SHALL format all user-facing messages using Adventure MiniMessage. The system SHALL support dynamic contextual placeholders (such as `<subcommand>`, `<label>`, `<permission>`, `<name>`, and `<count>`) and replace them before sending the final Component to the recipient.

#### Scenario: Render MiniMessage tags as Components
- **WHEN** a bundle entry contains MiniMessage tags like `<gold>`, `<red>`, `<bold>`
- **THEN** the system renders the message as an Adventure Component preserving color and text decorations.

#### Scenario: Dynamic placeholder replacement
- **WHEN** a message template contains placeholders like `<name>` and an argument `name=intro` is supplied
- **THEN** the rendered Component displays `intro` in place of `<name>`.

### Requirement: Localized Command Feedback and Action Results
The system SHALL route all command feedback (unknown subcommands, syntax errors, permission rejections, and help listings) and cinematic operation outcomes (`CinematicActionResult` for play, stop, pause, resume, and reload) through the localization service.

#### Scenario: Localized syntax error on command execution
- **WHEN** a sender executes `/cine play` without specifying a cinematic name
- **THEN** the sender receives a localized invalid syntax message corresponding to their resolved locale.

#### Scenario: Localized cinematic action result
- **WHEN** an administrator starts a cinematic that does not exist
- **THEN** the command execution returns a localized failure message indicating that the cinematic was not found.
