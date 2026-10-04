---
title: Actores y Efectos Ambientales
description: Coreografía de jugadores, NPCs, entidades simuladas, ráfagas de partículas y audio espacial en CinematicEngine.
sidebar:
  order: 7
---

# Actores y Efectos Ambientales

CinematicEngine no se limita a manipular cámaras: incorpora pistas dedicadas para **actores** (personajes y figuras en la escena) y **efectos de entorno** (señales audiovisuales, partículas y sonidos).

---

## 🎭 El Sistema de Actores

Un **Actor** representa una entidad participante en la secuencia cinematográfica cuya posición, orientación y estado son dirigidos mediante una pista `ActorTrack`.

### Tipos de Actores Disponibles

| Clase de Actor | Tipo | Descripción |
| :--- | :--- | :--- |
| `PlayerActor` | Jugador Real | Controla al propio espectador o a jugadores presentes en el mundo. |
| `FakeEntityActor` | Entidad por Paquetes | Entidad virtual ligera enviada por paquetes solo visible para el cliente. |
| `NPCActor` | Entidad de Servidor / NPC | Entidad persistente de servidor o NPC (compatible con Citizens u otros sistemas). |

### Ejemplo de Pista de Actores en YAML

```yaml
tracks:
  - type: "actor"
    id: "guard_patrol"
    data:
      actor-id: "guard_patrol"
    keyframes:
      - tick: 0
        position: [100.0, 64.0, 50.0]
        yaw: 0.0
        pitch: 0.0
        interpolation: "linear"
      - tick: 80
        position: [100.0, 64.0, 70.0]
        yaw: 0.0
        pitch: 0.0
        interpolation: "linear"
      - tick: 100
        position: [100.0, 64.0, 70.0]
        yaw: 90.0
        pitch: 10.0
        interpolation: "ease_in_out"
```

---

## ✨ Pista de Efectos Ambientales (`EffectTrack`)

La pista `effect` dispara eventos discretos de audio y partículas sincronizados a ticks específicos de la línea temporal. A diferencia de las pistas continuas, los efectos se activan de forma puntual.

### 1. Efectos de Partículas (`ParticleEffect`)
Spawnea partículas de Minecraft en el mundo relativas a coordenadas del escenario:

```yaml
tracks:
  - type: "effect"
    id: "magic_sparkles"
    keyframes:
      - tick: 40
        particle:
          type: "ENCHANTMENT_TABLE"
          count: 50
          offset: [1.0, 1.5, 1.0]
          speed: 0.2
      - tick: 120
        particle:
          type: "EXPLOSION_LARGE"
          count: 1
          offset: [0.0, 0.0, 0.0]
```

#### Parámetros de Partícula:
- `type` (`string`): Nombre del enum `Particle` de Bukkit (ej. `FIREWORK`, `FLAME`, `SOUL_FIRE_FLAME`, `CAMPFIRE_SIGNAL_SMOKE`).
- `count` (`integer`): Cantidad de partículas emitidas.
- `offset` (`[dx, dy, dz]`): Dispersión tridimensional respecto al punto de emisión.
- `speed` (`float`, opcional): Velocidad de dispersión inicial.

---

### 2. Sonidos Posicionales (`SoundEffect`)
Reproduce pistas sonoras y efectos auditivos con modulación de volumen y tono:

```yaml
tracks:
  - type: "effect"
    id: "soundtrack"
    keyframes:
      - tick: 0
        sound:
          name: "music.credits"
          volume: 0.8
          pitch: 1.0
      - tick: 120
        sound:
          name: "entity.generic.explode"
          volume: 1.0
          pitch: 0.9
```

#### Parámetros de Sonido:
- `name` (`string`): Clave de sonido o recurso de Minecraft (ej. `entity.ender_dragon.growl`, `ui.toast.challenge_complete`, `music.dragon`).
- `volume` (`float`): Multiplicador de volumen (`0.0` a `1.0+`).
- `pitch` (`float`): Frecuencia de tono (`0.5` grave a `2.0` agudo).

---

## 🎬 Ejemplo de Coreografía Integrada

Coordinando pistas de cámara, actores y efectos, es posible construir escenas cinematográficas completas:

```yaml
id: "summoning_ritual"
duration: 100

tracks:
  # La cámara orbita el altar
  - type: "camera"
    id: "cam"
    data:
      path-mode: "spline"
      look-at:
        mode: "static"
        target: [0.5, 65.0, 0.5]
    keyframes:
      - tick: 0
        position: [10.0, 68.0, 0.0]
      - tick: 50
        position: [0.0, 70.0, 10.0]
      - tick: 100
        position: [-10.0, 68.0, 0.0]

  # El mago realiza el ritual
  - type: "actor"
    id: "wizard"
    keyframes:
      - tick: 0
        position: [0.5, 64.0, 0.5]
        yaw: 180.0
        pitch: -20.0

  # Clímax con partículas y sonido en el tick 50
  - type: "effect"
    id: "fx"
    keyframes:
      - tick: 50
        particle:
          type: "DRAGON_BREATH"
          count: 100
          offset: [0.5, 1.0, 0.5]
        sound:
          name: "entity.wither.spawn"
          volume: 1.0
          pitch: 1.2
```
