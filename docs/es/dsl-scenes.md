---
title: Especificación de Escenas DSL
description: Referencia completa del lenguaje de dominio (DSL) en YAML para la creación de cinemáticas en CinematicEngine.
sidebar:
  order: 4
---

# Especificación de Escenas DSL

CinematicEngine utiliza un **lenguaje de dominio específico (DSL)** declarativo basado en YAML para estructurar cinemáticas. El formato es legible para humanos, modular y rigurosamente comprobado durante la carga por `SceneDtoValidator` y `SnakeYamlSceneParser`.

---

## 🏗️ Estructura Raíz de una Escena

Cada archivo de escena representa una secuencia temporal unificada y puede definir puesta en escena de actores, pistas concurrentes y metadatos:

```yaml
id: "castle_siege_intro"
duration: 300 # Duración total de la secuencia en ticks (20 ticks = 1 segundo)

metadata:
  title: "Introducción Dramática del Asedio"
  author: "BuildTeam"
  description: "Vuelo panorámico sobre las murallas del castillo con actores en escena."

# Puesta en escena opcional de actores (virtuales o clones)
actors:
  - id: "heroe"
    type: "self_clone"
    initial_position: [120.0, 64.0, 50.0]
    initial_yaw: 0.0
    initial_equipment:
      main_hand: "DIAMOND_SWORD"

tracks:
  - type: "camera"
    # ... configuración de la pista de cámara ...
  - type: "actor_motion"
    # ... trayectoria continua del actor ...
  - type: "actor_action"
    # ... poses, animaciones y equipamiento ...
  - type: "effect"
    # ... partículas y sonido sincronizados ...
```

### Propiedades de Nivel Superior

| Campo | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | `string` | **Sí** | Identificador único de la escena (usado en comandos como `/cine play <id>`). |
| `duration` | `integer` | **Sí** | Longitud de la secuencia en ticks de juego (`> 0`, ej. `200` = 10s). |
| `actors` | `list` | No | Declaración de elenco de actores puestos en escena (`virtual`, `self_clone`, `persistent`). |
| `metadata` | `map` | No | Metadatos informativos arbitrarios (ej. `title`, `author`, `description`). |
| `tracks` | `list` | **Sí** | Colección de pistas concurrentes en la línea temporal. |

---

## 🎭 Declaración de Actores (`actors`)

Cuando una cinemática requiere personajes dinámicos, se declaran en el bloque `actors:`:

```yaml
actors:
  - id: "villano"
    type: "virtual"                   # "virtual", "self_clone" o "persistent"
    skin: "DarkSorcerer"              # Nombre o UUID del skin de Mojang
    initial_position: [100.0, 64.0, 50.0]
    initial_yaw: 180.0
    initial_pitch: 0.0
    initial_equipment:
      main_hand: "NETHERITE_SWORD"
      helmet: "NETHERITE_HELMET"
```

| Campo | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | `string` | **Sí** | Identificador único del actor dentro de la escena. |
| `type` | `string` | No | `virtual` (por defecto), `self_clone` o `persistent`. |
| `skin` | `string` | No | Perfil de skin para actores virtuales. |
| `initial_position` | `[x, y, z]` | **Sí** | Coordenadas de aparición tridimensionales. |
| `initial_yaw` | `float` | No | Rotación horizontal inicial en grados (def. `0.0`). |
| `initial_pitch` | `float` | No | Inclinación vertical inicial en grados (def. `0.0`). |
| `initial_equipment`| `map` | No | Mapa de ranuras iniciales (`main_hand`, `off_hand`, armaduras). |

---

## 🛤️ Tipos de Pistas Soportadas (`tracks`)

CinematicEngine proporciona pistas especializadas para cada subsistema:

| Tipo (`type`) | Rol | Función Principal |
| :--- | :--- | :--- |
| `camera` | Continuo | Desplazamiento y orientación de cámara (splines, rigs y look-at). |
| `actor_motion` | Continuo | Trayectorias de movimiento de actores (LERP o splines con orientación tangente). |
| `actor_action` | Discreto | Poses (`CROUCHING`), animaciones (`SWING`), uso de ítems y swap de armas. |
| `effect` | Discreto | Emisión de partículas de Bukkit y reproducción de efectos de sonido. |
| `actor` | Continuo (Legado) | Pista combinada simple de movimiento de versiones anteriores. |

---

## 🎯 Especificación de Keyframes

Los keyframes dictan el valor de las propiedades en ticks específicos:

```yaml
keyframes:
  - tick: 0
    position: [100.0, 64.0, 100.0]
    yaw: -90.0
    pitch: 15.0
    fov: 70.0
    interpolation: "ease_in_out"
```

### Campos de Keyframe según la Pista:

| Campo | Tipo | Pistas Compatibles | Descripción |
| :--- | :--- | :--- | :--- |
| `tick` | `integer` | Todas | Tick de la línea temporal (`0 <= tick <= duration`). |
| `position` | `[x, y, z]` | `camera`, `actor_motion`, `actor` | Vector tridimensional de coordenadas mundiales. |
| `yaw` / `pitch` | `float` | `camera`, `actor_motion`, `actor` | Ángulos de orientación en grados. |
| `fov` | `float` | `camera` | Campo de visión (Field of View, default `70.0`). |
| `interpolation`| `string` | `camera`, `actor_motion` | Función de transición (`linear`, `ease_in_out`). |
| `pose` | `string` | `actor_action` | Pose del cuerpo (`STANDING`, `CROUCHING`, etc.). |
| `action` | `string` | `actor_action` | Animación de combate (`SWING_MAIN_HAND`, `HURT`, etc.). |
| `item_usage` | `string` | `actor_action` | Estado de uso (`BLOCKING`, `BOW_PULL`, etc.). |
| `equipment` | `map` | `actor_action` | Ranuras de armadura y mano modificadas en ese tick. |
| `particle` | `map` | `effect` | Parámetros de partícula a emitir. |
| `sound` | `map` | `effect` | Parámetros de sonido a reproducir. |

---

## 🛡️ Reglas de Validación (`SceneDtoValidator`)

Durante la carga o recarga con `/cine reload`, el validador aplica las siguientes reglas de consistencia:

1. **Puesta en Escena de Actores**:
   - Los identificadores de actores en `actors:` deben ser únicos.
   - Todo actor debe declarar coordenadas `initial_position` válidas de 3 componentes.
   - Pistas `actor_motion` y `actor_action` deben declarar un `actor_id` válido.
2. **Consistencia Cronológica**:
   - `tick` debe ser `>= 0` y `<= duration`.
   - Los keyframes se ordenan automáticamente por tick ascendente.
3. **Validación de Coordenadas**:
   - Los vectores `position`, `initial_position` y `target` deben ser listas numéricas de exactamente 3 elementos: `[x, y, z]`.
4. **Requisitos de Pista de Cámara**:
   - Requiere al menos 2 keyframes para definir una trayectoria.
   - Admite `look_at` / `look-at` con modos `fixed`, `static`, `actor` o `forward`.
   - El modo `actor` requiere `target_actor` (o `target-actor`).
