# Design: Arquitectura de Internacionalización (i18n) y Adventure MiniMessage

## Context

Actualmente los mensajes de interacción con el usuario (comandos, permisos, sintaxis, ayuda y estado de cinemáticas) están escritos directamente en español en clases de `plugin-bootstrap`. Para motivaciones detalladas e inventario completo, véase [proposal.md](proposal.md) y el documento de mapeo [i18n_messages_mapping.md](file:///C:/Users/antua/.gemini/antigravity/brain/0d120356-99ea-4761-b1b6-946484a1c0ba/i18n_messages_mapping.md).

Paper 1.20.6 incluye nativamente la librería Adventure (`net.kyori.adventure.text.*` y `MiniMessage`), permitiendo formateo moderno con tags semánticos sin requerir dependencias adicionales.

## Goals / Non-Goals

**Goals:**
- Implementar la interfaz `MessageService` y su implementación `DefaultMessageService` en `com.darkbladedev.cinematic.bootstrap.i18n`.
- Cargar y gestionar bundles externos en formato YAML ubicados en la carpeta `locales/` del plugin (`messages_es.yml` y `messages_en.yml`).
- Extracción automática de los archivos embebidos desde `resources/locales/` hacia el directorio de datos del plugin (`plugins/CinematicEngine/locales/`).
- Resolución de locale jerárquica: `Player.locale()` (si `per-player-locale` está habilitado) $\rightarrow$ `config.yml` (`language`) $\rightarrow$ bundle base embebido español (`es`).
- Renderizado completo con Adventure `MiniMessage` y soporte de placeholders contextuales mediante `TagResolver` o sustitución de tags unparsed.
- Adaptar `CommandManager`, subcomandos y `CinematicActionResult` para emplear claves de traducción desacopladas.
- Soportar recarga en caliente de archivos de idioma mediante `/cine reload`.
- Proveer implementaciones de prueba (`MockMessageService` / test bundles) para mantener el 100% de la suite de pruebas unitarias verde y estable.

**Non-Goals:**
- No internacionalizar las excepciones internas de validación en `cinematic-dsl` (`SceneDtoValidator`, `SnakeYamlSceneParser`), manteniéndolo como submódulo puramente desacoplado de la API de Bukkit.
- No implementar integración con servicios web externos de traducción automática.
- No usar formatos legacy con códigos de sección (`§` o `&`).

## Decisions

### 1. Ubicación y Estructura del Servicio (`MessageService`)
- **Decisión:** Ubicar `MessageService`, `DefaultMessageService` y utilidades de traducción dentro de `plugin-bootstrap` en el paquete `com.darkbladedev.cinematic.bootstrap.i18n`.
- **Razón:** `plugin-bootstrap` es el único módulo que interactúa directamente con `org.bukkit.command.CommandSender`, `Player` y el ciclo de vida de Bukkit/Paper. Evita introducir dependencias de Bukkit o Adventure en los módulos del núcleo (`cinematic-core`, `cinematic-dsl`).
- **Alternativas consideradas:**
  - *Módulo separado `cinematic-i18n`:* Agregaría complejidad de módulos innecesaria en Gradle para un plugin donde solo la capa bootstrap interactúa con jugadores.

### 2. Formato de Archivos de Traducción (YAML de Bukkit)
- **Decisión:** Utilizar archivos YAML con claves jerárquicas cargados mediante `YamlConfiguration.loadConfiguration(file)`.
- **Razón:** Integrado nativamente en Bukkit/Paper, maneja codificación UTF-8 de forma confiable, soporta comentarios y se adapta a la convención estándar del ecosistema Paper.
- **Alternativas consideradas:**
  - *ResourceBundle (.properties):* Poco legible para usuarios y diseñadores de servidores; no soporta jerarquías anidadas naturales.
  - *JSON:* Menos amigable para edición manual por administradores del servidor.

### 3. Renderizado y Sustitución con Adventure MiniMessage
- **Decisión:** Utilizar `MiniMessage.miniMessage()` como deserializador estándar. Los placeholders contextuales se pasan como pares clave-valor o `TagResolver` unparsed (ej. `<name>`, `<subcommand>`), evitando inyecciones de tags accidentales si el argumento contiene caracteres especiales.
- **Razón:** Robusto, previene inyección de formato en inputs de usuarios y permite colores degradados y estilos visuales modernos.

### 4. Desacoplamiento de `CinematicActionResult`
- **Decisión:** Extender `CinematicActionResult` para soportar `messageKey` y un mapa de argumentos opcionales, conservando `message()` con un mensaje formateado por defecto para compatibilidad.
- **Razón:** Permite que `DemoCinematicOrchestrator` reporte estados semánticos (`cinematic.play.started`, `cinematic.play.not_found`) sin tener que conocer de antemano el idioma del jugador que ejecutó el comando. `CommandManager` o los subcomandos resuelven la clave contra el `MessageService` usando el `sender` concreto.

### 5. Cadena de Resolución y Fallback
- **Decisión:** Implementar un fallback de 3 niveles:
  1. Idioma detectado del jugador (ej. `en_us` $\rightarrow$ `en`) si `per-player-locale: true`.
  2. Idioma por defecto en `config.yml` (`language: "es"`).
  3. Bundle base embebido en el `.jar` (`messages_es.yml`).
  Si una clave no existe en el idioma del jugador, se busca en el idioma del servidor y finalmente en el bundle base. Si no existe en ningún lado, se devuelve `"<red>[Missing key: " + key + "]</red>"`.

## Risks / Trade-offs

- **[Riesgo] Rotura de pruebas unitarias existentes:** `ListSubcommandTest` y `HelpSubcommandTest` comprueban cadenas específicas devueltas por los comandos.
  - *Mitigación:* Se inyectará `MessageService` en `CommandManager` y subcomandos. En pruebas unitarias, se utilizará una instancia con el bundle estándar o un mock predecible, adaptando las aserciones de prueba a las claves o textos generados por el bundle.
- **[Riesgo] Caracteres especiales o inyección en nombres de cinemáticas:** Si un nombre de cinemática contiene caracteres como `<` o `>`, MiniMessage podría intentar interpretarlo como tag.
  - *Mitigación:* Usar `Placeholder.unparsed("name", name)` o sanitización para asegurar que los valores dinámicos se traten como texto plano literal.
- **[Riesgo] Archivos YAML locales corruptos por edición manual:** Un error de sintaxis en `messages_es.yml` podría impedir la inicialización.
  - *Mitigación:* Manejo defensivo en la carga con `try-catch`, log de advertencia en consola y fallback automático al bundle embebido en memoria dentro del jar.

## Migration Plan

1. El archivo `config.yml` por defecto incluirá:
   ```yaml
   locale:
     default: "es"
     per-player: true
   ```
2. Al iniciar el plugin, si la carpeta `plugins/CinematicEngine/locales/` no contiene `messages_es.yml` o `messages_en.yml`, se extraerán desde los recursos del jar.
3. Se actualizan todos los subcomandos y `CommandManager` para recibir e invocar `MessageService`.
